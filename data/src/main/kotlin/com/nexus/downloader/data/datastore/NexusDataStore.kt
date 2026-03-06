package com.nexus.downloader.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.nexus.downloader.common.constants.AppConstants
import com.nexus.downloader.domain.model.AppSettings
import com.nexus.downloader.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nexus_settings")

@Singleton
class NexusDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object Keys {
        val DEFAULT_DOWNLOAD_PATH = stringPreferencesKey("default_download_path")
        val MAX_CONCURRENT_DOWNLOADS = intPreferencesKey("max_concurrent_downloads")
        val WIFI_ONLY_DOWNLOADS = booleanPreferencesKey("wifi_only_downloads")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    override fun getSettings(): Flow<AppSettings> =
        context.dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences ->
                AppSettings(
                    defaultDownloadPath = preferences[Keys.DEFAULT_DOWNLOAD_PATH] ?: AppConstants.DEFAULT_DOWNLOAD_PATH,
                    maxConcurrentDownloads = preferences[Keys.MAX_CONCURRENT_DOWNLOADS] ?: AppConstants.MAX_CONCURRENT_DOWNLOADS,
                    wifiOnlyDownloads = preferences[Keys.WIFI_ONLY_DOWNLOADS] ?: false,
                    darkModeEnabled = preferences[Keys.DARK_MODE_ENABLED] ?: false,
                    notificationsEnabled = preferences[Keys.NOTIFICATIONS_ENABLED] ?: true
                )
            }

    override suspend fun updateDefaultDownloadPath(path: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DEFAULT_DOWNLOAD_PATH] = path
        }
    }

    override suspend fun updateMaxConcurrentDownloads(max: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.MAX_CONCURRENT_DOWNLOADS] = max
        }
    }

    override suspend fun updateWifiOnlyDownloads(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.WIFI_ONLY_DOWNLOADS] = enabled
        }
    }

    override suspend fun updateDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DARK_MODE_ENABLED] = enabled
        }
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }
}
