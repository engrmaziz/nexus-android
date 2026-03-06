package com.nexus.downloader.core.downloadengine

import com.nexus.downloader.data.database.dao.ChunkDao
import com.nexus.downloader.data.database.entity.ChunkEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.IOException
import java.io.RandomAccessFile

/**
 * Downloads a single byte-range chunk of a file using an HTTP Range request.
 *
 * Writes directly to [file] at the chunk's byte offset using [RandomAccessFile].
 * Persists progress to the database every [PROGRESS_UPDATE_INTERVAL_MS] milliseconds.
 * Respects the [isPaused] flag to implement cooperative pause.
 */
class ChunkDownloader(
    private val url: String,
    private var chunk: ChunkEntity,
    private val file: RandomAccessFile,
    private val fileLock: Any,
    private val okHttpClient: OkHttpClient,
    private val chunkDao: ChunkDao,
    private val onBytesDownloaded: (Long) -> Unit,
    private val isPaused: () -> Boolean
) {

    companion object {
        private const val BUFFER_SIZE = 8 * 1024           // 8 KB
        private const val PROGRESS_UPDATE_INTERVAL_MS = 2_000L
        const val MAX_RETRIES = 3
        private const val TAG = "ChunkDownloader"
    }

    /**
     * Downloads the chunk with up to [MAX_RETRIES] attempts.
     * Returns `true` if the chunk completed successfully, `false` otherwise.
     */
    suspend fun download(): Boolean = withContext(Dispatchers.IO) {
        var attempt = 0
        while (attempt < MAX_RETRIES && isActive) {
            try {
                val result = downloadInternal()
                if (result) return@withContext true
                // Paused – not a failure, return false without retrying
                if (isPaused()) return@withContext false
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                Timber.tag(TAG).w(e, "Chunk ${chunk.chunkId} attempt $attempt failed")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Chunk ${chunk.chunkId} unexpected error on attempt $attempt")
            }
            attempt++
            if (attempt < MAX_RETRIES) delay(1_000L * attempt)
        }
        // Mark chunk as FAILED after all retries exhausted
        chunkDao.updateChunk(chunk.copy(status = "FAILED"))
        false
    }

    private suspend fun downloadInternal(): Boolean {
        val resumeOffset = chunk.downloadedBytes
        val startByte = chunk.startByte + resumeOffset
        val endByte = chunk.endByte
        val unknownSize = endByte < 0

        // Chunk is already complete (only deterministic when size is known)
        if (!unknownSize && startByte > endByte) {
            chunkDao.updateChunk(chunk.copy(status = "COMPLETED"))
            return true
        }

        val requestBuilder = Request.Builder().url(url)
        if (unknownSize) {
            requestBuilder.header("Range", "bytes=$startByte-")
        } else {
            requestBuilder.header("Range", "bytes=$startByte-$endByte")
        }
        val request = requestBuilder.build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            response.close()
            throw IOException("Server returned ${response.code} for range $startByte-$endByte")
        }

        val body = response.body ?: run {
            response.close()
            throw IOException("Empty response body for chunk ${chunk.chunkId}")
        }

        var downloadedInSession = 0L
        var lastProgressUpdate = System.currentTimeMillis()

        body.byteStream().use { inputStream ->
            val buffer = ByteArray(BUFFER_SIZE)

            while (true) {
                val bytesRead = inputStream.read(buffer)
                if (bytesRead == -1) break
                if (isPaused()) {
                    // Persist current progress and stop cooperatively
                    val saved = chunk.copy(
                        downloadedBytes = resumeOffset + downloadedInSession,
                        status = "PAUSED"
                    )
                    chunkDao.updateChunk(saved)
                    chunk = saved
                    return false
                }

                val writePos = chunk.startByte + resumeOffset + downloadedInSession
                synchronized(fileLock) {
                    file.seek(writePos)
                    file.write(buffer, 0, bytesRead)
                }

                downloadedInSession += bytesRead
                onBytesDownloaded(bytesRead.toLong())

                val now = System.currentTimeMillis()
                if (now - lastProgressUpdate >= PROGRESS_UPDATE_INTERVAL_MS) {
                    val updated = chunk.copy(
                        downloadedBytes = resumeOffset + downloadedInSession,
                        status = "DOWNLOADING"
                    )
                    chunkDao.updateChunk(updated)
                    chunk = updated
                    lastProgressUpdate = now
                }
            }
        }

        val totalDownloaded = resumeOffset + downloadedInSession
        // If endByte is unknown (-1), the stream ended naturally → complete.
        // Otherwise verify we received all expected bytes.
        val expectedBytes = if (!unknownSize) endByte - chunk.startByte + 1 else totalDownloaded
        val complete = totalDownloaded >= expectedBytes

        val finalChunk = chunk.copy(
            downloadedBytes = totalDownloaded,
            status = if (complete) "COMPLETED" else "FAILED"
        )
        chunkDao.updateChunk(finalChunk)
        chunk = finalChunk
        return complete
    }
}
