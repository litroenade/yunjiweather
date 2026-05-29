package com.litroenade.yunjiweather.widget

import com.litroenade.yunjiweather.data.local.CityDao
import com.litroenade.yunjiweather.data.local.WeatherCacheDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WeatherWidgetEntryPoint {
    fun cityDao(): CityDao

    fun weatherCacheDao(): WeatherCacheDao
}
