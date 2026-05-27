package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.data.api.WeatherApiService;
import com.litroenade.yunjiweather.data.local.LifeIndexCacheGateway;
import com.litroenade.yunjiweather.data.model.LifeIndexDefaults;
import com.litroenade.yunjiweather.data.model.LifeIndexItem;

import java.io.IOException;
import java.util.List;

public final class LifeIndexRepository {

    private static final String OPEN_METEO_LOCATION_PREFIX = "openmeteo:";
    private static final long INDEX_CACHE_TTL_MILLIS = 6L * 60L * 60L * 1000L;

    private final LifeIndexRemoteGateway remoteGateway;
    private final LifeIndexStore store;

    public LifeIndexRepository(WeatherApiService apiService, LifeIndexCacheGateway cacheGateway) {
        this(apiService == null ? null : new QWeatherLifeIndexRemoteGateway(apiService), cacheGateway);
    }

    LifeIndexRepository(LifeIndexRemoteGateway remoteGateway, LifeIndexStore store) {
        this.remoteGateway = remoteGateway;
        this.store = store;
    }

    public LoadResult load(String locationId, String cityName, long nowTime) {
        if (!canFetchRemoteIndices(remoteGateway != null, locationId)) {
            LoadSource source = remoteGateway == null ? LoadSource.CACHE_NO_API : LoadSource.CACHE_UNSUPPORTED_LOCATION;
            String errorMessage = remoteGateway == null ? null : "当前城市没有 QWeather 城市 ID";
            LoadResult cacheResult = readCache(locationId, nowTime, source, errorMessage);
            return cacheResult == null
                    ? LoadResult.local(LifeIndexDefaults.createFallbackItems(), errorMessage)
                    : cacheResult;
        }
        try {
            List<LifeIndexItem> remoteItems = remoteGateway.fetch(locationId);
            List<LifeIndexItem> completedItems = LifeIndexDefaults.completeWithFallbacks(remoteItems);
            store.save(locationId, cityName, completedItems, nowTime, nowTime + INDEX_CACHE_TTL_MILLIS);
            return LoadResult.remote(completedItems);
        } catch (IOException | RuntimeException exception) {
            LoadResult cacheResult = readCache(locationId, nowTime, LoadSource.CACHE_ERROR, exception.getMessage());
            return cacheResult == null
                    ? LoadResult.local(LifeIndexDefaults.createFallbackItems(), exception.getMessage())
                    : cacheResult;
        }
    }

    public static boolean canFetchRemoteIndices(boolean qWeatherConfigured, String locationId) {
        if (!qWeatherConfigured) {
            return false;
        }
        if (locationId == null || locationId.trim().isEmpty()) {
            throw new IllegalArgumentException("locationId must not be empty");
        }
        return !locationId.startsWith(OPEN_METEO_LOCATION_PREFIX);
    }

    private LoadResult readCache(String locationId, long nowTime, LoadSource source, String errorMessage) {
        LifeIndexStore.CacheRecord cacheRecord = store.readValid(locationId, nowTime);
        if (cacheRecord == null) {
            return null;
        }
        return LoadResult.cache(cacheRecord.getItems(), cacheRecord.getUpdateTime(), source, errorMessage);
    }

    public enum LoadSource {
        REMOTE,
        CACHE_NO_API,
        CACHE_UNSUPPORTED_LOCATION,
        CACHE_ERROR,
        LOCAL
    }

    public static final class LoadResult {
        private final List<LifeIndexItem> items;
        private final LoadSource source;
        private final long cacheUpdateTime;
        private final String errorMessage;

        private LoadResult(List<LifeIndexItem> items, LoadSource source, long cacheUpdateTime, String errorMessage) {
            this.items = items;
            this.source = source;
            this.cacheUpdateTime = cacheUpdateTime;
            this.errorMessage = errorMessage;
        }

        private static LoadResult remote(List<LifeIndexItem> items) {
            return new LoadResult(items, LoadSource.REMOTE, 0L, null);
        }

        private static LoadResult cache(
                List<LifeIndexItem> items,
                long cacheUpdateTime,
                LoadSource source,
                String errorMessage
        ) {
            return new LoadResult(items, source, cacheUpdateTime, errorMessage);
        }

        private static LoadResult local(List<LifeIndexItem> items, String errorMessage) {
            return new LoadResult(items, LoadSource.LOCAL, 0L, errorMessage);
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

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
