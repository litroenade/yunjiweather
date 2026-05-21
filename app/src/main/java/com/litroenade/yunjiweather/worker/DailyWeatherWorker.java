package com.litroenade.yunjiweather.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.litroenade.yunjiweather.auth.AuthSessionManager;
import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.api.WeatherGatewayFactory;
import com.litroenade.yunjiweather.data.api.WeatherApiService;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.WeatherRepository;
import com.litroenade.yunjiweather.data.repository.WeatherRepositoryFactory;
import com.litroenade.yunjiweather.notification.NotificationHelper;
import com.litroenade.yunjiweather.settings.SettingsManager;
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils;

public class DailyWeatherWorker extends Worker {

    public DailyWeatherWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        AuthSessionManager authSessionManager = new AuthSessionManager(context);
        long scheduledOwnerUserId = getInputData().getLong(WorkerScopeUtils.KEY_OWNER_USER_ID, -1L);
        if (!WorkerScopeUtils.shouldRunForCurrentUser(scheduledOwnerUserId, authSessionManager)) {
            return Result.success();
        }
        long ownerUserId = scheduledOwnerUserId;
        SettingsManager settingsManager = new SettingsManager(context);
        if (!settingsManager.isDailyReminderEnabled()) {
            return Result.success();
        }

        try {
            AppDatabase database = AppDatabase.getInstance(context);
            CityEntity defaultCity = new CityRepository(ownerUserId, database.cityDao()).findDefaultCity();
            if (defaultCity == null) {
                return Result.success();
            }

            WeatherApiService apiService = WeatherGatewayFactory.createQWeatherServiceOrNull();
            WeatherRepository repository = WeatherRepositoryFactory.createHomeRepository(ownerUserId, database, apiService);
            UiState<HomeWeatherData> state = repository.loadHomeWeather(
                    defaultCity.locationId,
                    defaultCity.cityName,
                    defaultCity.latitude,
                    defaultCity.longitude
            );
            HomeWeatherData data = state.getData();
            if ((state.getStatus() == UiState.Status.SUCCESS || state.getStatus() == UiState.Status.CACHE) && data != null) {
                String temperatureText = WeatherDisplayUtils.formatTemperature(
                        data.getTemperature(),
                        settingsManager.getTemperatureUnit()
                );
                String content = data.getCityName()
                        + " "
                        + data.getCondition()
                        + "，当前 "
                        + temperatureText
                        + "。"
                        + data.getTravelAdvice();
                NotificationHelper.showDailyWeatherNotification(context, "今日天气提醒", content);
                return Result.success();
            }
            return Result.retry();
        } catch (RuntimeException exception) {
            return Result.retry();
        }
    }
}
