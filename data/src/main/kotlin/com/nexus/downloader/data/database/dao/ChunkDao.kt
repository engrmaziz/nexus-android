package com.nexus.downloader.data.database.dao

import androidx.room.*
import com.nexus.downloader.data.database.entity.ChunkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChunkDao {
    @Query("SELECT * FROM chunks WHERE downloadId = :downloadId")
    fun getChunksByDownloadId(downloadId: Long): Flow<List<ChunkEntity>>

    @Query("SELECT * FROM chunks WHERE downloadId = :downloadId")
    suspend fun getChunksByDownloadIdOnce(downloadId: Long): List<ChunkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunk(chunk: ChunkEntity): Long

    @Update
    suspend fun updateChunk(chunk: ChunkEntity)

    @Query("DELETE FROM chunks WHERE downloadId = :downloadId")
    suspend fun deleteChunksByDownloadId(downloadId: Long)
}
