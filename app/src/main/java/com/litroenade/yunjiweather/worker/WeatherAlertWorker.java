package com.litroenade.yunjiweather.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.litroenade.yunjiweather.data.api.ApiClient;
import com.litroenade.yunjiweather.data.api.ApiConfig;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.data.repository.AlertRepository;
import com.litroenade.yunjiweather.notification.NotificationHelper;
import com.litroenade.yunjiweather.settings.SettingsManager;

import java.io.IOException;
import java.util.List;

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
        CityEntity defaultCity = database.cityDao().findDefaultCity();
        if (defaultCity == null) {
            return Result.success();
        }

        AlertRepository repository = new AlertRepository(
                ApiClient.createWeatherApiService(),
                database.warningDao()
        );
        try {
            List<WarningEntity> warnings = repository.refreshWarnings(defaultCity.locationId);
            for (WarningEntity warning : warnings) {
                if (!warning.isNotified && NotificationHelper.showWarningNotification(context, warning)) {
                    database.warningDao().markNotified(warning.warningId);
                }
            }
            return Result.success();
        } catch (IOException | RuntimeException exception) {
            return Result.retry();
        }
    }
}
