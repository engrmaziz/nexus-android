package com.nexus.downloader.common.logger

import timber.log.Timber

object NexusLogger {
    fun init(isDebug: Boolean) {
        if (isDebug) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
    }

    fun d(message: String, vararg args: Any?) = Timber.d(message, *args)
    fun i(message: String, vararg args: Any?) = Timber.i(message, *args)
    fun w(message: String, vararg args: Any?) = Timber.w(message, *args)
    fun e(message: String, vararg args: Any?) = Timber.e(message, *args)
    fun e(throwable: Throwable, message: String, vararg args: Any?) = Timber.e(throwable, message, *args)

    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority < android.util.Log.WARN) return
            Timber.tag(tag ?: "Nexus").log(priority, message, t)
        }
    }
}
