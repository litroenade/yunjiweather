package com.litroenade.yunjiweather.domain.usecase;

import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.SettingsRepository;
import com.litroenade.yunjiweather.data.repository.WeatherRepository;
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils;

public final class SendDailyWeatherReminderUseCase {

    private final CityRepository cityRepository;
    private final WeatherRepository weatherRepository;
    private final SettingsRepository settingsRepository;

    public SendDailyWeatherReminderUseCase(
            CityRepository cityRepository,
            WeatherRepository weatherRepository,
            SettingsRepository settingsRepository
    ) {
        this.cityRepository = cityRepository;
        this.weatherRepository = weatherRepository;
        this.settingsRepository = settingsRepository;
    }

    public Result execute() {
        if (!settingsRepository.isDailyReminderEnabled()) {
            return Result.skipped(SkipReason.DISABLED);
        }
        CityEntity defaultCity = cityRepository.findDefaultCity();
        if (defaultCity == null) {
            return Result.skipped(SkipReason.NO_DEFAULT_CITY);
        }
        UiState<HomeWeatherData> state = weatherRepository.loadHomeWeather(
                defaultCity.locationId,
                defaultCity.cityName,
                defaultCity.latitude,
                defaultCity.longitude
        );
        HomeWeatherData data = state.getData();
        if ((state.getStatus() == UiState.Status.SUCCESS || state.getStatus() == UiState.Status.CACHE) && data != null) {
            String temperatureText = WeatherDisplayUtils.formatTemperature(
                    data.getTemperature(),
                    settingsRepository.getTemperatureUnit()
            );
            String content = data.getCityName()
                    + " "
                    + data.getCondition()
                    + ", current "
                    + temperatureText
                    + ". "
                    + data.getTravelAdvice();
            return Result.ready("Daily weather reminder", content);
        }
        return Result.retry();
    }

    public enum SkipReason {
        DISABLED,
        NO_DEFAULT_CITY
    }

    public static final class Result {
        private final Status status;
        private final SkipReason skipReason;
        private final String title;
        private final String content;

        private Result(Status status, SkipReason skipReason, String title, String content) {
            this.status = status;
            this.skipReason = skipReason;
            this.title = title;
            this.content = content;
        }

        private static Result ready(String title, String content) {
            return new Result(Status.READY, null, title, content);
        }

        private static Result skipped(SkipReason skipReason) {
            return new Result(Status.SKIPPED, skipReason, "", "");
        }

        private static Result retry() {
            return new Result(Status.RETRY, null, "", "");
        }

        public Status getStatus() {
            return status;
        }

        public SkipReason getSkipReason() {
            return skipReason;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }
    }

    public enum Status {
        READY,
        SKIPPED,
        RETRY
    }
}
