package com.nexus.downloader.domain.model

data class Bookmark(
    val id: Long = 0,
    val url: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis()
)
