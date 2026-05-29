package com.litroenade.yunjiweather.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.api.CityLookupGateway;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.domain.usecase.LoadHomeWeatherPageUseCase;
import com.litroenade.yunjiweather.widget.WeatherAppWidgetProvider;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends AndroidViewModel {

    private final MutableLiveData<UiState<HomeWeatherData>> uiState = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<List<CityEntity>> cityPages = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Integer> selectedCityPage = new MutableLiveData<>(0);
    private final MutableLiveData<List<WarningEntity>> activeWarnings = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> refreshing = new MutableLiveData<>(false);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final CityLookupGateway cityLookupGateway;
    private final CityRepository cityRepository;
    private final LoadHomeWeatherPageUseCase loadHomeWeatherPageUseCase;
    private volatile String activeLocationId;

    @Inject
    public HomeViewModel(
            @NonNull Application application,
            CityRepository cityRepository,
            CityLookupGateway cityLookupGateway,
            LoadHomeWeatherPageUseCase loadHomeWeatherPageUseCase
    ) {
        super(application);
        this.cityRepository = cityRepository;
        this.cityLookupGateway = cityLookupGateway;
        this.loadHomeWeatherPageUseCase = loadHomeWeatherPageUseCase;
    }

    public LiveData<UiState<HomeWeatherData>> getUiState() {
        return uiState;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<List<CityEntity>> getCityPages() {
        return cityPages;
    }

    public LiveData<Integer> getSelectedCityPage() {
        return selectedCityPage;
    }

    public LiveData<List<WarningEntity>> getActiveWarnings() {
        return activeWarnings;
    }

    public LiveData<Boolean> getRefreshing() {
        return refreshing;
    }

    public void loadHomeWeather() {
        showLoadingIfWeatherIsEmpty();
        executorService.execute(() -> {
            try {
                LoadHomeWeatherPageUseCase.Result result = loadHomeWeatherPageUseCase.loadDefaultPage(
                        System.currentTimeMillis()
                );
                publishHomeWeatherResult(result);
                refreshExpiredVisibleCacheIfNeeded(result);
            } catch (RuntimeException exception) {
                refreshing.postValue(false);
                uiState.postValue(UiState.error(buildLoadErrorMessage(exception)));
            }
        });
    }

    public void refresh() {
        String locationId = activeLocationId;
        if (locationId == null || locationId.trim().isEmpty()) {
            loadHomeWeather();
            return;
        }
        loadHomeWeatherForCity(locationId, true);
    }

    public void loadHomeWeatherForCity(String locationId) {
        loadHomeWeatherForCity(locationId, false);
    }

    private void loadHomeWeatherForCity(String locationId, boolean forceRefresh) {
        if (locationId == null || locationId.trim().isEmpty()) {
            loadHomeWeather();
            return;
        }
        if (forceRefresh) {
            refreshing.setValue(true);
        }
        showLoadingIfWeatherIsEmpty();
        executorService.execute(() -> {
            try {
                LoadHomeWeatherPageUseCase.Result result = loadHomeWeatherPageUseCase.loadCityPage(
                        locationId,
                        forceRefresh,
                        System.currentTimeMillis()
                );
                publishHomeWeatherResult(result);
                refreshExpiredVisibleCacheIfNeeded(result);
            } catch (RuntimeException exception) {
                uiState.postValue(UiState.error(buildLoadErrorMessage(exception)));
            } finally {
                if (forceRefresh) {
                    refreshing.postValue(false);
                }
            }
        });
    }

    public void updateDefaultCityByLocation(double latitude, double longitude) {
        refreshing.setValue(true);
        showLoadingIfWeatherIsEmpty();
        executorService.execute(() -> {
            try {
                CityEntity city = resolveCityByCoordinate(latitude, longitude);
                saveAsDefaultCity(city);
                LoadHomeWeatherPageUseCase.Result result = loadHomeWeatherPageUseCase.loadCityPage(
                        city.locationId,
                        true,
                        System.currentTimeMillis()
                );
                publishHomeWeatherResult(result);
                message.postValue("已定位到 " + city.cityName);
                refreshing.postValue(false);
            } catch (IOException | RuntimeException exception) {
                message.postValue("定位城市解析失败：" + exception.getMessage());
                try {
                    LoadHomeWeatherPageUseCase.Result fallbackResult = loadHomeWeatherPageUseCase.loadDefaultPage(
                            System.currentTimeMillis()
                    );
                    publishHomeWeatherResult(fallbackResult);
                } catch (RuntimeException fallbackException) {
                    uiState.postValue(UiState.error(buildLoadErrorMessage(fallbackException)));
                } finally {
                    refreshing.postValue(false);
                }
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdownNow();
    }

    private void publishHomeWeatherResult(LoadHomeWeatherPageUseCase.Result result) {
        cityPages.postValue(result.getCityPages());
        selectedCityPage.postValue(result.getSelectedPageIndex());
        activeWarnings.postValue(result.getActiveWarnings());
        activeLocationId = result.getSelectedCity().locationId;
        UiState<HomeWeatherData> cachedState = result.getCachedState();
        if (cachedState != null && cachedState.getData() != null) {
            uiState.postValue(cachedState);
        }
        if (result.getWeatherState() != cachedState) {
            uiState.postValue(result.getWeatherState());
        }
        if (result.shouldUpdateWidget()) {
            WeatherAppWidgetProvider.updateAll(getApplication());
        }
    }

    private void refreshExpiredVisibleCacheIfNeeded(LoadHomeWeatherPageUseCase.Result result) {
        if (!result.needsBackgroundRefresh()) {
            if (!Boolean.TRUE.equals(refreshing.getValue())) {
                refreshing.postValue(false);
            }
            return;
        }
        CityEntity city = result.getSelectedCity();
        refreshing.postValue(true);
        UiState<HomeWeatherData> cachedState = result.getCachedState();
        String cacheMessage = cachedState == null ? null : cachedState.getMessage();
        message.postValue(cacheMessage == null || cacheMessage.trim().isEmpty()
                ? "正在同步 " + city.cityName + " 最新天气。"
                : cacheMessage);
        try {
            LoadHomeWeatherPageUseCase.Result refreshResult = loadHomeWeatherPageUseCase.loadCityPage(
                    city.locationId,
                    true,
                    System.currentTimeMillis()
            );
            publishHomeWeatherResult(refreshResult);
        } catch (RuntimeException exception) {
            message.postValue("天气同步失败，继续显示本地缓存：" + buildLoadErrorMessage(exception));
        } finally {
            refreshing.postValue(false);
        }
    }

    private void showLoadingIfWeatherIsEmpty() {
        UiState<HomeWeatherData> currentState = uiState.getValue();
        if (currentState == null || currentState.getData() == null) {
            uiState.setValue(UiState.loading());
        }
    }

    private CityEntity resolveCityByCoordinate(double latitude, double longitude) throws IOException {
        return cityLookupGateway.reverseLookup(latitude, longitude, System.currentTimeMillis());
    }

    private void saveAsDefaultCity(CityEntity city) {
        cityRepository.saveAsDefaultCity(city, System.currentTimeMillis());
    }

    private String buildLoadErrorMessage(RuntimeException exception) {
        String detail = exception.getMessage();
        if (detail == null || detail.trim().isEmpty()) {
            detail = exception.getClass().getSimpleName();
        }
        return "首页天气加载失败：" + detail;
    }
}
