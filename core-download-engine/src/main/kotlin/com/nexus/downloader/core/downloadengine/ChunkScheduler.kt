package com.nexus.downloader.core.downloadengine

/**
 * Determines how to segment a file download into parallel chunks.
 *
 * Chunk count rules:
 *   0 – 50 MB   → 4 chunks
 *   50 – 500 MB → 8 chunks
 *   500 MB – 2 GB → 12 chunks
 *   2 GB+        → 16 chunks
 */
object ChunkScheduler {

    private const val MB = 1024 * 1024L
    private const val GB = 1024 * MB

    data class ChunkRange(val index: Int, val startByte: Long, val endByte: Long) {
        val size: Long get() = endByte - startByte + 1
    }

    fun calculateChunkCount(fileSize: Long): Int = when {
        fileSize <= 50 * MB -> 4
        fileSize <= 500 * MB -> 8
        fileSize <= 2 * GB -> 12
        else -> 16
    }

    /**
     * Splits [fileSize] bytes into non-overlapping [ChunkRange] instances.
     * The last chunk absorbs any remainder from integer division.
     */
    fun createChunkRanges(fileSize: Long): List<ChunkRange> {
        val count = calculateChunkCount(fileSize)
        val chunkSize = fileSize / count
        return (0 until count).map { i ->
            val start = i * chunkSize
            val end = if (i == count - 1) fileSize - 1 else ((i + 1) * chunkSize) - 1
            ChunkRange(i, start, end)
        }
    }
}
