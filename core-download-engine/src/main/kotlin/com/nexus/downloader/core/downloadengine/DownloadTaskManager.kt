package com.nexus.downloader.core.downloadengine

import com.nexus.downloader.common.constants.AppConstants
import com.nexus.downloader.common.dispatcher.DispatcherProvider
import com.nexus.downloader.data.database.dao.ChunkDao
import com.nexus.downloader.data.database.dao.DownloadDao
import com.nexus.downloader.data.database.entity.ChunkEntity
import com.nexus.downloader.data.database.entity.DownloadEntity
import com.nexus.downloader.domain.model.DownloadStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central coordinator for all active downloads.
 *
 * Responsibilities:
 * - Manage active download coroutines
 * - Enforce max concurrent downloads ([AppConstants.MAX_CONCURRENT_DOWNLOADS])
 * - Maintain per-download state machines
 * - Schedule parallel chunk downloads
 * - Persist progress and state to Room
 */
@Singleton
class DownloadTaskManager @Inject constructor(
    private val downloadDao: DownloadDao,
    private val chunkDao: ChunkDao,
    private val okHttpClient: OkHttpClient,
    private val dispatchers: DispatcherProvider
) {

    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)

    private data class ActiveDownload(
        val job: Job,
        val stateMachine: DownloadStateMachine,
        val speedMonitor: SpeedMonitor,
        val paused: AtomicBoolean = AtomicBoolean(false)
    )

    private val active = ConcurrentHashMap<Long, ActiveDownload>()

    companion object {
        private const val TAG = "DownloadTaskManager"
        private const val PROGRESS_POLL_MS = 2_000L

        /**
         * Sentinel end-byte value stored in [ChunkEntity] when the server does not
         * expose file size (no Content-Length) or doesn't support range requests.
         * The [ChunkDownloader] treats any negative end-byte as "download until EOF".
         */
        const val UNKNOWN_END_BYTE = -1L
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    suspend fun startDownload(url: String, destination: String): Long {
        val fileName = extractFileName(url)
        val entity = DownloadEntity(
            url = url,
            fileName = fileName,
            destinationPath = destination,
            status = DownloadStatus.QUEUED.name
        )
        val downloadId = downloadDao.insertDownload(entity)
        Timber.tag(TAG).d("Queued download #$downloadId ← $url")
        launchDownload(downloadId, url, fileName, destination, resuming = false)
        return downloadId
    }

    suspend fun pauseDownload(downloadId: Long) {
        val entry = active[downloadId] ?: return
        if (!entry.stateMachine.canPause()) return
        Timber.tag(TAG).d("Pausing download #$downloadId")
        entry.paused.set(true)
        updateStatus(downloadId, DownloadStatus.PAUSED)
    }

    suspend fun resumeDownload(downloadId: Long) {
        if (active.containsKey(downloadId)) return  // already running
        val entity = downloadDao.getDownloadById(downloadId) ?: return
        if (entity.status != DownloadStatus.PAUSED.name) return
        Timber.tag(TAG).d("Resuming download #$downloadId")
        launchDownload(downloadId, entity.url, entity.fileName, entity.destinationPath, resuming = true)
    }

    suspend fun cancelDownload(downloadId: Long) {
        Timber.tag(TAG).d("Cancelling download #$downloadId")
        active[downloadId]?.let { entry ->
            entry.paused.set(true)     // stop chunk threads first
            entry.job.cancel()
            active.remove(downloadId)
        }
        updateStatus(downloadId, DownloadStatus.CANCELLED)
        chunkDao.deleteChunksByDownloadId(downloadId)

        // Delete the temp file
        val entity = downloadDao.getDownloadById(downloadId) ?: return
        val tempFile = FileAssembler.getTempFile(entity.destinationPath, entity.fileName)
        FileAssembler.deleteIfExists(tempFile)
    }

    fun getStatus(downloadId: Long): DownloadStatus? =
        active[downloadId]?.stateMachine?.current()

    /**
     * On app start: scan the database for any interrupted downloads and
     * automatically resume them.
     */
    suspend fun recoverDownloads() {
        Timber.tag(TAG).d("Scanning for recoverable downloads…")
        DownloadStateMachine.RECOVERABLE_STATES.forEach { status ->
            downloadDao.getDownloadsByStatusOnce(status.name).forEach { entity ->
                if (!active.containsKey(entity.id)) {
                    Timber.tag(TAG).d("Recovering download #${entity.id} (was ${entity.status})")
                    launchDownload(entity.id, entity.url, entity.fileName, entity.destinationPath, resuming = true)
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private fun launchDownload(
        downloadId: Long,
        url: String,
        fileName: String,
        destination: String,
        resuming: Boolean
    ) {
        // Respect max concurrent limit
        if (active.size >= AppConstants.MAX_CONCURRENT_DOWNLOADS) {
            scope.launch {
                updateStatus(downloadId, DownloadStatus.QUEUED)
                // Simple polling retry until a slot opens
                while (isActive && active.size >= AppConstants.MAX_CONCURRENT_DOWNLOADS) {
                    delay(2_000L)
                }
                if (isActive) {
                    launchDownload(downloadId, url, fileName, destination, resuming)
                }
            }
            return
        }

        val stateMachine = DownloadStateMachine(
            if (resuming) DownloadStatus.PAUSED else DownloadStatus.QUEUED
        )
        val speedMonitor = SpeedMonitor()
        val paused = AtomicBoolean(false)

        val job = scope.launch {
            try {
                executeDownload(downloadId, url, fileName, destination, resuming, stateMachine, speedMonitor, paused)
            } catch (e: CancellationException) {
                // Normal cancellation — no error state change
                throw e
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Download #$downloadId failed unexpectedly")
                stateMachine.transition(DownloadStatus.FAILED)
                updateStatus(downloadId, DownloadStatus.FAILED)
            } finally {
                active.remove(downloadId)
            }
        }

        active[downloadId] = ActiveDownload(job, stateMachine, speedMonitor, paused)
    }

    private suspend fun executeDownload(
        downloadId: Long,
        url: String,
        fileName: String,
        destination: String,
        resuming: Boolean,
        stateMachine: DownloadStateMachine,
        speedMonitor: SpeedMonitor,
        paused: AtomicBoolean
    ) {
        // ── Step 1: Fetch metadata ──────────────────────────────────────────
        stateMachine.transition(DownloadStatus.FETCHING_METADATA)
        updateStatus(downloadId, DownloadStatus.FETCHING_METADATA)

        val (fileSize, rangesSupported) = fetchMetadata(url)
            ?: run {
                stateMachine.transition(DownloadStatus.FAILED)
                updateStatus(downloadId, DownloadStatus.FAILED)
                return
            }

        downloadDao.getDownloadById(downloadId)?.let { existing ->
            downloadDao.updateDownload(existing.copy(fileSize = fileSize))
        }

        // Ensure destination directory exists
        File(destination).mkdirs()

        // ── Step 2: Allocate chunks ─────────────────────────────────────────
        stateMachine.transition(DownloadStatus.ALLOCATING_CHUNKS)
        updateStatus(downloadId, DownloadStatus.ALLOCATING_CHUNKS)

        val chunks: List<ChunkEntity> = if (resuming) {
            val existing = chunkDao.getChunksByDownloadIdOnce(downloadId)
            if (existing.isNotEmpty()) existing
            else createAndPersistChunks(downloadId, fileSize, rangesSupported)
        } else {
            chunkDao.deleteChunksByDownloadId(downloadId)
            createAndPersistChunks(downloadId, fileSize, rangesSupported)
        }

        // ── Step 3: Prepare temp file ───────────────────────────────────────
        val tempFile = FileAssembler.getTempFile(destination, fileName)
        if (!resuming && tempFile.exists()) tempFile.delete()
        if (!tempFile.exists()) {
            tempFile.parentFile?.mkdirs()
            tempFile.createNewFile()
        }
        RandomAccessFile(tempFile, "rw").use { raf ->
            // Pre-allocate file space to avoid fragmentation
            if (!resuming && fileSize > 0) raf.setLength(fileSize)

            // ── Step 4: Download all chunks ─────────────────────────────────
            stateMachine.transition(DownloadStatus.DOWNLOADING)
            updateStatus(downloadId, DownloadStatus.DOWNLOADING)

            val progressJob = launchProgressUpdater(
                downloadId, fileSize, speedMonitor, stateMachine, paused
            )

            val success = downloadAllChunks(
                downloadId, url, chunks, raf, paused, speedMonitor
            )

            progressJob.cancel()

            if (!success || paused.get()) {
                // Paused or partial failure — do not assemble
                if (paused.get()) {
                    stateMachine.transition(DownloadStatus.PAUSED)
                    // DB status was already set by pauseDownload()
                } else {
                    stateMachine.transition(DownloadStatus.FAILED)
                    updateStatus(downloadId, DownloadStatus.FAILED)
                }
                return
            }

            // ── Step 5: Verify integrity ────────────────────────────────────
            stateMachine.transition(DownloadStatus.VERIFYING)
            updateStatus(downloadId, DownloadStatus.VERIFYING)

            if (fileSize > 0 && !FileAssembler.verifyIntegrity(tempFile, fileSize)) {
                stateMachine.transition(DownloadStatus.FAILED)
                updateStatus(downloadId, DownloadStatus.FAILED)
                Timber.tag(TAG).e("Integrity check failed for download #$downloadId")
                return
            }

            // ── Step 6: Assemble final file ─────────────────────────────────
            stateMachine.transition(DownloadStatus.ASSEMBLING)
            updateStatus(downloadId, DownloadStatus.ASSEMBLING)

            val finalFile = FileAssembler.getFinalFile(destination, fileName)
            if (!FileAssembler.assemble(tempFile, finalFile)) {
                stateMachine.transition(DownloadStatus.FAILED)
                updateStatus(downloadId, DownloadStatus.FAILED)
                return
            }

            // ── Step 7: Complete ────────────────────────────────────────────
            stateMachine.transition(DownloadStatus.COMPLETED)
            downloadDao.getDownloadById(downloadId)?.let { existing ->
                downloadDao.updateDownload(
                    existing.copy(status = DownloadStatus.COMPLETED.name, progress = 100)
                )
            }
            Timber.tag(TAG).d("Download #$downloadId completed → ${finalFile.absolutePath}")
        }
    }

    /**
     * Sends an HTTP HEAD request to determine file size and range support.
     * Returns `(fileSize, rangesSupported)` or `null` on failure.
     */
    private suspend fun fetchMetadata(url: String): Pair<Long, Boolean>? {
        return try {
            val request = Request.Builder().url(url).head().build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Timber.tag(TAG).w("HEAD request failed: ${response.code}")
                return null
            }
            val contentLength = response.header("Content-Length")?.toLongOrNull() ?: 0L
            val acceptRanges = response.header("Accept-Ranges")?.equals("bytes", ignoreCase = true) == true
            response.close()
            Pair(contentLength, acceptRanges)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "fetchMetadata failed for $url")
            null
        }
    }

    private suspend fun createAndPersistChunks(
        downloadId: Long,
        fileSize: Long,
        rangesSupported: Boolean
    ): List<ChunkEntity> {
        if (!rangesSupported || fileSize == 0L) {
            // Single-chunk download for servers that don't support range requests
            // or when file size is unknown. endByte = -1 signals "unknown size";
            // the downloader will stop naturally when the response stream exhausts.
            val chunk = ChunkEntity(
                downloadId = downloadId,
                startByte = 0L,
                endByte = if (fileSize > 0) fileSize - 1 else UNKNOWN_END_BYTE
            )
            val id = chunkDao.insertChunk(chunk)
            return listOf(chunk.copy(chunkId = id))
        }

        val ranges = ChunkScheduler.createChunkRanges(fileSize)
        return ranges.map { range ->
            val entity = ChunkEntity(
                downloadId = downloadId,
                startByte = range.startByte,
                endByte = range.endByte
            )
            val id = chunkDao.insertChunk(entity)
            entity.copy(chunkId = id)
        }
    }

    /**
     * Launches all chunk downloaders in parallel and waits for them all.
     * Returns `true` only if every chunk completed successfully.
     */
    private suspend fun downloadAllChunks(
        downloadId: Long,
        url: String,
        chunks: List<ChunkEntity>,
        raf: RandomAccessFile,
        paused: AtomicBoolean,
        speedMonitor: SpeedMonitor
    ): Boolean = coroutineScope {
        val fileLock = Any()
        val results = chunks
            .filter { it.status != "COMPLETED" }
            .map { chunk ->
                async {
                    ChunkDownloader(
                        url = url,
                        chunk = chunk,
                        file = raf,
                        fileLock = fileLock,
                        okHttpClient = okHttpClient,
                        chunkDao = chunkDao,
                        onBytesDownloaded = speedMonitor::recordBytes,
                        isPaused = paused::get
                    ).download()
                }
            }
        results.awaitAll().all { it }
    }

    /**
     * Periodically updates the DownloadEntity's progress, speed, and ETA
     * in the database while the download is running.
     */
    private fun launchProgressUpdater(
        downloadId: Long,
        fileSize: Long,
        speedMonitor: SpeedMonitor,
        stateMachine: DownloadStateMachine,
        paused: AtomicBoolean
    ) = scope.launch {
        while (isActive && !stateMachine.isTerminal() && !paused.get()) {
            delay(PROGRESS_POLL_MS)
            try {
                val chunks = chunkDao.getChunksByDownloadIdOnce(downloadId)
                val downloaded = chunks.sumOf { it.downloadedBytes }
                val progress = if (fileSize > 0) (downloaded * 100 / fileSize).toInt() else 0
                val speed = speedMonitor.getSpeedBytesPerSecond()
                val eta = speedMonitor.calculateEtaSeconds(fileSize - downloaded)

                downloadDao.getDownloadById(downloadId)?.let { entity ->
                    downloadDao.updateDownload(
                        entity.copy(progress = progress, speed = speed, eta = eta)
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.tag(TAG).w(e, "Progress update failed for #$downloadId")
            }
        }
    }

    private suspend fun updateStatus(downloadId: Long, status: DownloadStatus) {
        downloadDao.getDownloadById(downloadId)?.let {
            downloadDao.updateDownload(it.copy(status = status.name))
        }
    }

    private fun extractFileName(url: String): String {
        val path = url.substringBefore('?').substringBefore('#')
        val name = path.substringAfterLast('/')
        return name.ifBlank { "download_${System.currentTimeMillis()}" }
    }
}
