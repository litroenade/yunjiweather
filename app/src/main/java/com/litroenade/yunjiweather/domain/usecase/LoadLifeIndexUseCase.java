package com.litroenade.yunjiweather.domain.usecase;

import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.HomeWeatherCacheSource;
import com.litroenade.yunjiweather.data.repository.LifeIndexRepository;
import com.litroenade.yunjiweather.utils.DateTimeUtils;

/**
 * 生活建议页面的用例入口。
 * 视图模型只关心结果状态，默认城市、天气缓存和建议来源由这里统一协调。
 */
public final class LoadLifeIndexUseCase {

    private final CityRepository cityRepository;
    private final LifeIndexRepository lifeIndexRepository;
    private final HomeWeatherCacheSource homeWeatherCacheSource;

    public LoadLifeIndexUseCase(
            CityRepository cityRepository,
            LifeIndexRepository lifeIndexRepository,
            HomeWeatherCacheSource homeWeatherCacheSource
    ) {
        this.cityRepository = cityRepository;
        this.lifeIndexRepository = lifeIndexRepository;
        this.homeWeatherCacheSource = homeWeatherCacheSource;
    }

    public Result execute(long nowTime) {
        CityEntity city = cityRepository.resolveDefaultCity(nowTime);
        UiState<HomeWeatherData> cachedWeather = homeWeatherCacheSource.loadCachedHomeWeather(city.locationId);
        HomeWeatherData weatherData = cachedWeather == null ? null : cachedWeather.getData();
        LifeIndexRepository.LoadResult result = lifeIndexRepository.load(
                city.locationId,
                city.cityName,
                weatherData,
                nowTime
        );
        return new Result(city, result, createStateText(city.cityName, result));
    }

    private String createStateText(String cityName, LifeIndexRepository.LoadResult result) {
        if (result.getSource() == LifeIndexRepository.LoadSource.CACHE) {
            return cityName + " 生活建议缓存 " + DateTimeUtils.formatCacheUpdateTime(result.getCacheUpdateTime());
        }
        if (result.getSource() == LifeIndexRepository.LoadSource.LOCAL_WEATHER) {
            return cityName + " 根据本地天气生成生活建议";
        }
        return cityName + " 使用通用本地生活建议";
    }

    public static final class Result {
        private final CityEntity city;
        private final LifeIndexRepository.LoadResult loadResult;
        private final String stateText;

        private Result(CityEntity city, LifeIndexRepository.LoadResult loadResult, String stateText) {
            this.city = city;
            this.loadResult = loadResult;
            this.stateText = stateText;
        }

        public CityEntity getCity() {
            return city;
        }

        public LifeIndexRepository.LoadResult getLoadResult() {
            return loadResult;
        }

        public String getStateText() {
            return stateText;
        }
    }
}
