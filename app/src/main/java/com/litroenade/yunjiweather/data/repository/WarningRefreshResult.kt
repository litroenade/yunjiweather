package com.litroenade.yunjiweather.data.repository

import com.litroenade.yunjiweather.data.entity.WarningEntity

data class WarningRefreshResult(
    val locationId: String,
    val warnings: List<WarningEntity>,
    val source: WarningSource
) {
    init {
        require(locationId.isNotBlank()) { "locationId must not be empty" }
    }
}
