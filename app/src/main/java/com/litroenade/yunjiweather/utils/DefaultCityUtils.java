package com.litroenade.yunjiweather.utils;

import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.local.CityDao;

import java.util.Objects;

public final class DefaultCityUtils {

    public static final String DEFAULT_CITY_NAME = "北京";
    public static final String DEFAULT_LOCATION_ID = "101010100";

    private DefaultCityUtils() {
    }

    public static CityEntity resolveDefaultCity(CityDao cityDao, long nowTime) {
        Objects.requireNonNull(cityDao, "cityDao");
        CityEntity defaultCity = cityDao.findDefaultCity();
        if (defaultCity != null) {
            return defaultCity;
        }
        CityEntity seedCity = createDefaultCity(nowTime);
        cityDao.insert(seedCity);
        return seedCity;
    }

    public static CityEntity createDefaultCity(long nowTime) {
        return new CityEntity(
                DEFAULT_CITY_NAME,
                DEFAULT_LOCATION_ID,
                "北京",
                "中国",
                39.9042,
                116.4074,
                true,
                0,
                nowTime,
                nowTime
        );
    }

    public static String formatDefaultCityText(CityEntity city) {
        Objects.requireNonNull(city, "city");
        return "默认城市：" + city.cityName;
    }
}
