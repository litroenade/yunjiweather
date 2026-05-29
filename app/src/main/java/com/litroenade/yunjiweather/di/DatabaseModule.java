package com.litroenade.yunjiweather.di;

import android.content.Context;

import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.data.local.CityDao;
import com.litroenade.yunjiweather.data.local.WarningDao;
import com.litroenade.yunjiweather.data.local.WeatherCacheDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public final class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return AppDatabase.getInstance(context);
    }

    @Provides
    public CityDao provideCityDao(AppDatabase database) {
        return database.cityDao();
    }

    @Provides
    public WeatherCacheDao provideWeatherCacheDao(AppDatabase database) {
        return database.weatherCacheDao();
    }

    @Provides
    public WarningDao provideWarningDao(AppDatabase database) {
        return database.warningDao();
    }
}
