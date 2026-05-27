package com.litroenade.yunjiweather.data.repository

import com.google.gson.Gson
import com.litroenade.yunjiweather.data.api.WeatherApiService
import com.litroenade.yunjiweather.data.api.WeatherGatewayFactory
import com.litroenade.yunjiweather.data.local.AppDatabase
import com.litroenade.yunjiweather.data.local.RoomWeatherCacheGateway

object WeatherRepositoryFactory {

    @JvmStatic
    fun createHomeRepository(
        database: AppDatabase,
        apiService: WeatherApiService?
    ): WeatherRepository {
        return WeatherRepository(
            WeatherGatewayFactory.createHomeRemoteGateway(apiService),
            RoomWeatherCacheGateway(database.weatherCacheDao(), Gson()),
            System::currentTimeMillis
        )
    }
}
