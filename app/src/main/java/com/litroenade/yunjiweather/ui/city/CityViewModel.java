package com.litroenade.yunjiweather.ui.city;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.litroenade.yunjiweather.data.api.CityLookupGateway;
import com.litroenade.yunjiweather.data.api.WeatherApiService;
import com.litroenade.yunjiweather.data.api.WeatherGatewayFactory;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.data.model.CityWeatherSummary;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.CityWeatherSummaryRepository;
import com.litroenade.yunjiweather.data.repository.WeatherRepository;
import com.litroenade.yunjiweather.data.repository.WeatherRepositoryFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CityViewModel extends AndroidViewModel {

    private final CityRepository cityRepository;
    private final CityLookupGateway cityLookupGateway;
    private final CityWeatherSummaryRepository cityWeatherSummaryRepository;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<List<CityEntity>> cities = new MutableLiveData<>();
    private final MutableLiveData<Map<String, CityWeatherSummary>> citySummaries = new MutableLiveData<>();
    private final MutableLiveData<String> defaultCity = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    private volatile boolean cleared;

    public CityViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        cityRepository = new CityRepository(database.cityDao());
        WeatherApiService apiService = WeatherGatewayFactory.createQWeatherServiceOrNull();
        cityLookupGateway = WeatherGatewayFactory.createCityLookupGateway(apiService);
        WeatherRepository weatherRepository = WeatherRepositoryFactory.createHomeRepository(database, apiService);
        cityWeatherSummaryRepository = new CityWeatherSummaryRepository(weatherRepository);
        reload();
    }

    public LiveData<List<CityEntity>> getCities() {
        return cities;
    }

    public LiveData<Map<String, CityWeatherSummary>> getCitySummaries() {
        return citySummaries;
    }

    public LiveData<String> getDefaultCity() {
        return defaultCity;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getBusy() {
        return busy;
    }

    public void addCity(String cityName) {
        busy.setValue(true);
        executorService.execute(() -> {
            try {
                CityEntity city = createCity(cityName, false);
                if (city == null) {
                    message.postValue("请输入城市名称");
                    return;
                }
                if (cityRepository.findByLocationId(city.locationId) != null) {
                    message.postValue("该城市已存在");
                    return;
                }
                if (cityRepository.count() == 0) {
                    city.isDefault = true;
                }
                cityRepository.insert(city);
                message.postValue("城市已添加");
                reloadOnExecutor();
            } catch (IOException exception) {
                message.postValue(exception.getMessage());
            } finally {
                busy.postValue(false);
            }
        });
    }

    public void removeCity(CityEntity city) {
        busy.setValue(true);
        executorService.execute(() -> {
            try {
                cityRepository.deleteCity(city, System.currentTimeMillis());
                message.postValue("城市已删除");
                reloadOnExecutor();
            } finally {
                busy.postValue(false);
            }
        });
    }

    public void setDefaultCity(CityEntity city) {
        busy.setValue(true);
        executorService.execute(() -> {
            try {
                cityRepository.setDefaultCity(city.locationId, System.currentTimeMillis());
                message.postValue("默认城市已切换为 " + city.cityName);
                reloadOnExecutor();
            } finally {
                busy.postValue(false);
            }
        });
    }

    private void reload() {
        executorService.execute(this::reloadOnExecutor);
    }

    private void reloadOnExecutor() {
        cityRepository.resolveDefaultCity(System.currentTimeMillis());
        List<CityEntity> cityEntities = cityRepository.findAll();
        CityEntity defaultCityEntity = cityRepository.findDefaultCity();
        cities.postValue(cityEntities);
        defaultCity.postValue(defaultCityEntity == null ? "北京" : defaultCityEntity.cityName);
        Map<String, CityWeatherSummary> summaries = cityWeatherSummaryRepository.loadSummaries(
                cityEntities,
                this::publishCitySummaries
        );
        if (cityEntities.isEmpty()) {
            publishCitySummaries(summaries);
        }
    }

    private void publishCitySummaries(Map<String, CityWeatherSummary> summaries) {
        if (cleared) {
            return;
        }
        mainHandler.post(() -> {
            if (!cleared) {
                citySummaries.setValue(summaries);
            }
        });
    }

    private CityEntity createCity(String cityName, boolean isDefault) throws IOException {
        if (cityName == null || cityName.trim().isEmpty()) {
            return null;
        }
        return cityLookupGateway.searchCity(
                cityName,
                isDefault,
                cityRepository.count() + 1,
                System.currentTimeMillis()
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cleared = true;
        mainHandler.removeCallbacksAndMessages(null);
        executorService.shutdownNow();
    }
}
