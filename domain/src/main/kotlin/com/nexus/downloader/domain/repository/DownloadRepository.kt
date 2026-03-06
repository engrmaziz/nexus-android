package com.nexus.downloader.domain.repository

import com.nexus.downloader.domain.model.Download
import com.nexus.downloader.domain.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun getAllDownloads(): Flow<List<Download>>
    fun getDownloadsByStatus(status: DownloadStatus): Flow<List<Download>>
    suspend fun getDownloadById(id: Long): Download?
    suspend fun insertDownload(download: Download): Long
    suspend fun updateDownload(download: Download)
    suspend fun deleteDownload(id: Long)
    suspend fun deleteAllDownloads()
}
