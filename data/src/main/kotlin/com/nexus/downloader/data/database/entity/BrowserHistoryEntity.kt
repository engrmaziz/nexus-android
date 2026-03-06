package com.nexus.downloader.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "browser_history")
data class BrowserHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val title: String,
    val visitedAt: Long = System.currentTimeMillis()
)
