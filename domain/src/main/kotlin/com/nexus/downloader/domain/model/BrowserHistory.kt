package com.nexus.downloader.domain.model

data class BrowserHistory(
    val id: Long = 0,
    val url: String,
    val title: String,
    val visitedAt: Long = System.currentTimeMillis()
)
