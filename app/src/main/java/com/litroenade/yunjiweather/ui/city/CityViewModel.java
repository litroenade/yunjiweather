package com.litroenade.yunjiweather.ui.city;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.litroenade.yunjiweather.data.api.CityLookupGateway;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.model.CityWeatherSummary;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.CityWeatherSummaryRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CityViewModel extends AndroidViewModel {

    private final CityRepository cityRepository;
    private final CityLookupGateway cityLookupGateway;
    private final CityWeatherSummaryRepository cityWeatherSummaryRepository;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<List<CityEntity>> cities = new MutableLiveData<>();
    private final MutableLiveData<List<CityEntity>> searchResults = new MutableLiveData<>();
    private final MutableLiveData<Map<String, CityWeatherSummary>> citySummaries = new MutableLiveData<>();
    private final MutableLiveData<String> defaultCity = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    private final MutableLiveData<Long> defaultCityChangeVersion = new MutableLiveData<>(0L);
    private long defaultCityChangeCounter;
    private volatile boolean cleared;

    @Inject
    public CityViewModel(
            @NonNull Application application,
            CityRepository cityRepository,
            CityLookupGateway cityLookupGateway,
            CityWeatherSummaryRepository cityWeatherSummaryRepository
    ) {
        super(application);
        this.cityRepository = cityRepository;
        this.cityLookupGateway = cityLookupGateway;
        this.cityWeatherSummaryRepository = cityWeatherSummaryRepository;
        reload();
    }

    public LiveData<List<CityEntity>> getCities() {
        return cities;
    }

    public LiveData<List<CityEntity>> getSearchResults() {
        return searchResults;
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

    public LiveData<Long> getDefaultCityChangeVersion() {
        return defaultCityChangeVersion;
    }

    public void addCity(String cityName) {
        busy.setValue(true);
        executorService.execute(() -> {
            try {
                CityEntity city = createCity(cityName);
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
                searchResults.postValue(Collections.emptyList());
                message.postValue("城市已添加");
                reloadOnExecutor();
            } catch (IOException exception) {
                message.postValue(exception.getMessage());
            } finally {
                busy.postValue(false);
            }
        });
    }

    public void searchCities(String cityName) {
        String normalizedCityName = cityName == null ? "" : cityName.trim();
        if (normalizedCityName.isEmpty()) {
            searchResults.setValue(Collections.emptyList());
            message.setValue("请输入城市名称");
            return;
        }
        busy.setValue(true);
        executorService.execute(() -> {
            try {
                List<CityEntity> results = cityLookupGateway.searchCities(
                        normalizedCityName,
                        cityRepository.count() + 1,
                        System.currentTimeMillis()
                );
                searchResults.postValue(results);
                message.postValue(results.isEmpty() ? "未找到匹配城市" : "请选择要添加的城市");
            } catch (IOException exception) {
                searchResults.postValue(Collections.emptyList());
                message.postValue(exception.getMessage());
            } finally {
                busy.postValue(false);
            }
        });
    }

    public void addCity(CityEntity city) {
        if (city == null) {
            return;
        }
        busy.setValue(true);
        executorService.execute(() -> {
            try {
                if (cityRepository.findByLocationId(city.locationId) != null) {
                    message.postValue("该城市已存在");
                    return;
                }
                long nowTime = System.currentTimeMillis();
                city.isDefault = cityRepository.count() == 0;
                city.sortOrder = cityRepository.count() + 1;
                city.updateTime = nowTime;
                city.createTime = nowTime;
                cityRepository.insert(city);
                searchResults.postValue(Collections.emptyList());
                message.postValue("城市已添加");
                reloadOnExecutor();
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
                if (city.isDefault) {
                    defaultCityChangeVersion.postValue(++defaultCityChangeCounter);
                }
            } finally {
                busy.postValue(false);
            }
        });
    }

    public void moveCityUp(CityEntity city) {
        moveCity(city, -1);
    }

    public void moveCityDown(CityEntity city) {
        moveCity(city, 1);
    }

    public void setDefaultCity(CityEntity city) {
        busy.setValue(true);
        executorService.execute(() -> {
            try {
                cityRepository.setDefaultCity(city.locationId, System.currentTimeMillis());
                message.postValue("默认城市已切换为 " + city.cityName);
                reloadOnExecutor();
                defaultCityChangeVersion.postValue(++defaultCityChangeCounter);
            } finally {
                busy.postValue(false);
            }
        });
    }

    private void moveCity(CityEntity city, int direction) {
        busy.setValue(true);
        executorService.execute(() -> {
            try {
                cityRepository.moveCity(city.locationId, direction, System.currentTimeMillis());
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

    private CityEntity createCity(String cityName) throws IOException {
        if (cityName == null || cityName.trim().isEmpty()) {
            return null;
        }
        return cityLookupGateway.searchCity(
                cityName,
                false,
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
