package com.litroenade.yunjiweather.ui.alert;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.litroenade.yunjiweather.data.api.ApiClient;
import com.litroenade.yunjiweather.data.api.ApiConfig;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.data.local.CityDao;
import com.litroenade.yunjiweather.data.local.WarningDao;
import com.litroenade.yunjiweather.data.repository.AlertRepository;
import com.litroenade.yunjiweather.notification.NotificationHelper;
import com.litroenade.yunjiweather.settings.SettingsManager;
import com.litroenade.yunjiweather.utils.DefaultCityUtils;
import com.litroenade.yunjiweather.utils.WarningListUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlertViewModel extends AndroidViewModel {

    private final SettingsManager settingsManager;
    private final CityDao cityDao;
    private final WarningDao warningDao;
    private final AlertRepository alertRepository;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<String> alertStateText = new MutableLiveData<>();
    private final MutableLiveData<List<WarningEntity>> warnings = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public AlertViewModel(@NonNull Application application) {
        super(application);
        settingsManager = new SettingsManager(application);
        AppDatabase database = AppDatabase.getInstance(application);
        cityDao = database.cityDao();
        warningDao = database.warningDao();
        alertRepository = new AlertRepository(
                ApiConfig.isConfigured() ? ApiClient.createWeatherApiService() : null,
                warningDao
        );
        refreshState();
    }

    public LiveData<String> getAlertStateText() {
        return alertStateText;
    }

    public LiveData<List<WarningEntity>> getWarnings() {
        return warnings;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void refreshState() {
        loading.setValue(true);
        executorService.execute(() -> {
            CityEntity city = null;
            try {
                city = resolveDefaultCity();
                if (!ApiConfig.isConfigured()) {
                    List<WarningEntity> cachedWarnings = warningDao.findByLocationId(city.locationId);
                    warnings.postValue(cachedWarnings);
                    alertStateText.postValue(WarningListUtils.createNoQWeatherText(city.cityName, cachedWarnings));
                    return;
                }
                List<WarningEntity> refreshedWarnings = alertRepository.refreshWarnings(city.locationId);
                notifyNewWarnings(refreshedWarnings);
                warnings.postValue(refreshedWarnings);
                alertStateText.postValue(createSuccessText(city, refreshedWarnings));
            } catch (IOException | RuntimeException exception) {
                List<WarningEntity> cachedWarnings = city == null
                        ? Collections.emptyList()
                        : warningDao.findByLocationId(city.locationId);
                warnings.postValue(cachedWarnings);
                alertStateText.postValue(createErrorText(cachedWarnings, exception));
            } finally {
                loading.postValue(false);
            }
        });
    }

    public void markWarningRead(String warningId) {
        executorService.execute(() -> {
            warningDao.markRead(warningId);
            List<WarningEntity> currentWarnings = warnings.getValue();
            if (currentWarnings != null) {
                warnings.postValue(WarningListUtils.markRead(currentWarnings, warningId));
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdownNow();
    }

    private CityEntity resolveDefaultCity() {
        return DefaultCityUtils.resolveDefaultCity(cityDao, System.currentTimeMillis());
    }

    private void notifyNewWarnings(List<WarningEntity> warningList) {
        if (!settingsManager.isWarningEnabled()) {
            return;
        }
        for (WarningEntity warning : warningList) {
            if (!warning.isNotified && NotificationHelper.showWarningNotification(getApplication(), warning)) {
                warningDao.markNotified(warning.warningId);
                warning.isNotified = true;
            }
        }
    }

    private String createSuccessText(CityEntity city, List<WarningEntity> warningList) {
        if (warningList.isEmpty()) {
            return settingsManager.isWarningEnabled()
                    ? city.cityName + "暂无天气预警，预警通知已开启。"
                    : city.cityName + "暂无天气预警，预警通知已关闭。";
        }
        String notificationText = settingsManager.isWarningEnabled() ? "通知已开启" : "通知已关闭";
        return city.cityName + "当前有 " + warningList.size() + " 条天气预警，" + notificationText + "。";
    }

    private String createErrorText(List<WarningEntity> cachedWarnings, Exception exception) {
        if (!cachedWarnings.isEmpty()) {
            return "天气预警刷新失败，已显示本地缓存。原因：" + exception.getMessage();
        }
        if (!ApiConfig.isConfigured()) {
            return "未配置 QWeather API，暂无本地预警缓存。";
        }
        return "天气预警刷新失败，暂无本地缓存。原因：" + exception.getMessage();
    }
}
