package com.nexus.downloader.domain.repository

import com.nexus.downloader.domain.model.PlaylistItem
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getPlaylistItems(playlistUrl: String): Flow<List<PlaylistItem>>
    suspend fun insertPlaylistItem(item: PlaylistItem): Long
    suspend fun deletePlaylistItem(id: Long)
    suspend fun deletePlaylist(playlistUrl: String)
}
