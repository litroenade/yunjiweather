package com.litroenade.yunjiweather.data.repository

import java.io.IOException

interface AlertRemoteGateway {
    @Throws(IOException::class)
    fun refresh(locationId: String): WarningRefreshResult
}
