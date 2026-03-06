package com.nexus.downloader.data.repository

import com.nexus.downloader.data.database.dao.DownloadDao
import com.nexus.downloader.data.database.entity.DownloadEntity
import com.nexus.downloader.domain.model.Download
import com.nexus.downloader.domain.model.DownloadStatus
import com.nexus.downloader.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepositoryImpl @Inject constructor(
    private val downloadDao: DownloadDao
) : DownloadRepository {

    override fun getAllDownloads(): Flow<List<Download>> =
        downloadDao.getAllDownloads().map { entities -> entities.map { it.toDomain() } }

    override fun getDownloadsByStatus(status: DownloadStatus): Flow<List<Download>> =
        downloadDao.getDownloadsByStatus(status.name).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getDownloadById(id: Long): Download? =
        downloadDao.getDownloadById(id)?.toDomain()

    override suspend fun insertDownload(download: Download): Long =
        downloadDao.insertDownload(download.toEntity())

    override suspend fun updateDownload(download: Download) =
        downloadDao.updateDownload(download.toEntity())

    override suspend fun deleteDownload(id: Long) =
        downloadDao.deleteDownload(id)

    override suspend fun deleteAllDownloads() =
        downloadDao.deleteAllDownloads()

    private fun DownloadEntity.toDomain() = Download(
        id = id,
        url = url,
        fileName = fileName,
        fileSize = fileSize,
        destinationPath = destinationPath,
        status = runCatching { DownloadStatus.valueOf(status) }.getOrDefault(DownloadStatus.QUEUED),
        progress = progress,
        speed = speed,
        eta = eta,
        createdAt = createdAt
    )

    private fun Download.toEntity() = DownloadEntity(
        id = id,
        url = url,
        fileName = fileName,
        fileSize = fileSize,
        destinationPath = destinationPath,
        status = status.name,
        progress = progress,
        speed = speed,
        eta = eta,
        createdAt = createdAt
    )
}
