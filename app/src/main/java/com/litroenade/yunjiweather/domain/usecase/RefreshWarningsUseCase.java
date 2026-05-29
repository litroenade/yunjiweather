package com.litroenade.yunjiweather.domain.usecase;

import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.data.repository.AlertRepository;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.SettingsRepository;
import com.litroenade.yunjiweather.data.repository.WarningRefreshResult;
import com.litroenade.yunjiweather.utils.WarningListUtils;

import java.util.List;

public final class RefreshWarningsUseCase {

    private final CityRepository cityRepository;
    private final AlertRepository alertRepository;
    private final SettingsRepository settingsRepository;

    public RefreshWarningsUseCase(
            CityRepository cityRepository,
            AlertRepository alertRepository,
            SettingsRepository settingsRepository
    ) {
        this.cityRepository = cityRepository;
        this.alertRepository = alertRepository;
        this.settingsRepository = settingsRepository;
    }

    public Result execute(long nowTime) {
        CityEntity city = cityRepository.resolveDefaultCity(nowTime);
        try {
            WarningRefreshResult refreshResult = alertRepository.refreshWarnings(city.locationId);
            List<WarningEntity> refreshedWarnings = refreshResult.getWarnings();
            return Result.success(
                    city,
                    refreshedWarnings,
                    WarningListUtils.createLocalCacheText(city.cityName, refreshedWarnings)
            );
        } catch (RuntimeException exception) {
            List<WarningEntity> cachedWarnings = alertRepository.findByLocationId(city.locationId);
            return Result.failure(city, cachedWarnings, createErrorText(cachedWarnings, exception));
        }
    }

    private String createErrorText(List<WarningEntity> cachedWarnings, RuntimeException exception) {
        String reason = exception.getMessage();
        if (reason == null || reason.trim().isEmpty()) {
            reason = exception.getClass().getSimpleName();
        }
        if (!cachedWarnings.isEmpty()) {
            return "Weather warning refresh failed; showing local cache. Reason: " + reason;
        }
        return settingsRepository.isWarningEnabled()
                ? "Weather warning refresh failed; no local cache. Reason: " + reason
                : "Warning notifications are disabled; no local warning cache.";
    }

    public static final class Result {
        private final CityEntity city;
        private final List<WarningEntity> warnings;
        private final String stateText;
        private final boolean success;

        private Result(CityEntity city, List<WarningEntity> warnings, String stateText, boolean success) {
            this.city = city;
            this.warnings = warnings;
            this.stateText = stateText;
            this.success = success;
        }

        private static Result success(CityEntity city, List<WarningEntity> warnings, String stateText) {
            return new Result(city, warnings, stateText, true);
        }

        private static Result failure(CityEntity city, List<WarningEntity> warnings, String stateText) {
            return new Result(city, warnings, stateText, false);
        }

        public CityEntity getCity() {
            return city;
        }

        public List<WarningEntity> getWarnings() {
            return warnings;
        }

        public String getStateText() {
            return stateText;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}
