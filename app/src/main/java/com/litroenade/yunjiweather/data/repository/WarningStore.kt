package com.litroenade.yunjiweather.data.repository

import com.litroenade.yunjiweather.data.entity.WarningEntity
import com.litroenade.yunjiweather.data.local.WarningDao

class WarningStore(warningDao: WarningDao?) {

    private val warningDao: WarningDao = warningDao ?: throw NullPointerException("warningDao")

    fun replaceByLocation(locationId: String, warnings: List<WarningEntity>) {
        warningDao.replaceByLocation(locationId, warnings)
    }

    fun findByLocationId(locationId: String): List<WarningEntity> {
        return warningDao.findByLocationId(locationId)
    }

    fun findByWarningId(locationId: String, warningId: String): WarningEntity? {
        return warningDao.findByWarningId(locationId, warningId)
    }

    fun findUnnotifiedWarnings(): List<WarningEntity> {
        return warningDao.findUnnotifiedWarnings()
    }

    fun markRead(locationId: String, warningId: String) {
        warningDao.markRead(locationId, warningId)
    }

    fun markNotified(locationId: String, warningId: String) {
        warningDao.markNotified(locationId, warningId)
    }
}
