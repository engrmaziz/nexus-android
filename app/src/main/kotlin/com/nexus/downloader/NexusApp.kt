package com.nexus.downloader

import android.app.Application
import com.nexus.downloader.common.logger.NexusLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NexusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NexusLogger.init(BuildConfig.DEBUG)
    }
}
