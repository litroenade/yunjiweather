package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.utils.DateTimeUtils;

import java.io.IOException;

/**
 * Owns the home-weather loading policy: remote refresh first, then Room cache fallback
 * with explicit cache messaging when the network or provider fails.
 */
public class WeatherRepository implements HomeWeatherSource, HomeWeatherCacheSource {

    private static final long HOME_CACHE_TTL_MILLIS = 30L * 60L * 1000L;

    private final RemoteGateway remoteGateway;
    private final CacheGateway cacheGateway;
    private final Clock clock;

    public WeatherRepository(RemoteGateway remoteGateway, CacheGateway cacheGateway, Clock clock) {
        this.remoteGateway = remoteGateway;
        this.cacheGateway = cacheGateway;
        this.clock = clock;
    }

    @Override
    public UiState<HomeWeatherData> loadHomeWeather(String locationId, String cityName, double latitude, double longitude) {
        long nowTime = clock.now();
        try {
            HomeWeatherData data = remoteGateway.fetchHomeWeather(locationId, cityName, latitude, longitude);
            if (data == null) {
                throw new IOException("empty weather response");
            }
            cacheGateway.saveHomeWeather(locationId, data, nowTime, nowTime + HOME_CACHE_TTL_MILLIS);
            return UiState.success(data);
        } catch (IOException exception) {
            CacheRecord<HomeWeatherData> cacheRecord = cacheGateway.readHomeWeather(locationId);
            if (cacheRecord != null && cacheRecord.getData() != null) {
                return UiState.cache(
                        cacheRecord.getData(),
                        createCacheFallbackMessage(nowTime, cacheRecord),
                        cacheRecord.getUpdateTime()
                );
            }
            return UiState.error("天气请求失败，请检查网络连接或天气服务配置。");
        }
    }

    @Override
    public UiState<HomeWeatherData> loadCachedHomeWeather(String locationId) {
        long nowTime = clock.now();
        CacheRecord<HomeWeatherData> cacheRecord = cacheGateway.readHomeWeather(locationId);
        if (cacheRecord == null || cacheRecord.getData() == null) {
            return null;
        }
        return UiState.cache(
                cacheRecord.getData(),
                createCacheRefreshingMessage(nowTime, cacheRecord),
                cacheRecord.getUpdateTime()
        );
    }

    public UiState<HomeWeatherData> loadHomeWeatherPreferCache(
            String locationId,
            String cityName,
            double latitude,
            double longitude
    ) {
        long nowTime = clock.now();
        CacheRecord<HomeWeatherData> cacheRecord = cacheGateway.readHomeWeather(locationId);
        if (isFreshCache(nowTime, cacheRecord)) {
            return createCacheState(nowTime, cacheRecord);
        }
        return loadHomeWeather(locationId, cityName, latitude, longitude);
    }

    public boolean hasFreshHomeWeatherCache(String locationId) {
        return isFreshCache(clock.now(), cacheGateway.readHomeWeather(locationId));
    }

    private String createCacheFallbackMessage(long nowTime, CacheRecord<HomeWeatherData> cacheRecord) {
        if (DateTimeUtils.isCacheExpired(nowTime, cacheRecord.getExpireTime())) {
            return "网络连接失败，当前缓存已过期，仅供参考。";
        }
        return "网络连接失败，已显示本地缓存。";
    }

    private String createCacheRefreshingMessage(long nowTime, CacheRecord<HomeWeatherData> cacheRecord) {
        if (DateTimeUtils.isCacheExpired(nowTime, cacheRecord.getExpireTime())) {
            return "已显示过期本地缓存，正在刷新天气。";
        }
        return "已显示本地缓存，正在刷新天气。";
    }

    private boolean isFreshCache(long nowTime, CacheRecord<HomeWeatherData> cacheRecord) {
        return cacheRecord != null
                && cacheRecord.getData() != null
                && !DateTimeUtils.isCacheExpired(nowTime, cacheRecord.getExpireTime());
    }

    private UiState<HomeWeatherData> createCacheState(long nowTime, CacheRecord<HomeWeatherData> cacheRecord) {
        return UiState.cache(
                cacheRecord.getData(),
                createCacheRefreshingMessage(nowTime, cacheRecord),
                cacheRecord.getUpdateTime()
        );
    }

    public interface RemoteGateway {
        HomeWeatherData fetchHomeWeather(String locationId, String cityName, double latitude, double longitude) throws IOException;
    }

    public interface CacheGateway {
        void saveHomeWeather(String locationId, HomeWeatherData data, long updateTime, long expireTime);

        CacheRecord<HomeWeatherData> readHomeWeather(String locationId);
    }

    public interface Clock {
        long now();
    }

    public static final class CacheRecord<T> {
        private final T data;
        private final long updateTime;
        private final long expireTime;

        public CacheRecord(T data, long updateTime, long expireTime) {
            this.data = data;
            this.updateTime = updateTime;
            this.expireTime = expireTime;
        }

        public T getData() {
            return data;
        }

        public long getUpdateTime() {
            return updateTime;
        }

        public long getExpireTime() {
            return expireTime;
        }
    }
}
