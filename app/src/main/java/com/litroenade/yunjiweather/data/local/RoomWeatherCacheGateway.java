package com.litroenade.yunjiweather.data.local;

import com.google.gson.Gson;
import com.litroenade.yunjiweather.data.entity.WeatherCacheEntity;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.repository.WeatherRepository;

public final class RoomWeatherCacheGateway implements WeatherRepository.CacheGateway {

    private final WeatherCacheDao weatherCacheDao;
    private final Gson gson;

    public RoomWeatherCacheGateway(WeatherCacheDao weatherCacheDao, Gson gson) {
        this.weatherCacheDao = weatherCacheDao;
        this.gson = gson;
    }

    @Override
    public void saveHomeWeather(String locationId, HomeWeatherData data, long updateTime, long expireTime) {
        WeatherCacheEntity entity = new WeatherCacheEntity(
                locationId,
                data.getCityName(),
                WeatherCacheTypes.HOME,
                gson.toJson(data),
                updateTime,
                expireTime
        );
        weatherCacheDao.insert(entity);
    }

    @Override
    public WeatherRepository.CacheRecord<HomeWeatherData> readHomeWeather(String locationId) {
        WeatherCacheEntity entity = weatherCacheDao.findByLocationAndType(locationId, WeatherCacheTypes.HOME);
        if (entity == null) {
            return null;
        }
        try {
            HomeWeatherData data = gson.fromJson(entity.weatherJson, HomeWeatherData.class);
            data.validateForDisplay();
            return new WeatherRepository.CacheRecord<>(data, entity.updateTime, entity.expireTime);
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
