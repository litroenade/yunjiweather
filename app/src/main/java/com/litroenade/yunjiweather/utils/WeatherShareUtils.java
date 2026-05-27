package com.litroenade.yunjiweather.utils;

import com.litroenade.yunjiweather.data.model.HomeWeatherData;

import java.util.Objects;

public final class WeatherShareUtils {

    private WeatherShareUtils() {
    }

    public static String buildShareText(
            HomeWeatherData data,
            String temperatureUnit,
            String windUnit
    ) {
        Objects.requireNonNull(data, "data");
        return data.getCityName()
                + " "
                + WeatherDisplayUtils.formatTemperature(data.getTemperature(), temperatureUnit)
                + "，"
                + data.getCondition()
                + "，"
                + WeatherDisplayUtils.formatTemperature(data.getTempMax(), temperatureUnit)
                + " / "
                + WeatherDisplayUtils.formatTemperature(data.getTempMin(), temperatureUnit)
                + "，空气"
                + data.getAirQualityCategory()
                + "，"
                + WeatherDisplayUtils.formatWind(
                        data.getWindDir(),
                        data.getWindScale(),
                        data.getWindSpeed(),
                        windUnit
                )
                + "。来自云迹天气。";
    }
}
