package com.litroenade.yunjiweather.domain.usecase;

import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.repository.AlertRepository;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.SettingsRepository;
import com.litroenade.yunjiweather.data.repository.WarningRefreshResult;
import com.litroenade.yunjiweather.notification.WarningNotificationDispatcher;
import com.litroenade.yunjiweather.notification.WarningNotifier;

public final class DispatchWarningNotificationsUseCase {

    private final CityRepository cityRepository;
    private final AlertRepository alertRepository;
    private final SettingsRepository settingsRepository;
    private final WarningNotificationDispatcher dispatcher;

    public DispatchWarningNotificationsUseCase(
            CityRepository cityRepository,
            AlertRepository alertRepository,
            SettingsRepository settingsRepository,
            WarningNotificationDispatcher dispatcher
    ) {
        this.cityRepository = cityRepository;
        this.alertRepository = alertRepository;
        this.settingsRepository = settingsRepository;
        this.dispatcher = dispatcher;
    }

    public Result execute(WarningNotifier notifier) {
        if (!settingsRepository.isWarningEnabled()) {
            return Result.skipped(SkipReason.DISABLED);
        }
        CityEntity defaultCity = cityRepository.findDefaultCity();
        if (defaultCity == null) {
            return Result.skipped(SkipReason.NO_DEFAULT_CITY);
        }
        WarningRefreshResult refreshResult = alertRepository.refreshWarnings(defaultCity.locationId);
        int sentCount = dispatcher.dispatch(
                refreshResult.getWarnings(),
                notifier,
                alertRepository::markNotified
        );
        return Result.dispatched(sentCount);
    }

    public enum SkipReason {
        DISABLED,
        NO_DEFAULT_CITY
    }

    public static final class Result {
        private final int sentCount;
        private final SkipReason skipReason;

        private Result(int sentCount, SkipReason skipReason) {
            this.sentCount = sentCount;
            this.skipReason = skipReason;
        }

        private static Result dispatched(int sentCount) {
            return new Result(sentCount, null);
        }

        private static Result skipped(SkipReason skipReason) {
            return new Result(0, skipReason);
        }

        public int getSentCount() {
            return sentCount;
        }

        public SkipReason getSkipReason() {
            return skipReason;
        }

        public boolean isSkipped() {
            return skipReason != null;
        }
    }
}
