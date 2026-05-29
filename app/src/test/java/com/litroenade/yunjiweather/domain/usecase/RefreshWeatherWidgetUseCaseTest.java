package com.litroenade.yunjiweather.domain.usecase;

import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.local.CityDao;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.WeatherRepository;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RefreshWeatherWidgetUseCaseTest {

    @Test
    public void executeSkipsNetworkWhenDefaultCityCacheIsFresh() {
        FakeWeatherRepository weatherRepository = new FakeWeatherRepository();
        weatherRepository.fresh = true;
        RefreshWeatherWidgetUseCase useCase = new RefreshWeatherWidgetUseCase(
                new CityRepository(new FakeCityDao(city())),
                weatherRepository,
                () -> 1716600000000L
        );

        RefreshWeatherWidgetUseCase.Result result = useCase.execute(false);

        assertEquals(RefreshWeatherWidgetUseCase.Status.SKIPPED_FRESH_CACHE, result.getStatus());
        assertEquals(0, weatherRepository.loadCount);
    }

    @Test
    public void executeRefreshesDefaultCityWhenCacheIsExpired() {
        FakeWeatherRepository weatherRepository = new FakeWeatherRepository();
        weatherRepository.fresh = false;
        RefreshWeatherWidgetUseCase useCase = new RefreshWeatherWidgetUseCase(
                new CityRepository(new FakeCityDao(city())),
                weatherRepository,
                () -> 1716600000000L
        );

        RefreshWeatherWidgetUseCase.Result result = useCase.execute(false);

        assertEquals(RefreshWeatherWidgetUseCase.Status.REFRESHED, result.getStatus());
        assertEquals(1, weatherRepository.loadCount);
        assertEquals("101010100", weatherRepository.loadedLocationId);
    }

    @Test
    public void executeForceRefreshBypassesFreshCache() {
        FakeWeatherRepository weatherRepository = new FakeWeatherRepository();
        weatherRepository.fresh = true;
        RefreshWeatherWidgetUseCase useCase = new RefreshWeatherWidgetUseCase(
                new CityRepository(new FakeCityDao(city())),
                weatherRepository,
                () -> 1716600000000L
        );

        RefreshWeatherWidgetUseCase.Result result = useCase.execute(true);

        assertEquals(RefreshWeatherWidgetUseCase.Status.REFRESHED, result.getStatus());
        assertEquals(1, weatherRepository.loadCount);
    }

    private static CityEntity city() {
        return new CityEntity("Beijing", "101010100", "Beijing", "China", 39.9042, 116.4074, true, 0, 1L, 1L);
    }

    private static final class FakeWeatherRepository extends WeatherRepository {
        private boolean fresh;
        private int loadCount;
        private String loadedLocationId;

        private FakeWeatherRepository() {
            super(
                    (locationId, cityName, latitude, longitude) -> homeWeather(cityName, locationId),
                    new CacheGateway() {
                        @Override
                        public void saveHomeWeather(String locationId, HomeWeatherData data, long updateTime, long expireTime) {
                        }

                        @Override
                        public CacheRecord<HomeWeatherData> readHomeWeather(String locationId) {
                            return null;
                        }
                    },
                    () -> 0L
            );
        }

        @Override
        public boolean hasFreshHomeWeatherCache(String locationId) {
            return fresh;
        }

        @Override
        public UiState<HomeWeatherData> loadHomeWeather(String locationId, String cityName, double latitude, double longitude) {
            loadCount++;
            loadedLocationId = locationId;
            return UiState.success(homeWeather(cityName, locationId));
        }
    }

    private static HomeWeatherData homeWeather(String cityName, String locationId) {
        return new HomeWeatherData(
                cityName,
                locationId,
                "19",
                "Cloudy",
                "18",
                "24",
                "17",
                "70",
                "NE",
                "2",
                "8",
                "1008",
                "6.0",
                "101",
                1716600000000L,
                "Wear jacket",
                "Good for travel",
                "42",
                "Good",
                "None"
        );
    }

    private static final class FakeCityDao implements CityDao {
        private final List<CityEntity> cities = new ArrayList<>();

        private FakeCityDao(CityEntity city) {
            cities.add(city);
        }

        @Override
        public void insert(CityEntity city) {
            cities.add(city);
        }

        @Override
        public List<CityEntity> findAll() {
            return cities;
        }

        @Override
        public CityEntity findByLocationId(String locationId) {
            for (CityEntity city : cities) {
                if (city.locationId.equals(locationId)) {
                    return city;
                }
            }
            return null;
        }

        @Override
        public CityEntity findDefaultCity() {
            for (CityEntity city : cities) {
                if (city.isDefault) {
                    return city;
                }
            }
            return null;
        }

        @Override
        public void clearDefaultCity() {
            for (CityEntity city : cities) {
                city.isDefault = false;
            }
        }

        @Override
        public void setDefaultCity(String locationId, long updateTime) {
            CityEntity city = findByLocationId(locationId);
            if (city != null) {
                city.isDefault = true;
                city.updateTime = updateTime;
            }
        }

        @Override
        public void updateSortOrder(String locationId, int sortOrder, long updateTime) {
            CityEntity city = findByLocationId(locationId);
            if (city != null) {
                city.sortOrder = sortOrder;
                city.updateTime = updateTime;
            }
        }

        @Override
        public void deleteByLocationId(String locationId) {
            cities.removeIf(city -> city.locationId.equals(locationId));
        }

        @Override
        public int count() {
            return cities.size();
        }
    }
}
