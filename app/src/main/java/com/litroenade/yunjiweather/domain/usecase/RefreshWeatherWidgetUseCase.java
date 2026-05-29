package com.litroenade.yunjiweather.domain.usecase;

import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.WeatherRepository;

public final class RefreshWeatherWidgetUseCase {

    private final CityRepository cityRepository;
    private final WeatherRepository weatherRepository;
    private final Clock clock;

    public RefreshWeatherWidgetUseCase(
            CityRepository cityRepository,
            WeatherRepository weatherRepository,
            Clock clock
    ) {
        this.cityRepository = cityRepository;
        this.weatherRepository = weatherRepository;
        this.clock = clock;
    }

    public Result execute(boolean forceRefresh) {
        CityEntity city = cityRepository.resolveDefaultCity(clock.now());
        if (!forceRefresh && weatherRepository.hasFreshHomeWeatherCache(city.locationId)) {
            return new Result(Status.SKIPPED_FRESH_CACHE);
        }
        UiState<HomeWeatherData> state = weatherRepository.loadHomeWeather(
                city.locationId,
                city.cityName,
                city.latitude,
                city.longitude
        );
        if (state.getStatus() == UiState.Status.ERROR) {
            return new Result(Status.FAILED);
        }
        return new Result(Status.REFRESHED);
    }

    public enum Status {
        REFRESHED,
        SKIPPED_FRESH_CACHE,
        FAILED
    }

    public interface Clock {
        long now();
    }

    public static final class Result {
        private final Status status;

        private Result(Status status) {
            this.status = status;
        }

        public Status getStatus() {
            return status;
        }
    }
}
