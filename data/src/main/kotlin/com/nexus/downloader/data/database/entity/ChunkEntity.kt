package com.nexus.downloader.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chunks",
    foreignKeys = [
        ForeignKey(
            entity = DownloadEntity::class,
            parentColumns = ["id"],
            childColumns = ["downloadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("downloadId")]
)
data class ChunkEntity(
    @PrimaryKey(autoGenerate = true)
    val chunkId: Long = 0,
    val downloadId: Long,
    val startByte: Long,
    val endByte: Long,
    val downloadedBytes: Long = 0,
    val status: String = "PENDING"
)
