package com.nexus.downloader.core.downloadengine

import com.nexus.downloader.domain.model.DownloadStatus
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of [DownloadEngine].
 *
 * Delegates all download management to [DownloadTaskManager].
 * On creation, automatically recovers any downloads that were in progress
 * when the app was last terminated.
 */
@Singleton
class DownloadEngineImpl @Inject constructor(
    private val taskManager: DownloadTaskManager
) : DownloadEngine {

    companion object {
        private const val TAG = "DownloadEngineImpl"

        /** A publicly accessible sample URL for integration testing. */
        const val SAMPLE_URL =
            "https://speed.hetzner.de/100MB.bin"
    }

    override suspend fun startDownload(url: String, destination: String): Long {
        Timber.tag(TAG).d("startDownload url=$url dest=$destination")
        return taskManager.startDownload(url, destination)
    }

    override suspend fun pauseDownload(downloadId: Long) {
        Timber.tag(TAG).d("pauseDownload #$downloadId")
        taskManager.pauseDownload(downloadId)
    }

    override suspend fun resumeDownload(downloadId: Long) {
        Timber.tag(TAG).d("resumeDownload #$downloadId")
        taskManager.resumeDownload(downloadId)
    }

    override suspend fun cancelDownload(downloadId: Long) {
        Timber.tag(TAG).d("cancelDownload #$downloadId")
        taskManager.cancelDownload(downloadId)
    }

    override suspend fun getDownloadStatus(downloadId: Long): DownloadStatus? =
        taskManager.getStatus(downloadId)

    override suspend fun recoverDownloads() {
        Timber.tag(TAG).d("recoverDownloads")
        taskManager.recoverDownloads()
    }

    /**
     * Convenience method for testing the engine end-to-end.
     * Downloads a sample 100 MB file into [destination] and returns the download ID.
     */
    suspend fun testDownload(destination: String): Long =
        startDownload(SAMPLE_URL, destination)
}
