package com.nexus.downloader.data.di

import com.nexus.downloader.data.datastore.NexusDataStore
import com.nexus.downloader.data.repository.BrowserRepositoryImpl
import com.nexus.downloader.data.repository.DownloadRepositoryImpl
import com.nexus.downloader.data.repository.PlaylistRepositoryImpl
import com.nexus.downloader.domain.repository.BrowserRepository
import com.nexus.downloader.domain.repository.DownloadRepository
import com.nexus.downloader.domain.repository.PlaylistRepository
import com.nexus.downloader.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDownloadRepository(impl: DownloadRepositoryImpl): DownloadRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(impl: PlaylistRepositoryImpl): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindBrowserRepository(impl: BrowserRepositoryImpl): BrowserRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: NexusDataStore): SettingsRepository
}
