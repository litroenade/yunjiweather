package com.litroenade.yunjiweather.ui.index;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.litroenade.yunjiweather.data.api.WeatherGatewayFactory;
import com.litroenade.yunjiweather.data.api.WeatherApiService;
import com.litroenade.yunjiweather.data.api.model.QWeatherIndicesResponse;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.data.local.CityDao;
import com.litroenade.yunjiweather.data.local.LifeIndexCacheGateway;
import com.litroenade.yunjiweather.utils.DateTimeUtils;
import com.litroenade.yunjiweather.utils.DefaultCityUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class LifeIndexViewModel extends AndroidViewModel {

    private static final String SUCCESS_CODE = "200";
    private static final String ALL_INDEX_TYPES = "0";
    private static final long INDEX_CACHE_TTL_MILLIS = 6L * 60L * 60L * 1000L;

    private final CityDao cityDao;
    private final WeatherApiService apiService;
    private final LifeIndexCacheGateway cacheGateway;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<LifeIndexItem>> indexItems = new MutableLiveData<>();
    private final MutableLiveData<String> stateText = new MutableLiveData<>();

    public LifeIndexViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        cityDao = database.cityDao();
        apiService = WeatherGatewayFactory.createQWeatherServiceOrNull();
        cacheGateway = new LifeIndexCacheGateway(database.weatherCacheDao(), new Gson());
        refresh();
    }

    public LiveData<List<LifeIndexItem>> getIndexItems() {
        return indexItems;
    }

    public LiveData<String> getStateText() {
        return stateText;
    }

    public void refresh() {
        executorService.execute(() -> {
            CityEntity city = DefaultCityUtils.resolveDefaultCity(cityDao, System.currentTimeMillis());
            long nowTime = System.currentTimeMillis();
            if (apiService == null) {
                if (renderCacheIfAvailable(city, nowTime, "未配置 QWeather API，已显示缓存生活指数。")) {
                    return;
                }
                indexItems.postValue(LifeIndexDefaults.createFallbackItems());
                stateText.postValue("未配置 QWeather API，已显示本地生活建议。");
                return;
            }
            try {
                List<LifeIndexItem> remoteItems = fetchRemoteItems(city.locationId);
                cacheGateway.save(city.locationId, city.cityName, remoteItems, nowTime, nowTime + INDEX_CACHE_TTL_MILLIS);
                indexItems.postValue(remoteItems);
                stateText.postValue("已更新 " + city.cityName + " 今日生活指数。");
            } catch (IOException | RuntimeException exception) {
                if (renderCacheIfAvailable(city, nowTime, "生活指数刷新失败，已显示缓存生活指数。")) {
                    return;
                }
                indexItems.postValue(LifeIndexDefaults.createFallbackItems());
                stateText.postValue("生活指数刷新失败，已显示本地建议。原因：" + exception.getMessage());
            }
        });
    }

    private boolean renderCacheIfAvailable(CityEntity city, long nowTime, String message) {
        LifeIndexCacheGateway.CacheRecord cacheRecord = cacheGateway.readValid(city.locationId, nowTime);
        if (cacheRecord == null) {
            return false;
        }
        indexItems.postValue(cacheRecord.getItems());
        stateText.postValue(message + " " + DateTimeUtils.formatCacheUpdateTime(cacheRecord.getUpdateTime()));
        return true;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdownNow();
    }

    private List<LifeIndexItem> fetchRemoteItems(String locationId) throws IOException {
        Response<QWeatherIndicesResponse> response = apiService.getLifeIndices(locationId, ALL_INDEX_TYPES, "zh").execute();
        QWeatherIndicesResponse body = response.body();
        if (!response.isSuccessful() || body == null || !SUCCESS_CODE.equals(body.code)) {
            throw new IOException("生活指数接口请求失败");
        }
        return LifeIndexDefaults.completeWithFallbacks(LifeIndexMapper.mapDailyIndices(body.daily));
    }
}
