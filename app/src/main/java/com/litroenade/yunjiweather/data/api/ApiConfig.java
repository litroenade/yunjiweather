package com.litroenade.yunjiweather.data.api;

import com.litroenade.yunjiweather.BuildConfig;

public final class ApiConfig {

    private ApiConfig() {
    }

    public static String getQWeatherBaseUrl() {
        String host = BuildConfig.QWEATHER_API_HOST.trim();
        if (host.isEmpty()) {
            return "";
        }
        if (host.startsWith("https://") || host.startsWith("http://")) {
            return ensureTrailingSlash(host);
        }
        return ensureTrailingSlash("https://" + host);
    }

    public static String getQWeatherApiKey() {
        return BuildConfig.QWEATHER_API_KEY.trim();
    }

    public static boolean isConfigured() {
        return !getQWeatherBaseUrl().isEmpty() && !getQWeatherApiKey().isEmpty();
    }

    private static String ensureTrailingSlash(String value) {
        return value.endsWith("/") ? value : value + "/";
    }
}
