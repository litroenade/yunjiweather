package com.litroenade.yunjiweather.di;

import com.litroenade.yunjiweather.data.api.ApiClient;
import com.litroenade.yunjiweather.data.api.OpenMeteoApiService;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public final class NetworkModule {

    @Provides
    @Singleton
    @Named("openMeteoForecast")
    public OpenMeteoApiService provideOpenMeteoForecastService() {
        return ApiClient.createOpenMeteoForecastService();
    }

    @Provides
    @Singleton
    @Named("openMeteoAirQuality")
    public OpenMeteoApiService provideOpenMeteoAirQualityService() {
        return ApiClient.createOpenMeteoAirQualityService();
    }

    @Provides
    @Singleton
    @Named("openMeteoGeocoding")
    public OpenMeteoApiService provideOpenMeteoGeocodingService() {
        return ApiClient.createOpenMeteoGeocodingService();
    }
}
