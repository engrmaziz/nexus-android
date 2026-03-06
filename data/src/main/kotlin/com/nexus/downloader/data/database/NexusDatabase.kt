package com.nexus.downloader.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nexus.downloader.data.database.dao.*
import com.nexus.downloader.data.database.entity.*

@Database(
    entities = [
        DownloadEntity::class,
        ChunkEntity::class,
        PlaylistItemEntity::class,
        BrowserHistoryEntity::class,
        BookmarkEntity::class,
        SettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class NexusDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun chunkDao(): ChunkDao
    abstract fun playlistItemDao(): PlaylistItemDao
    abstract fun browserHistoryDao(): BrowserHistoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun settingsDao(): SettingsDao
}
