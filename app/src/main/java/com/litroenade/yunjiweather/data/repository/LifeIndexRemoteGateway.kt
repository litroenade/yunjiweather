package com.litroenade.yunjiweather.data.repository

import com.litroenade.yunjiweather.data.model.LifeIndexItem
import java.io.IOException

interface LifeIndexRemoteGateway {
    @Throws(IOException::class)
    fun fetch(locationId: String): List<LifeIndexItem>
}
