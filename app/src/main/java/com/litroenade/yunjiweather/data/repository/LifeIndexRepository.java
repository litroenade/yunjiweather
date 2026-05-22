package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.data.api.WeatherApiService;
import com.litroenade.yunjiweather.data.api.model.QWeatherIndicesResponse;
import com.litroenade.yunjiweather.data.local.LifeIndexCacheGateway;
import com.litroenade.yunjiweather.data.model.LifeIndexDefaults;
import com.litroenade.yunjiweather.data.model.LifeIndexItem;
import com.litroenade.yunjiweather.data.model.LifeIndexMapper;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;

public final class LifeIndexRepository {

    private static final String SUCCESS_CODE = "200";
    private static final String ALL_INDEX_TYPES = "0";
    private static final long INDEX_CACHE_TTL_MILLIS = 6L * 60L * 60L * 1000L;

    private final WeatherApiService apiService;
    private final LifeIndexCacheGateway cacheGateway;

    public LifeIndexRepository(WeatherApiService apiService, LifeIndexCacheGateway cacheGateway) {
        this.apiService = apiService;
        this.cacheGateway = cacheGateway;
    }

    public LoadResult load(String locationId, String cityName, long nowTime) {
        if (apiService == null) {
            LoadResult cacheResult = readCache(locationId, nowTime, LoadSource.CACHE_NO_API, null);
            return cacheResult == null
                    ? LoadResult.local(LifeIndexDefaults.createFallbackItems(), null)
                    : cacheResult;
        }
        try {
            List<LifeIndexItem> remoteItems = fetchRemoteItems(locationId);
            cacheGateway.save(locationId, cityName, remoteItems, nowTime, nowTime + INDEX_CACHE_TTL_MILLIS);
            return LoadResult.remote(remoteItems);
        } catch (IOException | RuntimeException exception) {
            LoadResult cacheResult = readCache(locationId, nowTime, LoadSource.CACHE_ERROR, exception.getMessage());
            return cacheResult == null
                    ? LoadResult.local(LifeIndexDefaults.createFallbackItems(), exception.getMessage())
                    : cacheResult;
        }
    }

    private LoadResult readCache(String locationId, long nowTime, LoadSource source, String errorMessage) {
        LifeIndexCacheGateway.CacheRecord cacheRecord = cacheGateway.readValid(locationId, nowTime);
        if (cacheRecord == null) {
            return null;
        }
        return LoadResult.cache(cacheRecord.getItems(), cacheRecord.getUpdateTime(), source, errorMessage);
    }

    private List<LifeIndexItem> fetchRemoteItems(String locationId) throws IOException {
        Response<QWeatherIndicesResponse> response = apiService.getLifeIndices(locationId, ALL_INDEX_TYPES, "zh").execute();
        QWeatherIndicesResponse body = response.body();
        if (!response.isSuccessful() || body == null || !SUCCESS_CODE.equals(body.code)) {
            throw new IOException("生活指数接口请求失败");
        }
        return LifeIndexDefaults.completeWithFallbacks(LifeIndexMapper.mapDailyIndices(body.daily));
    }

    public enum LoadSource {
        REMOTE,
        CACHE_NO_API,
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
