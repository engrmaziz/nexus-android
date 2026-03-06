package com.nexus.downloader.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val fileName: String,
    val fileSize: Long = 0,
    val status: String = "PENDING",
    val progress: Int = 0,
    val speed: Long = 0,
    val eta: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)
