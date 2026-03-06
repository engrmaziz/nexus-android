package com.nexus.downloader.domain.repository

import com.nexus.downloader.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun updateDefaultDownloadPath(path: String)
    suspend fun updateMaxConcurrentDownloads(max: Int)
    suspend fun updateWifiOnlyDownloads(enabled: Boolean)
    suspend fun updateDarkModeEnabled(enabled: Boolean)
    suspend fun updateNotificationsEnabled(enabled: Boolean)
}
