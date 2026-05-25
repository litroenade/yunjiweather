package com.litroenade.yunjiweather.data.local;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.litroenade.yunjiweather.data.entity.WeatherCacheEntity;
import com.litroenade.yunjiweather.data.model.LifeIndexItem;
import com.litroenade.yunjiweather.utils.DateTimeUtils;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class LifeIndexCacheGateway {

    public static final String WEATHER_TYPE_INDEX = "INDEX";

    private static final Type LIFE_INDEX_LIST_TYPE = new TypeToken<List<LifeIndexItem>>() {
    }.getType();

    private final WeatherCacheDao weatherCacheDao;
    private final Gson gson;

    public LifeIndexCacheGateway(WeatherCacheDao weatherCacheDao, Gson gson) {
        this.weatherCacheDao = weatherCacheDao;
        this.gson = gson;
    }

    public void save(String locationId, String cityName, List<LifeIndexItem> items, long updateTime, long expireTime) {
        Objects.requireNonNull(items, "items");
        WeatherCacheEntity entity = new WeatherCacheEntity(
                requireText(locationId, "locationId"),
                requireText(cityName, "cityName"),
                WEATHER_TYPE_INDEX,
                gson.toJson(items, LIFE_INDEX_LIST_TYPE),
                updateTime,
                expireTime
        );
        weatherCacheDao.insert(entity);
    }

    public CacheRecord readValid(String locationId, long nowTime) {
        WeatherCacheEntity entity = weatherCacheDao.findByLocationAndType(locationId, WEATHER_TYPE_INDEX);
        if (entity == null || DateTimeUtils.isCacheExpired(nowTime, entity.expireTime)) {
            return null;
        }
        try {
            List<LifeIndexItem> items = gson.fromJson(entity.weatherJson, LIFE_INDEX_LIST_TYPE);
            if (items == null || items.isEmpty()) {
                return null;
            }
            return new CacheRecord(Collections.unmodifiableList(items), entity.updateTime);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static String requireText(String value, String fieldName) {
        String text = Objects.requireNonNull(value, fieldName);
        if (text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }
        return text;
    }

    public static final class CacheRecord {
        private final List<LifeIndexItem> items;
        private final long updateTime;

        private CacheRecord(List<LifeIndexItem> items, long updateTime) {
            this.items = items;
            this.updateTime = updateTime;
        }

        public List<LifeIndexItem> getItems() {
            return items;
        }

        public long getUpdateTime() {
            return updateTime;
        }
    }
}
