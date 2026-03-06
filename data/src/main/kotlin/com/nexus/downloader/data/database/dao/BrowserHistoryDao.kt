package com.nexus.downloader.data.database.dao

import androidx.room.*
import com.nexus.downloader.data.database.entity.BrowserHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowserHistoryDao {
    @Query("SELECT * FROM browser_history ORDER BY visitedAt DESC")
    fun getBrowserHistory(): Flow<List<BrowserHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: BrowserHistoryEntity)

    @Query("DELETE FROM browser_history")
    suspend fun clearHistory()
}
