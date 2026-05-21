package com.litroenade.yunjiweather.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.litroenade.yunjiweather.auth.AuthSessionManager;
import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.api.WeatherGatewayFactory;
import com.litroenade.yunjiweather.data.api.WeatherApiService;
import com.litroenade.yunjiweather.data.api.model.QWeatherCityLookupResponse;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.WeatherRepository;
import com.litroenade.yunjiweather.data.repository.WeatherRepositoryFactory;
import com.litroenade.yunjiweather.utils.LocationQueryUtils;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class HomeViewModel extends AndroidViewModel {

    private static final String SUCCESS_CODE = "200";

    private final MutableLiveData<UiState<HomeWeatherData>> uiState = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final WeatherRepository weatherRepository;
    private final WeatherApiService apiService;
    private final CityRepository cityRepository;
    private final long ownerUserId;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        ownerUserId = new AuthSessionManager(application).requireUserId();
        AppDatabase database = AppDatabase.getInstance(application);
        cityRepository = new CityRepository(ownerUserId, database.cityDao());
        apiService = WeatherGatewayFactory.createQWeatherServiceOrNull();
        weatherRepository = WeatherRepositoryFactory.createHomeRepository(ownerUserId, database, apiService);
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
        if (apiService == null) {
            return createCoordinateCity(latitude, longitude);
        }
        String locationQuery = LocationQueryUtils.formatQWeatherLocationQuery(latitude, longitude);
        Response<QWeatherCityLookupResponse> response = apiService.searchCity(locationQuery, "cn", 1, "zh").execute();
        QWeatherCityLookupResponse body = response.body();
        if (!response.isSuccessful() || body == null || !SUCCESS_CODE.equals(body.code)) {
            throw new IOException("城市反查接口请求失败");
        }
        if (body.location == null || body.location.isEmpty()) {
            throw new IOException("未找到当前位置对应城市");
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
                true,
                0,
                nowTime,
                nowTime
        );
    }

    private CityEntity createCoordinateCity(double latitude, double longitude) {
        long nowTime = System.currentTimeMillis();
        return new CityEntity(
                ownerUserId,
                "当前位置",
                String.format(Locale.US, "openmeteo:%.4f,%.4f", latitude, longitude),
                "定位坐标",
                "GPS",
                latitude,
                longitude,
                true,
                0,
                nowTime,
                nowTime
        );
    }

    private void saveAsDefaultCity(CityEntity city) {
        cityRepository.saveAsDefaultCity(city, System.currentTimeMillis());
    }

    private String requireText(String value, String fieldName) throws IOException {
        if (value == null || value.trim().isEmpty()) {
            throw new IOException("城市反查接口缺少字段：" + fieldName);
        }
        return value;
    }

    private double parseCoordinate(String value, String fieldName) throws IOException {
        try {
            return Double.parseDouble(requireText(value, fieldName));
        } catch (NumberFormatException exception) {
            throw new IOException("城市反查接口坐标格式错误：" + fieldName, exception);
        }
    }

    private String buildLoadErrorMessage(RuntimeException exception) {
        String detail = exception.getMessage();
        if (detail == null || detail.trim().isEmpty()) {
            detail = exception.getClass().getSimpleName();
        }
        return "首页天气加载失败：" + detail;
    }
}
