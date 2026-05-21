package com.litroenade.yunjiweather.data.repository;

import com.google.gson.Gson;
import com.litroenade.yunjiweather.data.api.WeatherApiService;
import com.litroenade.yunjiweather.data.api.WeatherGatewayFactory;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.data.local.RoomWeatherCacheGateway;

public final class WeatherRepositoryFactory {

    private WeatherRepositoryFactory() {
    }

    public static WeatherRepository createHomeRepository(
            long ownerUserId,
            AppDatabase database,
            WeatherApiService apiService
    ) {
        return new WeatherRepository(
                WeatherGatewayFactory.createHomeRemoteGateway(apiService),
                new RoomWeatherCacheGateway(ownerUserId, database.weatherCacheDao(), new Gson()),
                System::currentTimeMillis
        );
    }
}
