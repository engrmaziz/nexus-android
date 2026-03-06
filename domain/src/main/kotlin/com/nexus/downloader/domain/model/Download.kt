package com.nexus.downloader.domain.model

data class Download(
    val id: Long = 0,
    val url: String,
    val fileName: String,
    val fileSize: Long = 0,
    val destinationPath: String = "",
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val progress: Int = 0,
    val speed: Long = 0,
    val eta: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class DownloadStatus {
    PENDING,
    QUEUED,
    FETCHING_METADATA,
    ALLOCATING_CHUNKS,
    DOWNLOADING,
    PAUSED,
    VERIFYING,
    ASSEMBLING,
    COMPLETED,
    FAILED,
    CANCELLED
}
