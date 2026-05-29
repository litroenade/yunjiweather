package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.data.local.LifeIndexCacheGateway;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.model.LifeIndexDefaults;
import com.litroenade.yunjiweather.data.model.LifeIndexItem;

import java.util.List;

/**
 * 生活建议的数据策略层。
 * 不再依赖付费指数接口：优先用缓存，其次用当前天气生成本地建议，最后才给通用建议。
 */
public final class LifeIndexRepository {

    private final LifeIndexStore store;

    public LifeIndexRepository(LifeIndexCacheGateway cacheGateway) {
        this((LifeIndexStore) cacheGateway);
    }

    LifeIndexRepository(LifeIndexStore store) {
        this.store = store;
    }

    public LoadResult load(String locationId, String cityName, long nowTime) {
        return load(locationId, cityName, null, nowTime);
    }

    public LoadResult load(String locationId, String cityName, HomeWeatherData weatherData, long nowTime) {
        LifeIndexStore.CacheRecord cacheRecord = store.readValid(locationId, nowTime);
        if (cacheRecord != null) {
            return LoadResult.cache(cacheRecord.getItems(), cacheRecord.getUpdateTime());
        }
        if (weatherData != null) {
            return LoadResult.localWeather(LifeIndexDefaults.createWeatherDrivenItems(weatherData));
        }
        return LoadResult.local(LifeIndexDefaults.createFallbackItems());
    }

    public enum LoadSource {
        CACHE,
        LOCAL_WEATHER,
        LOCAL
    }

    public static final class LoadResult {
        private final List<LifeIndexItem> items;
        private final LoadSource source;
        private final long cacheUpdateTime;

        private LoadResult(List<LifeIndexItem> items, LoadSource source, long cacheUpdateTime) {
            this.items = items;
            this.source = source;
            this.cacheUpdateTime = cacheUpdateTime;
        }

        private static LoadResult cache(List<LifeIndexItem> items, long cacheUpdateTime) {
            return new LoadResult(items, LoadSource.CACHE, cacheUpdateTime);
        }

        private static LoadResult local(List<LifeIndexItem> items) {
            return new LoadResult(items, LoadSource.LOCAL, 0L);
        }

        private static LoadResult localWeather(List<LifeIndexItem> items) {
            return new LoadResult(items, LoadSource.LOCAL_WEATHER, 0L);
        }

        public List<LifeIndexItem> getItems() {
            return items;
        }

        public LoadSource getSource() {
            return source;
        }

        public long getCacheUpdateTime() {
            return cacheUpdateTime;
        }
    }
}
