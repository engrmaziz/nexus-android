package com.nexus.downloader.core.downloadengine.di

import com.nexus.downloader.core.downloadengine.DownloadEngine
import com.nexus.downloader.core.downloadengine.DownloadEngineImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DownloadEngineModule {

    @Binds
    @Singleton
    abstract fun bindDownloadEngine(impl: DownloadEngineImpl): DownloadEngine
}
