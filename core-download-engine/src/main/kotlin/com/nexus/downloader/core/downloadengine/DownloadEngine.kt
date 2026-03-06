package com.nexus.downloader.core.downloadengine

import com.nexus.downloader.domain.model.DownloadStatus

interface DownloadEngine {
    suspend fun startDownload(url: String, destination: String): Long
    suspend fun pauseDownload(downloadId: Long)
    suspend fun resumeDownload(downloadId: Long)
    suspend fun cancelDownload(downloadId: Long)
    suspend fun getDownloadStatus(downloadId: Long): DownloadStatus?
    suspend fun recoverDownloads()
}
