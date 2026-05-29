package com.litroenade.yunjiweather.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "weather_cache",
        indices = {
                @Index(value = {"locationId", "weatherType"}, unique = true)
        }
)
@SuppressWarnings("FieldMayBeFinal")
public class WeatherCacheEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

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
            @NonNull String locationId,
            @NonNull String cityName,
            @NonNull String weatherType,
            @NonNull String weatherJson,
            long updateTime,
            long expireTime
    ) {
        this.id = id;
        this.locationId = locationId;
        this.cityName = cityName;
        this.weatherType = weatherType;
        this.weatherJson = weatherJson;
        this.updateTime = updateTime;
        this.expireTime = expireTime;
    }

    @Ignore
    public WeatherCacheEntity(
            @NonNull String locationId,
            @NonNull String cityName,
            @NonNull String weatherType,
            @NonNull String weatherJson,
            long updateTime,
            long expireTime
    ) {
        this(0L, locationId, cityName, weatherType, weatherJson, updateTime, expireTime);
    }
}
