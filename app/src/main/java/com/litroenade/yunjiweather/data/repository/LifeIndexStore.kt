package com.litroenade.yunjiweather.data.repository

import com.litroenade.yunjiweather.data.model.LifeIndexItem

interface LifeIndexStore {
    fun save(
        locationId: String,
        cityName: String,
        items: List<LifeIndexItem>,
        updateTime: Long,
        expireTime: Long
    )

    fun readValid(locationId: String, nowTime: Long): CacheRecord?

    open class CacheRecord(
        val items: List<LifeIndexItem>,
        val updateTime: Long
    )
}
