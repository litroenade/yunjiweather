package com.litroenade.yunjiweather.data.repository

import com.litroenade.yunjiweather.data.api.WeatherApiService
import com.litroenade.yunjiweather.data.model.LifeIndexDefaults
import com.litroenade.yunjiweather.data.model.LifeIndexItem
import com.litroenade.yunjiweather.data.model.LifeIndexMapper
import java.io.IOException

class QWeatherLifeIndexRemoteGateway(
    private val apiService: WeatherApiService
) : LifeIndexRemoteGateway {

    @Throws(IOException::class)
    override fun fetch(locationId: String): List<LifeIndexItem> {
        val response = apiService.getLifeIndices(locationId, ALL_INDEX_TYPES, "zh").execute()
        val body = response.body()
        if (!response.isSuccessful || body == null || body.code != SUCCESS_CODE) {
            throw IOException("生活指数接口请求失败")
        }
        return LifeIndexDefaults.completeWithFallbacks(LifeIndexMapper.mapDailyIndices(body.daily))
    }

    private companion object {
        private const val SUCCESS_CODE = "200"
        private const val ALL_INDEX_TYPES = "0"
    }
}
