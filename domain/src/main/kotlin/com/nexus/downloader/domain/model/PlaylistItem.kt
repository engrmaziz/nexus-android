package com.nexus.downloader.domain.model

data class PlaylistItem(
    val id: Long = 0,
    val playlistUrl: String,
    val videoUrl: String,
    val title: String,
    val duration: Long = 0,
    val thumbnail: String = ""
)
