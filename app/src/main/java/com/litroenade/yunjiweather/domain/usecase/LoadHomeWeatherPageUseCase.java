package com.litroenade.yunjiweather.domain.usecase;

import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.data.local.WarningDao;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.WeatherRepository;

import java.util.List;

public final class LoadHomeWeatherPageUseCase {

    private final CityRepository cityRepository;
    private final WeatherRepository weatherRepository;
    private final WarningDao warningDao;

    public LoadHomeWeatherPageUseCase(
            CityRepository cityRepository,
            WeatherRepository weatherRepository,
            WarningDao warningDao
    ) {
        this.cityRepository = cityRepository;
        this.weatherRepository = weatherRepository;
        this.warningDao = warningDao;
    }

    public Result loadDefaultPage(long nowTime) {
        CityEntity city = cityRepository.resolveDefaultCity(nowTime);
        return loadResolvedCity(city, false);
    }

    public Result loadCityPage(String locationId, boolean forceRefresh, long nowTime) {
        if (locationId == null || locationId.trim().isEmpty()) {
            return loadDefaultPage(nowTime);
        }
        CityEntity city = cityRepository.findByLocationId(locationId);
        if (city == null) {
            city = cityRepository.resolveDefaultCity(nowTime);
        }
        return loadResolvedCity(city, forceRefresh);
    }

    private Result loadResolvedCity(CityEntity city, boolean forceRefresh) {
        List<CityEntity> cities = cityRepository.findAll();
        int selectedPageIndex = findCityPageIndex(cities, city.locationId);
        UiState<HomeWeatherData> cachedState = weatherRepository.loadCachedHomeWeather(city.locationId);
        List<WarningEntity> warnings = warningDao.findByLocationId(city.locationId);
        if (!forceRefresh && cachedState != null && cachedState.getData() != null) {
            boolean freshCache = weatherRepository.hasFreshHomeWeatherCache(city.locationId);
            return new Result(
                    city,
                    cities,
                    selectedPageIndex,
                    cachedState,
                    cachedState,
                    warnings,
                    freshCache,
                    false,
                    !freshCache
            );
        }
        UiState<HomeWeatherData> weatherState = weatherRepository.loadHomeWeather(
                city.locationId,
                city.cityName,
                city.latitude,
                city.longitude
        );
        return new Result(
                city,
                cities,
                selectedPageIndex,
                cachedState,
                weatherState,
                warnings,
                false,
                weatherState.getData() != null,
                false
        );
    }

    private static int findCityPageIndex(List<CityEntity> cities, String selectedLocationId) {
        if (selectedLocationId == null) {
            return 0;
        }
        for (int i = 0; i < cities.size(); i++) {
            if (selectedLocationId.equals(cities.get(i).locationId)) {
                return i;
            }
        }
        return 0;
    }

    public static final class Result {
        private final CityEntity selectedCity;
        private final List<CityEntity> cityPages;
        private final int selectedPageIndex;
        private final UiState<HomeWeatherData> cachedState;
        private final UiState<HomeWeatherData> weatherState;
        private final List<WarningEntity> activeWarnings;
        private final boolean satisfiedByFreshCache;
        private final boolean updateWidget;
        private final boolean needsBackgroundRefresh;

        private Result(
                CityEntity selectedCity,
                List<CityEntity> cityPages,
                int selectedPageIndex,
                UiState<HomeWeatherData> cachedState,
                UiState<HomeWeatherData> weatherState,
                List<WarningEntity> activeWarnings,
                boolean satisfiedByFreshCache,
                boolean updateWidget,
                boolean needsBackgroundRefresh
        ) {
            this.selectedCity = selectedCity;
            this.cityPages = cityPages;
            this.selectedPageIndex = selectedPageIndex;
            this.cachedState = cachedState;
            this.weatherState = weatherState;
            this.activeWarnings = activeWarnings;
            this.satisfiedByFreshCache = satisfiedByFreshCache;
            this.updateWidget = updateWidget;
            this.needsBackgroundRefresh = needsBackgroundRefresh;
        }

        public CityEntity getSelectedCity() {
            return selectedCity;
        }

        public List<CityEntity> getCityPages() {
            return cityPages;
        }

        public int getSelectedPageIndex() {
            return selectedPageIndex;
        }

        public UiState<HomeWeatherData> getCachedState() {
            return cachedState;
        }

        public UiState<HomeWeatherData> getWeatherState() {
            return weatherState;
        }

        public List<WarningEntity> getActiveWarnings() {
            return activeWarnings;
        }

        public boolean isSatisfiedByFreshCache() {
            return satisfiedByFreshCache;
        }

        public boolean shouldUpdateWidget() {
            return updateWidget;
        }

        public boolean needsBackgroundRefresh() {
            return needsBackgroundRefresh;
        }
    }
}
