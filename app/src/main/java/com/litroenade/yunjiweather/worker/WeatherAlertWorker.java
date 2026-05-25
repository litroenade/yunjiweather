package com.litroenade.yunjiweather.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.litroenade.yunjiweather.data.api.ApiClient;
import com.litroenade.yunjiweather.data.api.ApiConfig;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.data.repository.AlertRepository;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.WarningRefreshResult;
import com.litroenade.yunjiweather.notification.NotificationCandidateSelector;
import com.litroenade.yunjiweather.notification.SystemWarningNotifier;
import com.litroenade.yunjiweather.notification.WarningNotificationDispatcher;
import com.litroenade.yunjiweather.settings.SettingsManager;

import java.io.IOException;

public class WeatherAlertWorker extends Worker {

    public WeatherAlertWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SettingsManager settingsManager = new SettingsManager(context);
        if (!settingsManager.isWarningEnabled() || !ApiConfig.isConfigured()) {
            return Result.success();
        }

        AppDatabase database = AppDatabase.getInstance(context);
        CityEntity defaultCity = new CityRepository(database.cityDao()).findDefaultCity();
        if (defaultCity == null) {
            return Result.success();
        }

        AlertRepository repository = new AlertRepository(
                ApiClient.createWeatherApiService(),
                database.warningDao()
        );
        try {
            WarningRefreshResult result = repository.refreshWarnings(defaultCity.locationId);
            WarningNotificationDispatcher dispatcher = new WarningNotificationDispatcher(
                    new NotificationCandidateSelector()
            );
            dispatcher.dispatch(
                    result.getWarnings(),
                    new SystemWarningNotifier(context),
                    repository::markNotified
            );
            return Result.success();
        } catch (IOException | RuntimeException exception) {
            return Result.retry();
        }
    }
}
