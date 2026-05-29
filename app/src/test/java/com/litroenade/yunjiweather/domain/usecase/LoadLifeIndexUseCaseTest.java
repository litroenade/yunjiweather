package com.litroenade.yunjiweather.domain.usecase;

import com.google.gson.Gson;
import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.entity.WeatherCacheEntity;
import com.litroenade.yunjiweather.data.local.CityDao;
import com.litroenade.yunjiweather.data.local.LifeIndexCacheGateway;
import com.litroenade.yunjiweather.data.local.WeatherCacheDao;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.HomeWeatherCacheSource;
import com.litroenade.yunjiweather.data.repository.LifeIndexRepository;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class LoadLifeIndexUseCaseTest {

    @Test
    public void execute_usesDefaultCityAndFallsBackToLocalSuggestionsWhenWeatherCacheIsEmpty() {
        CityEntity defaultCity = createDefaultCity();
        LoadLifeIndexUseCase useCase = new LoadLifeIndexUseCase(
                new CityRepository(new FakeCityDao(defaultCity)),
                new LifeIndexRepository(new LifeIndexCacheGateway(new EmptyWeatherCacheDao(), new Gson())),
                new FakeHomeWeatherCacheSource(null)
        );

        LoadLifeIndexUseCase.Result result = useCase.execute(200L);

        assertEquals("Beijing", result.getCity().cityName);
        assertEquals(LifeIndexRepository.LoadSource.LOCAL, result.getLoadResult().getSource());
        assertFalse(result.getLoadResult().getItems().isEmpty());
    }

    @Test
    public void execute_generatesLocalSuggestionsFromCachedHomeWeather() {
        CityEntity defaultCity = createDefaultCity();
        LoadLifeIndexUseCase useCase = new LoadLifeIndexUseCase(
                new CityRepository(new FakeCityDao(defaultCity)),
                new LifeIndexRepository(new LifeIndexCacheGateway(new EmptyWeatherCacheDao(), new Gson())),
                new FakeHomeWeatherCacheSource(createWeather("34", "晴", "2", "45", "8"))
        );

        LoadLifeIndexUseCase.Result result = useCase.execute(200L);

        assertEquals(LifeIndexRepository.LoadSource.LOCAL_WEATHER, result.getLoadResult().getSource());
        assertEquals(10, result.getLoadResult().getItems().size());
    }

    private CityEntity createDefaultCity() {
        return new CityEntity(
                "Beijing",
                "openmeteo:1816670",
                "Beijing",
                "China",
                39.9042,
                116.4074,
                true,
                0,
                100L,
                100L
        );
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
                "60",
                "南风",
                windScale,
                "10",
                "1008",
                "10",
                "100",
                100L,
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

    private static final class FakeHomeWeatherCacheSource implements HomeWeatherCacheSource {
        private final HomeWeatherData weatherData;

        private FakeHomeWeatherCacheSource(HomeWeatherData weatherData) {
            this.weatherData = weatherData;
        }

        @Override
        public UiState<HomeWeatherData> loadCachedHomeWeather(String locationId) {
            return weatherData == null ? null : UiState.cache(weatherData, "cache", 100L);
        }
    }

    private static final class FakeCityDao implements CityDao {
        private final CityEntity defaultCity;

        private FakeCityDao(CityEntity defaultCity) {
            this.defaultCity = defaultCity;
        }

        @Override
        public void insert(CityEntity city) {
        }

        @Override
        public List<CityEntity> findAll() {
            return Collections.singletonList(defaultCity);
        }

        @Override
        public CityEntity findByLocationId(String locationId) {
            return defaultCity.locationId.equals(locationId) ? defaultCity : null;
        }

        @Override
        public CityEntity findDefaultCity() {
            return defaultCity;
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
            return 1;
        }
    }

    private static final class EmptyWeatherCacheDao implements WeatherCacheDao {
        @Override
        public void insert(WeatherCacheEntity entity) {
        }

        @Override
        public WeatherCacheEntity findByLocationAndType(String locationId, String weatherType) {
            return null;
        }

        @Override
        public Long findLatestUpdateTime() {
            return null;
        }

        @Override
        public void clearAll() {
        }

        @Override
        public int count() {
            return 0;
        }
    }
}
