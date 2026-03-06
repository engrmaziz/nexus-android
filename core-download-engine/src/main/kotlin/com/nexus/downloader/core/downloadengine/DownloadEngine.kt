package com.nexus.downloader.core.downloadengine

interface DownloadEngine {
    suspend fun startDownload(url: String, destination: String): Boolean
    suspend fun pauseDownload(downloadId: Long)
    suspend fun resumeDownload(downloadId: Long)
    suspend fun cancelDownload(downloadId: Long)
}
