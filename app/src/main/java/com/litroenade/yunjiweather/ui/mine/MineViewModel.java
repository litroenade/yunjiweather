package com.litroenade.yunjiweather.ui.mine;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.settings.SettingsManager;
import com.litroenade.yunjiweather.utils.DefaultCityUtils;
import com.litroenade.yunjiweather.utils.MineCacheStatusUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MineViewModel extends AndroidViewModel {

    private final SettingsManager settingsManager;
    private final AppDatabase database;
    private final ExecutorService diskExecutor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<String> defaultCity = new MutableLiveData<>();
    private final MutableLiveData<Boolean> warningEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> animationEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> darkModeEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> temperatureUnit = new MutableLiveData<>();
    private final MutableLiveData<String> windUnit = new MutableLiveData<>();
    private final MutableLiveData<Boolean> dailyReminderEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> dataUpdateTime = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public MineViewModel(@NonNull Application application) {
        super(application);
        settingsManager = new SettingsManager(application);
        database = AppDatabase.getInstance(application);
        refresh();
    }

    public LiveData<String> getDefaultCity() {
        return defaultCity;
    }

    public LiveData<Boolean> getWarningEnabled() {
        return warningEnabled;
    }

    public LiveData<Boolean> getAnimationEnabled() {
        return animationEnabled;
    }

    public LiveData<Boolean> getDarkModeEnabled() {
        return darkModeEnabled;
    }

    public LiveData<String> getTemperatureUnit() {
        return temperatureUnit;
    }

    public LiveData<String> getWindUnit() {
        return windUnit;
    }

    public LiveData<Boolean> getDailyReminderEnabled() {
        return dailyReminderEnabled;
    }

    public LiveData<String> getDataUpdateTime() {
        return dataUpdateTime;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void refresh() {
        reloadSettings();
        refreshDefaultCity();
        refreshDataUpdateTime();
    }

    public void setWarningEnabled(boolean enabled) {
        settingsManager.setWarningEnabled(enabled);
        reloadSettings();
    }

    public void setAnimationEnabled(boolean enabled) {
        settingsManager.setAnimationEnabled(enabled);
        reloadSettings();
    }

    public void setDarkModeEnabled(boolean enabled) {
        settingsManager.setDarkModeEnabled(enabled);
        reloadSettings();
    }

    public void setTemperatureUnit(String unit) {
        settingsManager.setTemperatureUnit(unit);
        reloadSettings();
    }

    public void setWindUnit(String unit) {
        settingsManager.setWindUnit(unit);
        reloadSettings();
    }

    public void setDailyReminderEnabled(boolean enabled) {
        settingsManager.setDailyReminderEnabled(enabled);
        reloadSettings();
    }

    public void clearCache() {
        diskExecutor.execute(() -> {
            database.weatherCacheDao().clearAll();
            message.postValue("天气缓存已清理");
            dataUpdateTime.postValue(MineCacheStatusUtils.formatDataUpdateTime(null));
        });
    }

    private void reloadSettings() {
        warningEnabled.setValue(settingsManager.isWarningEnabled());
        animationEnabled.setValue(settingsManager.isAnimationEnabled());
        darkModeEnabled.setValue(settingsManager.isDarkModeEnabled());
        temperatureUnit.setValue(settingsManager.getTemperatureUnit());
        windUnit.setValue(settingsManager.getWindUnit());
        dailyReminderEnabled.setValue(settingsManager.isDailyReminderEnabled());
    }

    private void refreshDefaultCity() {
        diskExecutor.execute(() -> {
            CityEntity city = DefaultCityUtils.resolveDefaultCity(database.cityDao(), System.currentTimeMillis());
            defaultCity.postValue(DefaultCityUtils.formatDefaultCityText(city));
        });
    }

    private void refreshDataUpdateTime() {
        diskExecutor.execute(() -> {
            Long latestUpdateTime = database.weatherCacheDao().findLatestUpdateTime();
            dataUpdateTime.postValue(MineCacheStatusUtils.formatDataUpdateTime(latestUpdateTime));
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        diskExecutor.shutdown();
    }
}
