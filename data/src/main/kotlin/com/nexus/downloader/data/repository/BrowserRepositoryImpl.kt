package com.nexus.downloader.data.repository

import com.nexus.downloader.data.database.dao.BookmarkDao
import com.nexus.downloader.data.database.dao.BrowserHistoryDao
import com.nexus.downloader.data.database.entity.BookmarkEntity
import com.nexus.downloader.data.database.entity.BrowserHistoryEntity
import com.nexus.downloader.domain.model.Bookmark
import com.nexus.downloader.domain.model.BrowserHistory
import com.nexus.downloader.domain.repository.BrowserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrowserRepositoryImpl @Inject constructor(
    private val browserHistoryDao: BrowserHistoryDao,
    private val bookmarkDao: BookmarkDao
) : BrowserRepository {

    override fun getBrowserHistory(): Flow<List<BrowserHistory>> =
        browserHistoryDao.getBrowserHistory().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addToHistory(history: BrowserHistory) =
        browserHistoryDao.insertHistory(history.toEntity())

    override suspend fun clearHistory() =
        browserHistoryDao.clearHistory()

    override fun getBookmarks(): Flow<List<Bookmark>> =
        bookmarkDao.getBookmarks().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addBookmark(bookmark: Bookmark) {
        bookmarkDao.insertBookmark(bookmark.toEntity())
    }

    override suspend fun deleteBookmark(id: Long) =
        bookmarkDao.deleteBookmark(id)

    private fun BrowserHistoryEntity.toDomain() = BrowserHistory(
        id = id, url = url, title = title, visitedAt = visitedAt
    )

    private fun BrowserHistory.toEntity() = BrowserHistoryEntity(
        id = id, url = url, title = title, visitedAt = visitedAt
    )

    private fun BookmarkEntity.toDomain() = Bookmark(
        id = id, url = url, title = title, createdAt = createdAt
    )

    private fun Bookmark.toEntity() = BookmarkEntity(
        id = id, url = url, title = title, createdAt = createdAt
    )
}
