package com.nexus.downloader.core.playlistengine

interface PlaylistEngine {
    suspend fun loadPlaylist(url: String): List<PlaylistEntry>
    suspend fun downloadPlaylist(url: String, destinationDir: String): Boolean
}

data class PlaylistEntry(
    val url: String,
    val title: String,
    val duration: Long = 0
)
