package com.litroenade.yunjiweather.domain.usecase;

import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.data.local.CityDao;
import com.litroenade.yunjiweather.data.local.WarningDao;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.WeatherRepository;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class LoadHomeWeatherPageUseCaseTest {

    @Test
    public void loadDefaultPage_returnsExpiredCacheImmediatelyAndMarksBackgroundRefresh() {
        CityEntity beijing = city("Beijing", "beijing", true);
        CityEntity shanghai = city("Shanghai", "shanghai", false);
        FakeWeatherRepository weatherRepository = new FakeWeatherRepository();
        UiState<HomeWeatherData> cachedState = UiState.cache(homeWeather("Beijing", "19"), "cached", 100L);
        weatherRepository.cachedState = cachedState;
        weatherRepository.freshCache = false;
        WarningEntity warning = warning("beijing");
        LoadHomeWeatherPageUseCase useCase = new LoadHomeWeatherPageUseCase(
                new CityRepository(new FakeCityDao(beijing, shanghai)),
                weatherRepository,
                new FakeWarningDao(Collections.singletonList(warning))
        );

        LoadHomeWeatherPageUseCase.Result result = useCase.loadDefaultPage(500L);

        assertEquals("beijing", result.getSelectedCity().locationId);
        assertEquals(0, result.getSelectedPageIndex());
        assertEquals(2, result.getCityPages().size());
        assertSame(cachedState, result.getCachedState());
        assertSame(cachedState, result.getWeatherState());
        assertEquals(1, result.getActiveWarnings().size());
        assertFalse(result.shouldUpdateWidget());
        assertFalse(result.isSatisfiedByFreshCache());
        assertTrue(result.needsBackgroundRefresh());
        assertEquals(0, weatherRepository.remoteLoadCount);
    }

    @Test
    public void loadCityPage_usesFreshCacheWithoutRemoteWhenRefreshIsNotForced() {
        CityEntity beijing = city("Beijing", "beijing", true);
        CityEntity shanghai = city("Shanghai", "shanghai", false);
        FakeWeatherRepository weatherRepository = new FakeWeatherRepository();
        UiState<HomeWeatherData> cachedState = UiState.cache(homeWeather("Shanghai", "22"), "fresh cache", 200L);
        weatherRepository.cachedState = cachedState;
        weatherRepository.freshCache = true;
        LoadHomeWeatherPageUseCase useCase = new LoadHomeWeatherPageUseCase(
                new CityRepository(new FakeCityDao(beijing, shanghai)),
                weatherRepository,
                new FakeWarningDao(Collections.emptyList())
        );

        LoadHomeWeatherPageUseCase.Result result = useCase.loadCityPage("shanghai", false, 500L);

        assertEquals("shanghai", result.getSelectedCity().locationId);
        assertEquals(1, result.getSelectedPageIndex());
        assertSame(cachedState, result.getCachedState());
        assertSame(cachedState, result.getWeatherState());
        assertTrue(result.isSatisfiedByFreshCache());
        assertFalse(result.shouldUpdateWidget());
        assertFalse(result.needsBackgroundRefresh());
        assertEquals(0, weatherRepository.remoteLoadCount);
    }

    @Test
    public void loadCityPage_forceRefreshBypassesCacheAndUpdatesWidget() {
        CityEntity beijing = city("Beijing", "beijing", true);
        CityEntity shanghai = city("Shanghai", "shanghai", false);
        FakeWeatherRepository weatherRepository = new FakeWeatherRepository();
        UiState<HomeWeatherData> cachedState = UiState.cache(homeWeather("Shanghai", "22"), "expired cache", 200L);
        UiState<HomeWeatherData> remoteState = UiState.success(homeWeather("Shanghai", "25"));
        weatherRepository.cachedState = cachedState;
        weatherRepository.remoteState = remoteState;
        LoadHomeWeatherPageUseCase useCase = new LoadHomeWeatherPageUseCase(
                new CityRepository(new FakeCityDao(beijing, shanghai)),
                weatherRepository,
                new FakeWarningDao(Collections.emptyList())
        );

        LoadHomeWeatherPageUseCase.Result result = useCase.loadCityPage("shanghai", true, 500L);

        assertEquals("shanghai", result.getSelectedCity().locationId);
        assertSame(cachedState, result.getCachedState());
        assertSame(remoteState, result.getWeatherState());
        assertFalse(result.needsBackgroundRefresh());
        assertTrue(result.shouldUpdateWidget());
        assertEquals(1, weatherRepository.remoteLoadCount);
    }

    private static CityEntity city(String name, String locationId, boolean isDefault) {
        return new CityEntity(name, locationId, name, "China", 1.0, 2.0, isDefault, 0, 0L, 0L);
    }

    private static WarningEntity warning(String locationId) {
        return new WarningEntity("w1", locationId, "Rain", "rain", "yellow", "content", 100L, false, false);
    }

    private static HomeWeatherData homeWeather(String cityName, String temperature) {
        return new HomeWeatherData(
                cityName,
                cityName.toLowerCase(),
                temperature,
                "Cloudy",
                "26",
                "30",
                "20",
                "72",
                "North",
                "1",
                "18",
                "1003",
                "16",
                "101",
                1_700_000_000_000L,
                "clothing",
                "travel",
                "55",
                "Good",
                "PM2.5"
        );
    }

    private static final class FakeWeatherRepository extends WeatherRepository {
        private UiState<HomeWeatherData> cachedState;
        private UiState<HomeWeatherData> remoteState;
        private boolean freshCache;
        private int remoteLoadCount;

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
        public UiState<HomeWeatherData> loadHomeWeather(String locationId, String cityName, double latitude, double longitude) {
            remoteLoadCount++;
            return remoteState;
        }

        @Override
        public UiState<HomeWeatherData> loadHomeWeatherPreferCache(String locationId, String cityName, double latitude, double longitude) {
            remoteLoadCount++;
            return remoteState;
        }

        @Override
        public UiState<HomeWeatherData> loadCachedHomeWeather(String locationId) {
            return cachedState;
        }

        @Override
        public boolean hasFreshHomeWeatherCache(String locationId) {
            return freshCache;
        }
    }

    private static final class FakeCityDao implements CityDao {
        private final List<CityEntity> cities;

        private FakeCityDao(CityEntity... cities) {
            this.cities = new ArrayList<>();
            Collections.addAll(this.cities, cities);
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
        }

        @Override
        public void setDefaultCity(String locationId, long updateTime) {
        }

        @Override
        public void updateSortOrder(String locationId, int sortOrder, long updateTime) {
        }

        @Override
        public void deleteByLocationId(String locationId) {
        }

        @Override
        public int count() {
            return cities.size();
        }
    }

    private static final class FakeWarningDao implements WarningDao {
        private final List<WarningEntity> warnings;

        private FakeWarningDao(List<WarningEntity> warnings) {
            this.warnings = warnings;
        }

        @Override
        public void insertAll(List<WarningEntity> warnings) {
        }

        @Override
        public List<WarningEntity> findByLocationId(String locationId) {
            return warnings;
        }

        @Override
        public WarningEntity findByWarningId(String locationId, String warningId) {
            return null;
        }

        @Override
        public List<WarningEntity> findUnnotifiedWarnings() {
            return Collections.emptyList();
        }

        @Override
        public void markNotified(String locationId, String warningId) {
        }

        @Override
        public void markRead(String locationId, String warningId) {
        }

        @Override
        public void deleteMissingByLocation(String locationId, List<String> activeWarningIds) {
        }

        @Override
        public void deleteByLocationId(String locationId) {
        }

        @Override
        public int count() {
            return warnings.size();
        }
    }
}
