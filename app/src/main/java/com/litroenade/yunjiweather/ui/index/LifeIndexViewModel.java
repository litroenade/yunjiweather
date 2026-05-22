package com.litroenade.yunjiweather.ui.index;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.litroenade.yunjiweather.auth.AuthSessionManager;
import com.litroenade.yunjiweather.data.api.WeatherApiService;
import com.litroenade.yunjiweather.data.api.WeatherGatewayFactory;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.data.local.LifeIndexCacheGateway;
import com.litroenade.yunjiweather.data.model.LifeIndexDefaults;
import com.litroenade.yunjiweather.data.model.LifeIndexItem;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.LifeIndexRepository;
import com.litroenade.yunjiweather.utils.DateTimeUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LifeIndexViewModel extends AndroidViewModel {

    private CityRepository cityRepository;
    private LifeIndexRepository lifeIndexRepository;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<LifeIndexItem>> indexItems = new MutableLiveData<>();
    private final MutableLiveData<String> stateText = new MutableLiveData<>();

    public LifeIndexViewModel(@NonNull Application application) {
        super(application);
        if (initializeDependencies()) {
            refresh();
        }
    }

    public LiveData<List<LifeIndexItem>> getIndexItems() {
        return indexItems;
    }

    public LiveData<String> getStateText() {
        return stateText;
    }

    public void refresh() {
        if (cityRepository == null || lifeIndexRepository == null) {
            publishLocalFallback("生活服务初始化失败，请重新登录后再试。");
            return;
        }
        executorService.execute(() -> {
            try {
                CityEntity city = cityRepository.resolveDefaultCity(System.currentTimeMillis());
                long nowTime = System.currentTimeMillis();
                LifeIndexRepository.LoadResult result = lifeIndexRepository.load(city.locationId, city.cityName, nowTime);
                indexItems.postValue(result.getItems());
                stateText.postValue(createStateText(city.cityName, result));
            } catch (RuntimeException exception) {
                publishLocalFallback("生活指数加载失败，已显示本地建议。原因：" + readableMessage(exception));
            }
        });
    }

    private boolean initializeDependencies() {
        try {
            Application application = getApplication();
            long ownerUserId = new AuthSessionManager(application).requireUserId();
            AppDatabase database = AppDatabase.getInstance(application);
            WeatherApiService apiService = WeatherGatewayFactory.createQWeatherServiceOrNull();
            cityRepository = new CityRepository(ownerUserId, database.cityDao());
            lifeIndexRepository = new LifeIndexRepository(
                    apiService,
                    new LifeIndexCacheGateway(ownerUserId, database.weatherCacheDao(), new Gson())
            );
            return true;
        } catch (RuntimeException exception) {
            publishLocalFallback("生活服务初始化失败，已显示本地建议。原因：" + readableMessage(exception));
            return false;
        }
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

    private void publishLocalFallback(String message) {
        indexItems.postValue(LifeIndexDefaults.createFallbackItems());
        stateText.postValue(message);
    }

    private String readableMessage(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return exception.getClass().getSimpleName();
        }
        return message;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdownNow();
    }
}
