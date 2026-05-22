package com.litroenade.yunjiweather.ui.home;

import com.litroenade.yunjiweather.utils.WeatherIconUtils;

public enum WeatherAnimationType {
    SUNNY,
    CLOUDY,
    RAIN,
    SNOW;

    public static WeatherAnimationType fromIconCode(String iconCode) {
        switch (WeatherIconUtils.getWeatherCategory(iconCode)) {
            case SUNNY:
            case NIGHT:
                return SUNNY;
            case RAIN:
                return RAIN;
            case SNOW:
                return SNOW;
            case CLOUDY:
            default:
                return CLOUDY;
        }
    }
}
