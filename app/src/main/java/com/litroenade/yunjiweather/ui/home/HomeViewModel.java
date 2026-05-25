package com.litroenade.yunjiweather.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.api.CityLookupGateway;
import com.litroenade.yunjiweather.data.api.WeatherApiService;
import com.litroenade.yunjiweather.data.api.WeatherGatewayFactory;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.WeatherRepository;
import com.litroenade.yunjiweather.data.repository.WeatherRepositoryFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeViewModel extends AndroidViewModel {

    private final MutableLiveData<UiState<HomeWeatherData>> uiState = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final WeatherRepository weatherRepository;
    private final CityLookupGateway cityLookupGateway;
    private final CityRepository cityRepository;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        cityRepository = new CityRepository(database.cityDao());
        WeatherApiService apiService = WeatherGatewayFactory.createQWeatherServiceOrNull();
        cityLookupGateway = WeatherGatewayFactory.createCityLookupGateway(apiService);
        weatherRepository = WeatherRepositoryFactory.createHomeRepository(database, apiService);
    }

    public LiveData<UiState<HomeWeatherData>> getUiState() {
        return uiState;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void loadHomeWeather() {
        uiState.setValue(UiState.loading());
        executorService.execute(() -> {
            try {
                CityEntity city = resolveDefaultCity();
                uiState.postValue(loadWeatherForCity(city));
            } catch (RuntimeException exception) {
                uiState.postValue(UiState.error(buildLoadErrorMessage(exception)));
            }
        });
    }

    public void refresh() {
        loadHomeWeather();
    }

    public void updateDefaultCityByLocation(double latitude, double longitude) {
        uiState.setValue(UiState.loading());
        executorService.execute(() -> {
            try {
                CityEntity city = resolveCityByCoordinate(latitude, longitude);
                saveAsDefaultCity(city);
                message.postValue("已定位到 " + city.cityName);
                uiState.postValue(loadWeatherForCity(city));
            } catch (IOException | RuntimeException exception) {
                message.postValue("定位城市解析失败：" + exception.getMessage());
                try {
                    CityEntity fallbackCity = resolveDefaultCity();
                    uiState.postValue(loadWeatherForCity(fallbackCity));
                } catch (RuntimeException fallbackException) {
                    uiState.postValue(UiState.error(buildLoadErrorMessage(fallbackException)));
                }
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdownNow();
    }

    private UiState<HomeWeatherData> loadWeatherForCity(CityEntity city) {
        return weatherRepository.loadHomeWeather(
                city.locationId,
                city.cityName,
                city.latitude,
                city.longitude
        );
    }

    private CityEntity resolveDefaultCity() {
        return cityRepository.resolveDefaultCity(System.currentTimeMillis());
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
