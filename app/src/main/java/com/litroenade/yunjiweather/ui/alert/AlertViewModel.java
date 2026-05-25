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
import com.litroenade.yunjiweather.data.repository.AlertRepository;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.WarningRefreshResult;
import com.litroenade.yunjiweather.data.repository.WarningSource;
import com.litroenade.yunjiweather.settings.SettingsManager;
import com.litroenade.yunjiweather.utils.WarningListUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlertViewModel extends AndroidViewModel {

    private final SettingsManager settingsManager;
    private final CityRepository cityRepository;
    private final AlertRepository alertRepository;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<String> alertStateText = new MutableLiveData<>();
    private final MutableLiveData<List<WarningEntity>> warnings = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>("");

    public AlertViewModel(@NonNull Application application) {
        super(application);
        settingsManager = new SettingsManager(application);
        AppDatabase database = AppDatabase.getInstance(application);
        cityRepository = new CityRepository(database.cityDao());
        alertRepository = new AlertRepository(
                ApiConfig.isConfigured() ? ApiClient.createWeatherApiService() : null,
                database.warningDao()
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

    public LiveData<String> getMessage() {
        return message;
    }

    public void refreshState() {
        loading.setValue(true);
        message.setValue("");
        executorService.execute(() -> {
            CityEntity city = null;
            try {
                city = resolveDefaultCity();
                WarningRefreshResult result = alertRepository.refreshWarnings(city.locationId);
                List<WarningEntity> refreshedWarnings = result.getWarnings();
                warnings.postValue(refreshedWarnings);
                if (result.getSource() == WarningSource.CACHE_NO_API) {
                    alertStateText.postValue(WarningListUtils.createNoQWeatherText(city.cityName, refreshedWarnings));
                } else {
                    alertStateText.postValue(createSuccessText(city, refreshedWarnings));
                }
            } catch (IOException | RuntimeException exception) {
                List<WarningEntity> cachedWarnings = city == null
                        ? Collections.emptyList()
                        : alertRepository.findByLocationId(city.locationId);
                warnings.postValue(cachedWarnings);
                alertStateText.postValue(createErrorText(cachedWarnings, exception));
            } finally {
                loading.postValue(false);
            }
        });
    }

    public void markWarningRead(String warningId) {
        executorService.execute(() -> {
            List<WarningEntity> currentWarnings = warnings.getValue();
            WarningEntity targetWarning = findWarning(currentWarnings, warningId);
            if (targetWarning != null) {
                alertRepository.markRead(targetWarning.locationId, warningId);
                message.postValue("已标记为已读：" + targetWarning.title);
            }
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
        return cityRepository.resolveDefaultCity(System.currentTimeMillis());
    }

    private WarningEntity findWarning(List<WarningEntity> warningList, String warningId) {
        if (warningList == null) {
            return null;
        }
        for (WarningEntity warning : warningList) {
            if (warning.warningId.equals(warningId)) {
                return warning;
            }
        }
        return null;
    }

    private String createSuccessText(CityEntity city, List<WarningEntity> warningList) {
        if (warningList.isEmpty()) {
            return settingsManager.isWarningEnabled()
                    ? city.cityName + " 暂无天气预警，预警通知已开启。"
                    : city.cityName + " 暂无天气预警，预警通知已关闭。";
        }
        String notificationText = settingsManager.isWarningEnabled() ? "通知已开启" : "通知已关闭";
        return city.cityName + " 当前有 " + warningList.size() + " 条天气预警，" + notificationText + "。";
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
