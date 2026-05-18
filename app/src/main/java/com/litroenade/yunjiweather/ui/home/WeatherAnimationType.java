package com.litroenade.yunjiweather.ui.home;

public enum WeatherAnimationType {
    SUNNY,
    CLOUDY,
    RAIN,
    SNOW;

    public static WeatherAnimationType fromIconCode(String iconCode) {
        if (iconCode == null || iconCode.trim().isEmpty()) {
            return CLOUDY;
        }
        if (iconCode.startsWith("3")) {
            return RAIN;
        }
        if (iconCode.startsWith("4")) {
            return SNOW;
        }
        if ("100".equals(iconCode) || "150".equals(iconCode)) {
            return SUNNY;
        }
        return CLOUDY;
    }
}
