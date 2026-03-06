package com.nexus.downloader.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist_items")
data class PlaylistItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playlistUrl: String,
    val videoUrl: String,
    val title: String,
    val duration: Long = 0,
    val thumbnail: String = ""
)
