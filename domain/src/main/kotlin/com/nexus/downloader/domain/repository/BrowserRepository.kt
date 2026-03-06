package com.nexus.downloader.domain.repository

import com.nexus.downloader.domain.model.Bookmark
import com.nexus.downloader.domain.model.BrowserHistory
import kotlinx.coroutines.flow.Flow

interface BrowserRepository {
    fun getBrowserHistory(): Flow<List<BrowserHistory>>
    suspend fun addToHistory(history: BrowserHistory)
    suspend fun clearHistory()
    fun getBookmarks(): Flow<List<Bookmark>>
    suspend fun addBookmark(bookmark: Bookmark)
    suspend fun deleteBookmark(id: Long)
}
