package com.litroenade.yunjiweather.di;

import android.content.Context;

import com.google.gson.Gson;
import com.litroenade.yunjiweather.data.api.CityLookupGateway;
import com.litroenade.yunjiweather.data.api.OpenMeteoApiService;
import com.litroenade.yunjiweather.data.api.OpenMeteoCitySearchGateway;
import com.litroenade.yunjiweather.data.api.OpenMeteoRemoteGateway;
import com.litroenade.yunjiweather.data.local.CityDao;
import com.litroenade.yunjiweather.data.local.LifeIndexCacheGateway;
import com.litroenade.yunjiweather.data.local.RoomWeatherCacheGateway;
import com.litroenade.yunjiweather.data.local.WarningDao;
import com.litroenade.yunjiweather.data.local.WeatherCacheDao;
import com.litroenade.yunjiweather.data.local.prefs.SettingsPreferencesDataSource;
import com.litroenade.yunjiweather.data.repository.AlertRepository;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.CityWeatherSummaryRepository;
import com.litroenade.yunjiweather.data.repository.LifeIndexRepository;
import com.litroenade.yunjiweather.data.repository.SettingsRepository;
import com.litroenade.yunjiweather.data.repository.WeatherRepository;
import com.litroenade.yunjiweather.notification.NotificationCandidateSelector;
import com.litroenade.yunjiweather.notification.WarningNotificationDispatcher;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public final class RepositoryModule {

    @Provides
    @Singleton
    public Gson provideGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    public SettingsRepository provideSettingsRepository(@ApplicationContext Context context) {
        return new SettingsPreferencesDataSource(context);
    }

    @Provides
    @Singleton
    public CityRepository provideCityRepository(CityDao cityDao) {
        return new CityRepository(cityDao);
    }

    @Provides
    @Singleton
    public AlertRepository provideAlertRepository(WarningDao warningDao) {
        return new AlertRepository(warningDao);
    }

    @Provides
    @Singleton
    public LifeIndexRepository provideLifeIndexRepository(WeatherCacheDao weatherCacheDao, Gson gson) {
        return new LifeIndexRepository(new LifeIndexCacheGateway(weatherCacheDao, gson));
    }

    @Provides
    @Singleton
    public OpenMeteoCitySearchGateway provideOpenMeteoCitySearchGateway(
            @Named("openMeteoGeocoding") OpenMeteoApiService geocodingService
    ) {
        return new OpenMeteoCitySearchGateway(geocodingService);
    }

    @Provides
    @Singleton
    public CityLookupGateway provideCityLookupGateway(OpenMeteoCitySearchGateway citySearchGateway) {
        return new CityLookupGateway(citySearchGateway);
    }

    @Provides
    @Singleton
    public WeatherRepository.RemoteGateway provideWeatherRemoteGateway(
            @Named("openMeteoForecast") OpenMeteoApiService forecastService,
            @Named("openMeteoAirQuality") OpenMeteoApiService airQualityService
    ) {
        return new OpenMeteoRemoteGateway(forecastService, airQualityService);
    }

    @Provides
    @Singleton
    public WeatherRepository.CacheGateway provideWeatherCacheGateway(WeatherCacheDao weatherCacheDao, Gson gson) {
        return new RoomWeatherCacheGateway(weatherCacheDao, gson);
    }

    @Provides
    @Singleton
    public WeatherRepository provideWeatherRepository(
            WeatherRepository.RemoteGateway remoteGateway,
            WeatherRepository.CacheGateway cacheGateway
    ) {
        return new WeatherRepository(remoteGateway, cacheGateway, System::currentTimeMillis);
    }

    @Provides
    @Singleton
    public CityWeatherSummaryRepository provideCityWeatherSummaryRepository(WeatherRepository weatherRepository) {
        return new CityWeatherSummaryRepository(weatherRepository, weatherRepository, false);
    }

    @Provides
    @Singleton
    public WarningNotificationDispatcher provideWarningNotificationDispatcher() {
        return new WarningNotificationDispatcher(new NotificationCandidateSelector());
    }
}
