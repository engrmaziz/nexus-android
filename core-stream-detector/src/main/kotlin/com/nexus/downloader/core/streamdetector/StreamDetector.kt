package com.nexus.downloader.core.streamdetector

interface StreamDetector {
    suspend fun detectStreams(url: String): List<StreamInfo>
}

data class StreamInfo(
    val url: String,
    val quality: String,
    val format: String,
    val size: Long = 0
)
