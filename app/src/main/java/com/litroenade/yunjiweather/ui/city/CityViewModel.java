package com.litroenade.yunjiweather.ui.city;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.litroenade.yunjiweather.auth.AuthSessionManager;
import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.api.OpenMeteoCitySearchGateway;
import com.litroenade.yunjiweather.data.api.WeatherApiService;
import com.litroenade.yunjiweather.data.api.WeatherGatewayFactory;
import com.litroenade.yunjiweather.data.api.model.QWeatherCityLookupResponse;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.data.model.CityWeatherSummary;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.WeatherRepository;
import com.litroenade.yunjiweather.data.repository.WeatherRepositoryFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class CityViewModel extends AndroidViewModel {

    private static final String SUCCESS_CODE = "200";

    private final CityRepository cityRepository;
    private final WeatherApiService apiService;
    private final OpenMeteoCitySearchGateway openMeteoCitySearchGateway;
    private final WeatherRepository weatherRepository;
    private final long ownerUserId;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<CityEntity>> cities = new MutableLiveData<>();
    private final MutableLiveData<Map<String, CityWeatherSummary>> citySummaries = new MutableLiveData<>();
    private final MutableLiveData<String> defaultCity = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public CityViewModel(@NonNull Application application) {
        super(application);
        ownerUserId = new AuthSessionManager(application).requireUserId();
        AppDatabase database = AppDatabase.getInstance(application);
        cityRepository = new CityRepository(ownerUserId, database.cityDao());
        apiService = WeatherGatewayFactory.createQWeatherServiceOrNull();
        openMeteoCitySearchGateway = WeatherGatewayFactory.createOpenMeteoCitySearchGateway();
        weatherRepository = WeatherRepositoryFactory.createHomeRepository(ownerUserId, database, apiService);
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

    public void addCity(String cityName) {
        executorService.execute(() -> {
            CityEntity city;
            try {
                city = createCity(cityName, false);
            } catch (IOException exception) {
                message.postValue(exception.getMessage());
                return;
            }
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
            reload();
        });
    }

    public void removeCity(CityEntity city) {
        executorService.execute(() -> {
            cityRepository.deleteCity(city, System.currentTimeMillis());
            message.postValue("城市已删除");
            reload();
        });
    }

    public void setDefaultCity(CityEntity city) {
        executorService.execute(() -> {
            cityRepository.setDefaultCity(city.locationId, System.currentTimeMillis());
            message.postValue("默认城市已切换为 " + city.cityName);
            reload();
        });
    }

    private void reload() {
        executorService.execute(() -> {
            cityRepository.resolveDefaultCity(System.currentTimeMillis());
            List<CityEntity> cityEntities = cityRepository.findAll();
            CityEntity defaultCityEntity = cityRepository.findDefaultCity();
            cities.postValue(cityEntities);
            defaultCity.postValue(defaultCityEntity == null ? "北京" : defaultCityEntity.cityName);
            refreshCityWeatherSummaries(cityEntities);
        });
    }

    private void refreshCityWeatherSummaries(List<CityEntity> cityEntities) {
        Map<String, CityWeatherSummary> summaries = new LinkedHashMap<>();
        for (CityEntity city : cityEntities) {
            try {
                UiState<HomeWeatherData> state = weatherRepository.loadHomeWeather(
                        city.locationId,
                        city.cityName,
                        city.latitude,
                        city.longitude
                );
                summaries.put(city.locationId, CityWeatherSummary.fromWeatherState(city.locationId, state));
            } catch (RuntimeException exception) {
                summaries.put(city.locationId, CityWeatherSummary.unavailable(city.locationId));
            }
            citySummaries.postValue(new LinkedHashMap<>(summaries));
        }
        if (cityEntities.isEmpty()) {
            citySummaries.postValue(summaries);
        }
    }

    private CityEntity createCity(String cityName, boolean isDefault) throws IOException {
        if (cityName == null || cityName.trim().isEmpty()) {
            return null;
        }
        String normalized = cityName.trim();
        CityEntity presetCity = createPresetCity(normalized, isDefault);
        if (presetCity != null) {
            return presetCity;
        }
        if (apiService == null) {
            return openMeteoCitySearchGateway.searchCity(
                    ownerUserId,
                    normalized,
                    isDefault,
                    cityRepository.count() + 1,
                    System.currentTimeMillis()
            );
        }
        Response<QWeatherCityLookupResponse> response = apiService.searchCity(normalized, "cn", 1, "zh").execute();
        QWeatherCityLookupResponse body = response.body();
        if (!response.isSuccessful() || body == null || !SUCCESS_CODE.equals(body.code)) {
            throw new IOException("城市搜索接口请求失败，请检查网络或 API 配置。");
        }
        if (body.location == null || body.location.isEmpty()) {
            throw new IOException("未找到该城市，请检查城市名称。");
        }
        QWeatherCityLookupResponse.Location location = body.location.get(0);
        long nowTime = System.currentTimeMillis();
        return new CityEntity(
                ownerUserId,
                requireText(location.name, "location.name"),
                requireText(location.id, "location.id"),
                requireText(location.adm1, "location.adm1"),
                requireText(location.country, "location.country"),
                parseCoordinate(location.lat, "location.lat"),
                parseCoordinate(location.lon, "location.lon"),
                isDefault,
                cityRepository.count() + 1,
                nowTime,
                nowTime
        );
    }

    private CityEntity createPresetCity(String cityName, boolean isDefault) {
        long nowTime = System.currentTimeMillis();
        if ("北京".equals(cityName)) {
            return new CityEntity(ownerUserId, "北京", "101010100", "北京", "中国", 39.9042, 116.4074, isDefault, 0, nowTime, nowTime);
        }
        if ("上海".equals(cityName)) {
            return new CityEntity(ownerUserId, "上海", "101020100", "上海", "中国", 31.2304, 121.4737, isDefault, 1, nowTime, nowTime);
        }
        if ("广州".equals(cityName)) {
            return new CityEntity(ownerUserId, "广州", "101280101", "广东", "中国", 23.1291, 113.2644, isDefault, 2, nowTime, nowTime);
        }
        if ("深圳".equals(cityName)) {
            return new CityEntity(ownerUserId, "深圳", "101280601", "广东", "中国", 22.5431, 114.0579, isDefault, 3, nowTime, nowTime);
        }
        return null;
    }

    private String requireText(String value, String fieldName) throws IOException {
        if (value == null || value.trim().isEmpty()) {
            throw new IOException("城市搜索接口缺少字段：" + fieldName);
        }
        return value;
    }

    private double parseCoordinate(String value, String fieldName) throws IOException {
        try {
            return Double.parseDouble(requireText(value, fieldName));
        } catch (NumberFormatException exception) {
            throw new IOException("城市搜索接口坐标格式错误：" + fieldName, exception);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdownNow();
    }
}
