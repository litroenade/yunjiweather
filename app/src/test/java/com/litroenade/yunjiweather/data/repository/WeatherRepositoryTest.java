package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WeatherRepositoryTest {

    @Test
    public void loadHomeWeather_returnsSuccessAndSavesCacheWhenRemoteSucceeds() {
        FakeRemoteGateway remoteGateway = new FakeRemoteGateway(homeWeather("北京", "24", "多云"));
        FakeCacheGateway cacheGateway = new FakeCacheGateway(null);
        WeatherRepository repository = new WeatherRepository(remoteGateway, cacheGateway, () -> 1_000L);

        UiState<HomeWeatherData> result = repository.loadHomeWeather("101010100", "北京", 39.9042, 116.4074);

        assertEquals(UiState.Status.SUCCESS, result.getStatus());
        assertEquals("北京", result.getData().getCityName());
        assertEquals("24", result.getData().getTemperature());
        assertEquals("18", result.getData().getWindSpeed());
        assertEquals("55", result.getData().getAirQualityIndex());
        assertEquals("良", result.getData().getAirQualityCategory());
        assertEquals("PM2.5", result.getData().getPrimaryPollutant());
        assertEquals(39.9042, remoteGateway.latitude, 0.0001);
        assertEquals(116.4074, remoteGateway.longitude, 0.0001);
        assertTrue(cacheGateway.saved);
        assertEquals(1_000L, cacheGateway.savedUpdateTime);
        assertEquals(1_801_000L, cacheGateway.savedExpireTime);
    }

    @Test
    public void loadHomeWeather_returnsCacheWhenRemoteFailsAndCacheExists() {
        FakeRemoteGateway remoteGateway = new FakeRemoteGateway(new IOException("网络连接失败"));
        FakeCacheGateway cacheGateway = new FakeCacheGateway(
                new WeatherRepository.CacheRecord<>(homeWeather("北京", "19", "晴"), 500L, 1_800L)
        );
        WeatherRepository repository = new WeatherRepository(remoteGateway, cacheGateway, () -> 1_000L);

        UiState<HomeWeatherData> result = repository.loadHomeWeather("101010100", "北京", 39.9042, 116.4074);

        assertEquals(UiState.Status.CACHE, result.getStatus());
        assertEquals("北京", result.getData().getCityName());
        assertEquals("19", result.getData().getTemperature());
        assertEquals("网络连接失败，已显示本地缓存。", result.getMessage());
        assertEquals(500L, result.getUpdateTime());
    }

    @Test
    public void loadHomeWeather_marksCacheExpiredWhenRemoteFailsAndCacheIsExpired() {
        FakeRemoteGateway remoteGateway = new FakeRemoteGateway(new IOException("网络连接失败"));
        FakeCacheGateway cacheGateway = new FakeCacheGateway(
                new WeatherRepository.CacheRecord<>(homeWeather("北京", "18", "阴"), 400L, 900L)
        );
        WeatherRepository repository = new WeatherRepository(remoteGateway, cacheGateway, () -> 1_000L);

        UiState<HomeWeatherData> result = repository.loadHomeWeather("101010100", "北京", 39.9042, 116.4074);

        assertEquals(UiState.Status.CACHE, result.getStatus());
        assertEquals("18", result.getData().getTemperature());
        assertEquals("网络连接失败，当前缓存已过期，仅供参考。", result.getMessage());
        assertEquals(400L, result.getUpdateTime());
    }

    @Test
    public void loadHomeWeather_returnsErrorWhenRemoteFailsAndCacheMissing() {
        FakeRemoteGateway remoteGateway = new FakeRemoteGateway(new IOException("401"));
        FakeCacheGateway cacheGateway = new FakeCacheGateway(null);
        WeatherRepository repository = new WeatherRepository(remoteGateway, cacheGateway, () -> 1_000L);

        UiState<HomeWeatherData> result = repository.loadHomeWeather("101010100", "北京", 39.9042, 116.4074);

        assertEquals(UiState.Status.ERROR, result.getStatus());
        assertEquals("天气请求失败，请检查网络连接或天气服务配置。", result.getMessage());
        assertFalse(cacheGateway.saved);
    }

    private static HomeWeatherData homeWeather(String cityName, String temperature, String condition) {
        return new HomeWeatherData(
                cityName,
                "101010100",
                temperature,
                condition,
                "26",
                "30",
                "20",
                "72",
                "东南风",
                "1",
                "18",
                "1003",
                "16",
                "101",
                1_700_000_000_000L,
                WeatherAdviceUtilsShim.clothing(temperature),
                "适合通勤出行，注意查看实时天气变化。",
                "55",
                "良",
                "PM2.5"
        );
    }

    private static final class FakeRemoteGateway implements WeatherRepository.RemoteGateway {
        private final HomeWeatherData data;
        private final IOException exception;
        private double latitude;
        private double longitude;

        private FakeRemoteGateway(HomeWeatherData data) {
            this.data = data;
            this.exception = null;
        }

        private FakeRemoteGateway(IOException exception) {
            this.data = null;
            this.exception = exception;
        }

        @Override
        public HomeWeatherData fetchHomeWeather(String locationId, String cityName, double latitude, double longitude) throws IOException {
            this.latitude = latitude;
            this.longitude = longitude;
            if (exception != null) {
                throw exception;
            }
            return data;
        }
    }

    private static final class FakeCacheGateway implements WeatherRepository.CacheGateway {
        private final WeatherRepository.CacheRecord<HomeWeatherData> cacheRecord;
        private boolean saved;
        private long savedUpdateTime;
        private long savedExpireTime;

        private FakeCacheGateway(WeatherRepository.CacheRecord<HomeWeatherData> cacheRecord) {
            this.cacheRecord = cacheRecord;
        }

        @Override
        public void saveHomeWeather(String locationId, HomeWeatherData data, long updateTime, long expireTime) {
            this.saved = true;
            this.savedUpdateTime = updateTime;
            this.savedExpireTime = expireTime;
        }

        @Override
        public WeatherRepository.CacheRecord<HomeWeatherData> readHomeWeather(String locationId) {
            return cacheRecord;
        }
    }

    private static final class WeatherAdviceUtilsShim {
        private static String clothing(String temperature) {
            return Integer.parseInt(temperature) >= 24
                    ? "建议穿短袖或薄外套，早晚温差明显时注意加衣。"
                    : "建议穿长袖或薄外套，体感偏凉时及时加衣。";
        }
    }
}
