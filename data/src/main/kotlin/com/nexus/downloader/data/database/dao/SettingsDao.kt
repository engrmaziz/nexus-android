package com.nexus.downloader.data.database.dao

import androidx.room.*
import com.nexus.downloader.data.database.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings")
    fun getAllSettings(): Flow<List<SettingsEntity>>

    @Query("SELECT * FROM settings WHERE `key` = :key")
    suspend fun getSettingByKey(key: String): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: SettingsEntity)

    @Query("DELETE FROM settings WHERE `key` = :key")
    suspend fun deleteSettingByKey(key: String)
}
