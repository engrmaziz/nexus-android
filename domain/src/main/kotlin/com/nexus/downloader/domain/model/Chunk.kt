package com.nexus.downloader.domain.model

data class Chunk(
    val chunkId: Long = 0,
    val downloadId: Long,
    val startByte: Long,
    val endByte: Long,
    val downloadedBytes: Long = 0,
    val status: ChunkStatus = ChunkStatus.PENDING
)

enum class ChunkStatus {
    PENDING, DOWNLOADING, COMPLETED, FAILED
}
