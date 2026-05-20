package com.litroenade.yunjiweather.data.local;

import com.litroenade.yunjiweather.data.entity.WeatherCacheEntity;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UserScopedCacheTest {

    @Test
    public void cache_isolatedBetweenUsers() {
        FakeWeatherCacheDao dao = new FakeWeatherCacheDao();
        dao.insert(new WeatherCacheEntity(1L, "101010100", "北京", "HOME", "{\"temp\":20}", 100L, 200L));
        dao.insert(new WeatherCacheEntity(2L, "101010100", "北京", "HOME", "{\"temp\":30}", 100L, 200L));

        assertEquals("{\"temp\":20}", dao.findByLocationAndType(1L, "101010100", "HOME").weatherJson);
        assertEquals("{\"temp\":30}", dao.findByLocationAndType(2L, "101010100", "HOME").weatherJson);
        assertNull(dao.findByLocationAndType(3L, "101010100", "HOME"));
    }

    @Test
    public void sameUserSameLocationAndType_updatesExistingCache() {
        FakeWeatherCacheDao dao = new FakeWeatherCacheDao();
        dao.insert(new WeatherCacheEntity(1L, "101010100", "北京", "HOME", "{\"temp\":20}", 100L, 200L));
        dao.insert(new WeatherCacheEntity(1L, "101010100", "北京", "HOME", "{\"temp\":22}", 300L, 400L));

        WeatherCacheEntity result = dao.findByLocationAndType(1L, "101010100", "HOME");

        assertEquals("{\"temp\":22}", result.weatherJson);
        assertEquals(300L, result.updateTime);
    }

    private static final class FakeWeatherCacheDao implements WeatherCacheDao {
        private final List<WeatherCacheEntity> caches = new ArrayList<>();

        @Override
        public void insert(WeatherCacheEntity entity) {
            WeatherCacheEntity oldEntity = findByLocationAndType(entity.ownerUserId, entity.locationId, entity.weatherType);
            if (oldEntity != null) {
                caches.remove(oldEntity);
            }
            caches.add(entity);
        }

        @Override
        public WeatherCacheEntity findByLocationAndType(long ownerUserId, String locationId, String weatherType) {
            for (WeatherCacheEntity entity : caches) {
                if (entity.ownerUserId == ownerUserId
                        && entity.locationId.equals(locationId)
                        && entity.weatherType.equals(weatherType)) {
                    return entity;
                }
            }
            return null;
        }

        @Override
        public Long findLatestUpdateTime(long ownerUserId) {
            Long latest = null;
            for (WeatherCacheEntity entity : caches) {
                if (entity.ownerUserId == ownerUserId && (latest == null || entity.updateTime > latest)) {
                    latest = entity.updateTime;
                }
            }
            return latest;
        }

        @Override
        public int count(long ownerUserId) {
            int result = 0;
            for (WeatherCacheEntity entity : caches) {
                if (entity.ownerUserId == ownerUserId) {
                    result++;
                }
            }
            return result;
        }

        @Override
        public void clearAll(long ownerUserId) {
            caches.removeIf(entity -> entity.ownerUserId == ownerUserId);
        }
    }
}
