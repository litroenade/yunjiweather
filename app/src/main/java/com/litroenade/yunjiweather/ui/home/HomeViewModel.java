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
import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.data.local.WarningDao;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.WeatherRepository;
import com.litroenade.yunjiweather.data.repository.WeatherRepositoryFactory;
import com.litroenade.yunjiweather.widget.WeatherAppWidgetProvider;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeViewModel extends AndroidViewModel {

    private final MutableLiveData<UiState<HomeWeatherData>> uiState = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<List<CityEntity>> cityPages = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Integer> selectedCityPage = new MutableLiveData<>(0);
    private final MutableLiveData<List<WarningEntity>> activeWarnings = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> refreshing = new MutableLiveData<>(false);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final WeatherRepository weatherRepository;
    private final CityLookupGateway cityLookupGateway;
    private final CityRepository cityRepository;
    private final WarningDao warningDao;
    private volatile String activeLocationId;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        cityRepository = new CityRepository(database.cityDao());
        warningDao = database.warningDao();
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

    public void publishMessage(String text) {
        if (text != null && !text.trim().isEmpty()) {
            message.setValue(text);
        }
    }

    public void loadHomeWeather() {
        refreshing.setValue(true);
        showLoadingIfWeatherIsEmpty();
        executorService.execute(() -> {
            try {
                CityEntity city = resolveDefaultCity();
                List<CityEntity> cities = publishCityPages(city.locationId);
                postCachedWeatherForCity(city);
                uiState.postValue(loadWeatherForCity(city));
                refreshing.postValue(false);
                refreshStaleCityCaches(cities, city.locationId);
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
                CityEntity city = cityRepository.findByLocationId(locationId);
                if (city == null) {
                    city = resolveDefaultCity();
                }
                publishCityPages(city.locationId);
                UiState<HomeWeatherData> cachedState = postCachedWeatherForCity(city);
                if (!forceRefresh && cachedState != null && weatherRepository.hasFreshHomeWeatherCache(city.locationId)) {
                    return;
                }
                uiState.postValue(loadWeatherForCity(city, !forceRefresh));
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
                List<CityEntity> cities = publishCityPages(city.locationId);
                message.postValue("已定位到 " + city.cityName);
                postCachedWeatherForCity(city);
                uiState.postValue(loadWeatherForCity(city));
                refreshing.postValue(false);
                refreshStaleCityCaches(cities, city.locationId);
            } catch (IOException | RuntimeException exception) {
                message.postValue("定位城市解析失败：" + exception.getMessage());
                try {
                    CityEntity fallbackCity = resolveDefaultCity();
                    uiState.postValue(loadWeatherForCity(fallbackCity));
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

    private UiState<HomeWeatherData> loadWeatherForCity(CityEntity city) {
        return loadWeatherForCity(city, false);
    }

    private UiState<HomeWeatherData> loadWeatherForCity(CityEntity city, boolean preferCache) {
        UiState<HomeWeatherData> state = preferCache
                ? weatherRepository.loadHomeWeatherPreferCache(
                        city.locationId,
                        city.cityName,
                        city.latitude,
                        city.longitude
                )
                : weatherRepository.loadHomeWeather(
                        city.locationId,
                        city.cityName,
                        city.latitude,
                        city.longitude
                );
        activeWarnings.postValue(warningDao.findByLocationId(city.locationId));
        if (state.getData() != null) {
            WeatherAppWidgetProvider.updateAll(getApplication());
        }
        return state;
    }

    private UiState<HomeWeatherData> postCachedWeatherForCity(CityEntity city) {
        UiState<HomeWeatherData> cachedState = weatherRepository.loadCachedHomeWeather(city.locationId);
        if (cachedState == null || cachedState.getData() == null) {
            return null;
        }
        activeWarnings.postValue(warningDao.findByLocationId(city.locationId));
        uiState.postValue(cachedState);
        return cachedState;
    }

    private void refreshStaleCityCaches(List<CityEntity> cities, String visibleLocationId) {
        for (CityEntity city : cities) {
            if (city.locationId.equals(visibleLocationId) || weatherRepository.hasFreshHomeWeatherCache(city.locationId)) {
                continue;
            }
            try {
                weatherRepository.loadHomeWeather(
                        city.locationId,
                        city.cityName,
                        city.latitude,
                        city.longitude
                );
            } catch (RuntimeException exception) {
                message.postValue(buildLoadErrorMessage(exception));
            }
        }
    }

    private void showLoadingIfWeatherIsEmpty() {
        UiState<HomeWeatherData> currentState = uiState.getValue();
        if (currentState == null || currentState.getData() == null) {
            uiState.setValue(UiState.loading());
        }
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

    private List<CityEntity> publishCityPages(String selectedLocationId) {
        List<CityEntity> cities = cityRepository.findAll();
        cityPages.postValue(cities);
        selectedCityPage.postValue(findCityPageIndex(cities, selectedLocationId));
        activeLocationId = selectedLocationId;
        return cities;
    }

    private int findCityPageIndex(List<CityEntity> cities, String selectedLocationId) {
        if (selectedLocationId == null) {
            return 0;
        }
        for (int i = 0; i < cities.size(); i++) {
            if (selectedLocationId.equals(cities.get(i).locationId)) {
                return i;
            }
        }
        return 0;
    }

    private String buildLoadErrorMessage(RuntimeException exception) {
        String detail = exception.getMessage();
        if (detail == null || detail.trim().isEmpty()) {
            detail = exception.getClass().getSimpleName();
        }
        return "首页天气加载失败：" + detail;
    }
}
