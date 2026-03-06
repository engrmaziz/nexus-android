package com.nexus.downloader.domain.model

data class AppSettings(
    val defaultDownloadPath: String = "Downloads/Nexus",
    val maxConcurrentDownloads: Int = 3,
    val wifiOnlyDownloads: Boolean = false,
    val darkModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true
)
