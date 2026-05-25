package com.litroenade.yunjiweather.ui.mine;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.settings.SettingsManager;
import com.litroenade.yunjiweather.utils.DefaultCityUtils;
import com.litroenade.yunjiweather.utils.LocalStorageSummaryUtils;
import com.litroenade.yunjiweather.utils.MineCacheStatusUtils;
import com.litroenade.yunjiweather.utils.VisualTheme;
import com.litroenade.yunjiweather.utils.VisualThemeCatalog;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MineViewModel extends AndroidViewModel {

    private final SettingsManager settingsManager;
    private final AppDatabase database;
    private final CityRepository cityRepository;
    private final ExecutorService diskExecutor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<String> localSpaceText = new MutableLiveData<>();
    private final MutableLiveData<String> defaultCity = new MutableLiveData<>();
    private final MutableLiveData<Boolean> warningEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> animationEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> darkModeEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> temperatureUnit = new MutableLiveData<>();
    private final MutableLiveData<String> windUnit = new MutableLiveData<>();
    private final MutableLiveData<Boolean> dailyReminderEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> visualTheme = new MutableLiveData<>();
    private final MutableLiveData<String> dataUpdateTime = new MutableLiveData<>();
    private final MutableLiveData<String> localStorageSummary = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public MineViewModel(@NonNull Application application) {
        super(application);
        settingsManager = new SettingsManager(application);
        database = AppDatabase.getInstance(application);
        cityRepository = new CityRepository(database.cityDao());
        refresh();
    }

    public LiveData<String> getLocalSpaceText() {
        return localSpaceText;
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

    public LiveData<String> getVisualTheme() {
        return visualTheme;
    }

    public String getCurrentVisualTheme() {
        return settingsManager.getVisualTheme();
    }

    public List<VisualTheme> getVisualThemes() {
        return VisualThemeCatalog.getThemes();
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

    public void refresh() {
        refreshLocalSpaceText();
        reloadSettings();
        refreshDefaultCity();
        refreshDataUpdateTime();
        refreshLocalStorageSummary();
    }

    public void setWarningEnabled(boolean enabled) {
        settingsManager.setWarningEnabled(enabled);
        reloadSettings();
        message.setValue(enabled ? "天气预警通知已开启" : "天气预警通知已关闭");
    }

    public void setAnimationEnabled(boolean enabled) {
        settingsManager.setAnimationEnabled(enabled);
        reloadSettings();
        message.setValue(enabled ? "天气动画已开启" : "天气动画已关闭");
    }

    public void setDarkModeEnabled(boolean enabled) {
        settingsManager.setDarkModeEnabled(enabled);
        reloadSettings();
        message.setValue(enabled ? "深色模式已开启" : "深色模式已关闭");
    }

    public void setTemperatureUnit(String unit) {
        settingsManager.setTemperatureUnit(unit);
        reloadSettings();
        message.setValue("温度单位已更新");
    }

    public void setWindUnit(String unit) {
        settingsManager.setWindUnit(unit);
        reloadSettings();
        message.setValue("风速单位已更新");
    }

    public void setDailyReminderEnabled(boolean enabled) {
        settingsManager.setDailyReminderEnabled(enabled);
        reloadSettings();
        message.setValue(enabled ? "每日提醒已开启" : "每日提醒已关闭");
    }

    public void setVisualTheme(String themeKey) {
        settingsManager.setVisualTheme(themeKey);
        reloadSettings();
        VisualTheme theme = VisualThemeCatalog.getThemeOrDefault(themeKey);
        message.setValue("视觉主题已应用：" + theme.getDisplayName());
    }

    public void clearCache() {
        diskExecutor.execute(() -> {
            database.weatherCacheDao().clearAll();
            message.postValue("天气缓存已清理");
            dataUpdateTime.postValue(MineCacheStatusUtils.formatDataUpdateTime(null));
            refreshLocalStorageSummary();
        });
    }

    private void refreshLocalSpaceText() {
        localSpaceText.setValue("本机天气空间");
    }

    private void reloadSettings() {
        warningEnabled.setValue(settingsManager.isWarningEnabled());
        animationEnabled.setValue(settingsManager.isAnimationEnabled());
        darkModeEnabled.setValue(settingsManager.isDarkModeEnabled());
        temperatureUnit.setValue(settingsManager.getTemperatureUnit());
        windUnit.setValue(settingsManager.getWindUnit());
        dailyReminderEnabled.setValue(settingsManager.isDailyReminderEnabled());
        visualTheme.setValue(settingsManager.getVisualTheme());
    }

    private void refreshDefaultCity() {
        diskExecutor.execute(() -> {
            CityEntity city = cityRepository.resolveDefaultCity(System.currentTimeMillis());
            defaultCity.postValue(DefaultCityUtils.formatDefaultCityText(city));
        });
    }

    private void refreshDataUpdateTime() {
        diskExecutor.execute(() -> {
            Long latestUpdateTime = database.weatherCacheDao().findLatestUpdateTime();
            dataUpdateTime.postValue(MineCacheStatusUtils.formatDataUpdateTime(latestUpdateTime));
        });
    }

    private void refreshLocalStorageSummary() {
        diskExecutor.execute(() -> {
            int cityCount = cityRepository.count();
            int cacheCount = database.weatherCacheDao().count();
            int warningCount = database.warningDao().count();
            localStorageSummary.postValue(LocalStorageSummaryUtils.formatSummary(
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
