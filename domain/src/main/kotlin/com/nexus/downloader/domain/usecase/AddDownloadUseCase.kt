package com.nexus.downloader.domain.usecase

import com.nexus.downloader.domain.model.Download
import com.nexus.downloader.domain.repository.DownloadRepository
import javax.inject.Inject

class AddDownloadUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
) {
    suspend operator fun invoke(download: Download): Long =
        downloadRepository.insertDownload(download)
}
