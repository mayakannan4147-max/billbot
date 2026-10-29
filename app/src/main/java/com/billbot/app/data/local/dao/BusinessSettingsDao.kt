package com.billbot.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.billbot.app.data.local.entity.BusinessSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessSettingsDao {
    @Query("SELECT * FROM business_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<BusinessSettingsEntity?>

    @Query("SELECT * FROM business_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): BusinessSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: BusinessSettingsEntity)

    @Query("UPDATE business_settings SET nextInvoiceSequence = nextInvoiceSequence + 1 WHERE id = 1")
    suspend fun incrementInvoiceSequence()
}
