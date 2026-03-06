package com.nexus.downloader.data.repository

import com.nexus.downloader.data.database.dao.PlaylistItemDao
import com.nexus.downloader.data.database.entity.PlaylistItemEntity
import com.nexus.downloader.domain.model.PlaylistItem
import com.nexus.downloader.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistItemDao: PlaylistItemDao
) : PlaylistRepository {

    override fun getPlaylistItems(playlistUrl: String): Flow<List<PlaylistItem>> =
        playlistItemDao.getPlaylistItems(playlistUrl).map { entities -> entities.map { it.toDomain() } }

    override suspend fun insertPlaylistItem(item: PlaylistItem): Long =
        playlistItemDao.insertPlaylistItem(item.toEntity())

    override suspend fun deletePlaylistItem(id: Long) =
        playlistItemDao.deletePlaylistItem(id)

    override suspend fun deletePlaylist(playlistUrl: String) =
        playlistItemDao.deletePlaylist(playlistUrl)

    private fun PlaylistItemEntity.toDomain() = PlaylistItem(
        id = id,
        playlistUrl = playlistUrl,
        videoUrl = videoUrl,
        title = title,
        duration = duration,
        thumbnail = thumbnail
    )

    private fun PlaylistItem.toEntity() = PlaylistItemEntity(
        id = id,
        playlistUrl = playlistUrl,
        videoUrl = videoUrl,
        title = title,
        duration = duration,
        thumbnail = thumbnail
    )
}
