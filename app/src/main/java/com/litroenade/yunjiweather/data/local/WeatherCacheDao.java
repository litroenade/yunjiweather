package com.litroenade.yunjiweather.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.litroenade.yunjiweather.data.entity.WeatherCacheEntity;

@Dao
public interface WeatherCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WeatherCacheEntity entity);

    @Query("SELECT * FROM weather_cache WHERE locationId = :locationId AND weatherType = :weatherType LIMIT 1")
    WeatherCacheEntity findByLocationAndType(String locationId, String weatherType);

    @Query("SELECT MAX(updateTime) FROM weather_cache")
    Long findLatestUpdateTime();

    @Query("DELETE FROM weather_cache")
    void clearAll();

    @Query("SELECT COUNT(*) FROM weather_cache")
    int count();
}
