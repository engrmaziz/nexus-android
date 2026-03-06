package com.nexus.downloader.core.downloadengine

/**
 * Tracks download speed using a rolling 5-second window and calculates ETA.
 */
class SpeedMonitor {

    private data class Sample(val timestampMs: Long, val bytes: Long)

    private val samples = ArrayDeque<Sample>()
    private val lock = Any()

    companion object {
        private const val WINDOW_MS = 5_000L
    }

    /** Records [bytes] downloaded at the current time. */
    fun recordBytes(bytes: Long) {
        if (bytes <= 0) return
        synchronized(lock) {
            val now = System.currentTimeMillis()
            samples.addLast(Sample(now, bytes))
            pruneOldSamples(now)
        }
    }

    /** Returns smoothed bytes-per-second over the last 5-second window. */
    fun getSpeedBytesPerSecond(): Long {
        synchronized(lock) {
            val now = System.currentTimeMillis()
            pruneOldSamples(now)
            if (samples.size < 2) return 0L
            val totalBytes = samples.sumOf { it.bytes }
            val elapsed = now - samples.first().timestampMs
            return if (elapsed <= 0L) 0L else totalBytes * 1_000L / elapsed
        }
    }

    /**
     * Estimates how many seconds remain to download [remainingBytes]
     * at the current speed. Returns [Long.MAX_VALUE] if speed is unknown.
     */
    fun calculateEtaSeconds(remainingBytes: Long): Long {
        val speed = getSpeedBytesPerSecond()
        return if (speed <= 0L || remainingBytes <= 0L) Long.MAX_VALUE
        else remainingBytes / speed
    }

    fun reset() {
        synchronized(lock) { samples.clear() }
    }

    private fun pruneOldSamples(nowMs: Long) {
        val cutoff = nowMs - WINDOW_MS
        while (samples.isNotEmpty() && samples.first().timestampMs < cutoff) {
            samples.removeFirst()
        }
    }
}
