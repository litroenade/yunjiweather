package com.litroenade.yunjiweather.domain.usecase;

import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.repository.WeatherRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RefreshAllCityWeatherCacheUseCase {

    private final WeatherRepository weatherRepository;

    public RefreshAllCityWeatherCacheUseCase(WeatherRepository weatherRepository) {
        this.weatherRepository = weatherRepository;
    }

    public Result execute(List<CityEntity> cities, String visibleLocationId) {
        if (cities == null || cities.isEmpty()) {
            return new Result(0, Collections.emptyList());
        }
        int refreshedCount = 0;
        List<String> failedMessages = new ArrayList<>();
        for (CityEntity city : cities) {
            if (city.locationId.equals(visibleLocationId) || weatherRepository.hasFreshHomeWeatherCache(city.locationId)) {
                continue;
            }
            try {
                weatherRepository.loadHomeWeather(
                        city.locationId,
                        city.cityName,
                        city.latitude,
                        city.longitude
                );
                refreshedCount++;
            } catch (RuntimeException exception) {
                failedMessages.add(buildLoadErrorMessage(exception));
            }
        }
        return new Result(refreshedCount, failedMessages);
    }

    private static String buildLoadErrorMessage(RuntimeException exception) {
        String detail = exception.getMessage();
        if (detail == null || detail.trim().isEmpty()) {
            detail = exception.getClass().getSimpleName();
        }
        return "Home weather cache refresh failed: " + detail;
    }

    public static final class Result {
        private final int refreshedCount;
        private final List<String> failedMessages;

        private Result(int refreshedCount, List<String> failedMessages) {
            this.refreshedCount = refreshedCount;
            this.failedMessages = failedMessages;
        }

        public int getRefreshedCount() {
            return refreshedCount;
        }

        public List<String> getFailedMessages() {
            return failedMessages;
        }
    }
}
