package com.litroenade.yunjiweather.ui.mine;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.WorkManager;

import com.litroenade.yunjiweather.auth.AuthSessionManager;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.settings.SettingsManager;
import com.litroenade.yunjiweather.utils.DefaultCityUtils;
import com.litroenade.yunjiweather.utils.LocalStorageSummaryUtils;
import com.litroenade.yunjiweather.utils.MineCacheStatusUtils;
import com.litroenade.yunjiweather.worker.WorkerScopeUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MineViewModel extends AndroidViewModel {

    private final SettingsManager settingsManager;
    private final AuthSessionManager authSessionManager;
    private final AppDatabase database;
    private final CityRepository cityRepository;
    private final long ownerUserId;
    private final ExecutorService diskExecutor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<String> accountText = new MutableLiveData<>();
    private final MutableLiveData<String> defaultCity = new MutableLiveData<>();
    private final MutableLiveData<Boolean> warningEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> animationEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> darkModeEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> temperatureUnit = new MutableLiveData<>();
    private final MutableLiveData<String> windUnit = new MutableLiveData<>();
    private final MutableLiveData<Boolean> dailyReminderEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> dataUpdateTime = new MutableLiveData<>();
    private final MutableLiveData<String> localStorageSummary = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> logoutEvent = new MutableLiveData<>(false);

    public MineViewModel(@NonNull Application application) {
        super(application);
        authSessionManager = new AuthSessionManager(application);
        ownerUserId = authSessionManager.requireUserId();
        settingsManager = new SettingsManager(application);
        database = AppDatabase.getInstance(application);
        cityRepository = new CityRepository(ownerUserId, database.cityDao());
        refresh();
    }

    public LiveData<String> getAccountText() {
        return accountText;
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

    public LiveData<String> getLocalStorageSummary() {
        return localStorageSummary;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getLogoutEvent() {
        return logoutEvent;
    }

    public void refresh() {
        refreshAccountText();
        reloadSettings();
        refreshDefaultCity();
        refreshDataUpdateTime();
        refreshLocalStorageSummary();
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
            database.weatherCacheDao().clearAll(ownerUserId);
            message.postValue("天气缓存已清理");
            dataUpdateTime.postValue(MineCacheStatusUtils.formatDataUpdateTime(null));
            refreshLocalStorageSummary();
        });
    }

    public void logout() {
        WorkManager.getInstance(getApplication()).cancelUniqueWork(WorkerScopeUtils.weatherAlertWorkName(ownerUserId));
        WorkManager.getInstance(getApplication()).cancelUniqueWork(WorkerScopeUtils.dailyWeatherWorkName(ownerUserId));
        authSessionManager.logout();
        logoutEvent.setValue(true);
    }

    public void consumeLogoutEvent() {
        logoutEvent.setValue(false);
    }

    private void refreshAccountText() {
        accountText.setValue("当前账户：" + authSessionManager.getDisplayName() + "（" + authSessionManager.getUsername() + "）");
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
            CityEntity city = cityRepository.resolveDefaultCity(System.currentTimeMillis());
            defaultCity.postValue(DefaultCityUtils.formatDefaultCityText(city));
        });
    }

    private void refreshDataUpdateTime() {
        diskExecutor.execute(() -> {
            Long latestUpdateTime = database.weatherCacheDao().findLatestUpdateTime(ownerUserId);
            dataUpdateTime.postValue(MineCacheStatusUtils.formatDataUpdateTime(latestUpdateTime));
        });
    }

    private void refreshLocalStorageSummary() {
        diskExecutor.execute(() -> {
            boolean accountExists = database.userDao().findById(ownerUserId) != null;
            int cityCount = cityRepository.count();
            int cacheCount = database.weatherCacheDao().count(ownerUserId);
            int warningCount = database.warningDao().count(ownerUserId);
            localStorageSummary.postValue(LocalStorageSummaryUtils.formatSummary(
                    accountExists,
                    cityCount,
                    cacheCount,
                    warningCount
            ));
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        diskExecutor.shutdown();
    }
}
