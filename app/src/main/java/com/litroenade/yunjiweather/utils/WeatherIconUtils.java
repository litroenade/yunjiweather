package com.litroenade.yunjiweather.utils;

import com.litroenade.yunjiweather.R;

public final class WeatherIconUtils {

    private WeatherIconUtils() {
    }

    public static int getWeatherIconRes(String iconCode) {
        if (iconCode == null || iconCode.trim().isEmpty()) {
            return R.drawable.ic_weather_cloudy;
        }
        if (iconCode.startsWith("3")) {
            return R.drawable.ic_weather_rain;
        }
        if (iconCode.startsWith("4")) {
            return R.drawable.ic_weather_snow;
        }
        if ("100".equals(iconCode) || "150".equals(iconCode)) {
            return R.drawable.ic_weather_sunny;
        }
        return R.drawable.ic_weather_cloudy;
    }

    public static int getWeatherBackgroundRes(String iconCode) {
        if (iconCode == null || iconCode.trim().isEmpty()) {
            return R.drawable.bg_weather_cloudy;
        }
        if (iconCode.startsWith("3")) {
            return R.drawable.bg_weather_rain;
        }
        if (iconCode.startsWith("4")) {
            return R.drawable.bg_weather_snow;
        }
        if ("100".equals(iconCode) || "150".equals(iconCode)) {
            return R.drawable.bg_weather_sunny;
        }
        return R.drawable.bg_weather_cloudy;
    }
}
