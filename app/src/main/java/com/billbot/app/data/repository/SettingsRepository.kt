package com.billbot.app.data.repository

import com.billbot.app.data.local.dao.BusinessSettingsDao
import com.billbot.app.data.local.entity.BusinessSettingsEntity
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDao: BusinessSettingsDao) {

    val settingsFlow: Flow<BusinessSettingsEntity?> = settingsDao.getSettingsFlow()

    suspend fun getSettings(): BusinessSettingsEntity {
        return settingsDao.getSettings() ?: BusinessSettingsEntity()
    }

    suspend fun updateSettings(settings: BusinessSettingsEntity) {
        settingsDao.insertOrUpdate(settings)
    }

    suspend fun incrementInvoiceSequence() {
        settingsDao.incrementInvoiceSequence()
    }
}
