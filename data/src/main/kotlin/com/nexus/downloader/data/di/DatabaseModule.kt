package com.nexus.downloader.data.di

import android.content.Context
import androidx.room.Room
import com.nexus.downloader.common.constants.AppConstants
import com.nexus.downloader.data.database.NexusDatabase
import com.nexus.downloader.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNexusDatabase(@ApplicationContext context: Context): NexusDatabase =
        Room.databaseBuilder(context, NexusDatabase::class.java, AppConstants.DATABASE_NAME)
            .build()

    @Provides
    fun provideDownloadDao(database: NexusDatabase): DownloadDao = database.downloadDao()

    @Provides
    fun provideChunkDao(database: NexusDatabase): ChunkDao = database.chunkDao()

    @Provides
    fun providePlaylistItemDao(database: NexusDatabase): PlaylistItemDao = database.playlistItemDao()

    @Provides
    fun provideBrowserHistoryDao(database: NexusDatabase): BrowserHistoryDao = database.browserHistoryDao()

    @Provides
    fun provideBookmarkDao(database: NexusDatabase): BookmarkDao = database.bookmarkDao()

    @Provides
    fun provideSettingsDao(database: NexusDatabase): SettingsDao = database.settingsDao()
}
