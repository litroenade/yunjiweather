package com.litroenade.yunjiweather.ui.index;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.litroenade.yunjiweather.auth.AuthSessionManager;
import com.litroenade.yunjiweather.data.api.WeatherGatewayFactory;
import com.litroenade.yunjiweather.data.api.WeatherApiService;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.data.local.LifeIndexCacheGateway;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.LifeIndexRepository;
import com.litroenade.yunjiweather.utils.DateTimeUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LifeIndexViewModel extends AndroidViewModel {

    private final CityRepository cityRepository;
    private final LifeIndexRepository lifeIndexRepository;
    private final long ownerUserId;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<LifeIndexItem>> indexItems = new MutableLiveData<>();
    private final MutableLiveData<String> stateText = new MutableLiveData<>();

    public LifeIndexViewModel(@NonNull Application application) {
        super(application);
        ownerUserId = new AuthSessionManager(application).requireUserId();
        AppDatabase database = AppDatabase.getInstance(application);
        cityRepository = new CityRepository(ownerUserId, database.cityDao());
        WeatherApiService apiService = WeatherGatewayFactory.createQWeatherServiceOrNull();
        LifeIndexCacheGateway cacheGateway = new LifeIndexCacheGateway(ownerUserId, database.weatherCacheDao(), new Gson());
        lifeIndexRepository = new LifeIndexRepository(apiService, cacheGateway);
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
            CityEntity city = cityRepository.resolveDefaultCity(System.currentTimeMillis());
            long nowTime = System.currentTimeMillis();
            LifeIndexRepository.LoadResult result = lifeIndexRepository.load(city.locationId, city.cityName, nowTime);
            indexItems.postValue(result.getItems());
            stateText.postValue(createStateText(city.cityName, result));
        });
    }

    private String createStateText(String cityName, LifeIndexRepository.LoadResult result) {
        if (result.getSource() == LifeIndexRepository.LoadSource.REMOTE) {
            return "已更新 " + cityName + " 今日生活指数。";
        }
        if (result.getSource() == LifeIndexRepository.LoadSource.CACHE_NO_API) {
            return "未配置 QWeather API，已显示缓存生活指数。 " + DateTimeUtils.formatCacheUpdateTime(result.getCacheUpdateTime());
        }
        if (result.getSource() == LifeIndexRepository.LoadSource.CACHE_ERROR) {
            return "生活指数刷新失败，已显示缓存生活指数。 " + DateTimeUtils.formatCacheUpdateTime(result.getCacheUpdateTime());
        }
        if (result.getErrorMessage() == null || result.getErrorMessage().trim().isEmpty()) {
            return "未配置 QWeather API，已显示本地生活建议。";
        }
        return "生活指数刷新失败，已显示本地建议。原因：" + result.getErrorMessage();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdownNow();
    }
}
