package com.litroenade.yunjiweather.domain.usecase;

import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.repository.WeatherRepository;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RefreshAllCityWeatherCacheUseCaseTest {

    @Test
    public void execute_refreshesOnlyNonVisibleCitiesWithoutFreshCache() {
        FakeWeatherRepository weatherRepository = new FakeWeatherRepository();
        weatherRepository.freshLocationId = "fresh";
        RefreshAllCityWeatherCacheUseCase useCase = new RefreshAllCityWeatherCacheUseCase(weatherRepository);
        List<CityEntity> cities = Arrays.asList(
                city("Visible", "visible"),
                city("Fresh", "fresh"),
                city("Stale", "stale")
        );

        RefreshAllCityWeatherCacheUseCase.Result result = useCase.execute(cities, "visible");

        assertEquals(1, weatherRepository.loadCount);
        assertEquals("stale", weatherRepository.loadedLocationId);
        assertEquals(1, result.getRefreshedCount());
        assertEquals(0, result.getFailedMessages().size());
    }

    private static CityEntity city(String name, String locationId) {
        return new CityEntity(name, locationId, name, "China", 1.0, 2.0, false, 0, 0L, 0L);
    }

    private static final class FakeWeatherRepository extends WeatherRepository {
        private String freshLocationId;
        private String loadedLocationId;
        private int loadCount;

        private FakeWeatherRepository() {
            super(
                    (locationId, cityName, latitude, longitude) -> null,
                    new WeatherRepository.CacheGateway() {
                        @Override
                        public void saveHomeWeather(String locationId, HomeWeatherData data, long updateTime, long expireTime) {
                        }

                        @Override
                        public WeatherRepository.CacheRecord<HomeWeatherData> readHomeWeather(String locationId) {
                            return null;
                        }
                    },
                    () -> 0L
            );
        }

        @Override
        public boolean hasFreshHomeWeatherCache(String locationId) {
            return locationId.equals(freshLocationId);
        }

        @Override
        public UiState<HomeWeatherData> loadHomeWeather(String locationId, String cityName, double latitude, double longitude) {
            loadCount++;
            loadedLocationId = locationId;
            return UiState.success(null);
        }
    }
}
