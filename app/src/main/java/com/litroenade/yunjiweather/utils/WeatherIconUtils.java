package com.litroenade.yunjiweather.utils;

import com.litroenade.yunjiweather.R;

public final class WeatherIconUtils {

    public enum WeatherCategory {
        SUNNY,
        NIGHT,
        RAIN,
        SNOW,
        CLOUDY
    }

    private WeatherIconUtils() {
    }

    public static int getWeatherIconRes(String iconCode) {
        switch (getWeatherCategory(iconCode)) {
            case RAIN:
                return R.drawable.ic_weather_rain;
            case SNOW:
                return R.drawable.ic_weather_snow;
            case SUNNY:
            case NIGHT:
                return R.drawable.ic_weather_sunny;
            case CLOUDY:
            default:
                return R.drawable.ic_weather_cloudy;
        }
    }

    public static WeatherCategory getWeatherCategory(String iconCode) {
        if (iconCode == null || iconCode.trim().isEmpty()) {
            return WeatherCategory.CLOUDY;
        }
        if (iconCode.startsWith("3")) {
            return WeatherCategory.RAIN;
        }
        if (iconCode.startsWith("4")) {
            return WeatherCategory.SNOW;
        }
        if (isNightCode(iconCode)) {
            return WeatherCategory.NIGHT;
        }
        if ("100".equals(iconCode) || "150".equals(iconCode)) {
            return WeatherCategory.SUNNY;
        }
        return WeatherCategory.CLOUDY;
    }

    private static boolean isNightCode(String iconCode) {
        return iconCode.startsWith("15");
    }
}
