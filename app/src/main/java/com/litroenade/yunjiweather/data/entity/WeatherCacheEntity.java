package com.litroenade.yunjiweather.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "weather_cache",
        indices = {
                @Index(value = {"ownerUserId", "locationId", "weatherType"}, unique = true)
        }
)
public class WeatherCacheEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long ownerUserId;

    @NonNull
    public String locationId;

    @NonNull
    public String cityName;

    @NonNull
    public String weatherType;

    @NonNull
    public String weatherJson;

    public long updateTime;

    public long expireTime;

    public WeatherCacheEntity(
            long id,
            long ownerUserId,
            @NonNull String locationId,
            @NonNull String cityName,
            @NonNull String weatherType,
            @NonNull String weatherJson,
            long updateTime,
            long expireTime
    ) {
        this.id = id;
        this.ownerUserId = ownerUserId;
        this.locationId = locationId;
        this.cityName = cityName;
        this.weatherType = weatherType;
        this.weatherJson = weatherJson;
        this.updateTime = updateTime;
        this.expireTime = expireTime;
    }

    @Ignore
    public WeatherCacheEntity(
            long ownerUserId,
            @NonNull String locationId,
            @NonNull String cityName,
            @NonNull String weatherType,
            @NonNull String weatherJson,
            long updateTime,
            long expireTime
    ) {
        this(0L, ownerUserId, locationId, cityName, weatherType, weatherJson, updateTime, expireTime);
    }
}
