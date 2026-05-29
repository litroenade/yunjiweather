package com.litroenade.yunjiweather.data.repository;

import static org.junit.Assert.assertEquals;

import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.model.LifeIndexItem;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LifeIndexRepositoryTest {

    @Test
    public void loadReturnsValidCacheBeforeLocalFallback() {
        FakeStore store = new FakeStore();
        store.cacheRecord = new LifeIndexStore.CacheRecord(
                Arrays.asList(new LifeIndexItem("出行", "适宜", "适合出行。", "适合出行。")),
                800L
        );
        LifeIndexRepository repository = new LifeIndexRepository(store);

        LifeIndexRepository.LoadResult result = repository.load("openmeteo:1816670", "北京", 1_000L);

        assertEquals(LifeIndexRepository.LoadSource.CACHE, result.getSource());
        assertEquals("出行", result.getItems().get(0).getName());
        assertEquals(800L, result.getCacheUpdateTime());
    }

    @Test
    public void loadReturnsLocalFallbackWhenCacheIsMissingAndWeatherIsMissing() {
        LifeIndexRepository repository = new LifeIndexRepository(new FakeStore());

        LifeIndexRepository.LoadResult result = repository.load("openmeteo:1816670", "北京", 1_000L);

        assertEquals(LifeIndexRepository.LoadSource.LOCAL, result.getSource());
        assertEquals(10, result.getItems().size());
    }

    @Test
    public void loadReturnsWeatherDrivenLocalSuggestionsWhenCurrentWeatherIsAvailable() {
        LifeIndexRepository repository = new LifeIndexRepository(new FakeStore());

        LifeIndexRepository.LoadResult result = repository.load(
                "openmeteo:1816670",
                "Beijing",
                createWeather("33", "晴", "2", "55", "8"),
                1_000L
        );

        assertEquals(LifeIndexRepository.LoadSource.LOCAL_WEATHER, result.getSource());
        assertEquals(10, result.getItems().size());
        assertEquals("穿衣", result.getItems().get(0).getName());
    }

    private HomeWeatherData createWeather(
            String temperature,
            String condition,
            String windScale,
            String airQualityIndex,
            String uvIndex
    ) {
        return new HomeWeatherData(
                "Beijing",
                "openmeteo:1816670",
                temperature,
                condition,
                temperature,
                "35",
                "24",
                "62",
                "西南风",
                windScale,
                "10",
                "1008",
                "10",
                "100",
                1_000L,
                "cached clothing",
                "cached travel",
                airQualityIndex,
                "良",
                "PM2.5",
                uvIndex,
                "05:20",
                "19:12",
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    private static final class FakeStore implements LifeIndexStore {
        private LifeIndexStore.CacheRecord cacheRecord;

        @Override
        public void save(String locationId, String cityName, List<LifeIndexItem> items, long updateTime, long expireTime) {
            throw new UnsupportedOperationException();
        }

        @Override
        public LifeIndexStore.CacheRecord readValid(String locationId, long nowTime) {
            return cacheRecord;
        }
    }
}
