package com.nexus.downloader.data.database.dao

import androidx.room.*
import com.nexus.downloader.data.database.entity.PlaylistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistItemDao {
    @Query("SELECT * FROM playlist_items WHERE playlistUrl = :playlistUrl")
    fun getPlaylistItems(playlistUrl: String): Flow<List<PlaylistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItem(item: PlaylistItemEntity): Long

    @Query("DELETE FROM playlist_items WHERE id = :id")
    suspend fun deletePlaylistItem(id: Long)

    @Query("DELETE FROM playlist_items WHERE playlistUrl = :playlistUrl")
    suspend fun deletePlaylist(playlistUrl: String)
}
