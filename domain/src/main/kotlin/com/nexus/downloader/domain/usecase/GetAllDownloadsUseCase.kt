package com.nexus.downloader.domain.usecase

import com.nexus.downloader.domain.model.Download
import com.nexus.downloader.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllDownloadsUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
) {
    operator fun invoke(): Flow<List<Download>> = downloadRepository.getAllDownloads()
}
