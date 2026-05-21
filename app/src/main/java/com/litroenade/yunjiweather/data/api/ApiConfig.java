package com.litroenade.yunjiweather.data.api;

import com.litroenade.yunjiweather.BuildConfig;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public final class ApiConfig {

    private ApiConfig() {
    }

    public static String getQWeatherBaseUrl() {
        return normalizeQWeatherBaseUrl(BuildConfig.QWEATHER_API_HOST);
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

    static String normalizeQWeatherBaseUrl(String rawHost) {
        if (rawHost == null) {
            return "";
        }
        String host = rawHost.trim();
        if (host.isEmpty()) {
            return "";
        }
        String lowerHost = host.toLowerCase(Locale.US);
        if (host.contains("://") && !lowerHost.startsWith("https://") && !lowerHost.startsWith("http://")) {
            return "";
        }
        String candidate = lowerHost.startsWith("https://") || lowerHost.startsWith("http://")
                ? host
                : "https://" + host;
        String normalized = ensureTrailingSlash(candidate);
        try {
            URI uri = new URI(normalized);
            String scheme = uri.getScheme();
            String authority = uri.getAuthority();
            String parsedHost = uri.getHost();
            if (scheme == null
                    || authority == null
                    || authority.trim().isEmpty()
                    || parsedHost == null
                    || parsedHost.trim().isEmpty()) {
                return "";
            }
            String lowerScheme = scheme.toLowerCase(Locale.US);
            if (!"https".equals(lowerScheme) && !"http".equals(lowerScheme)) {
                return "";
            }
            return normalized;
        } catch (URISyntaxException exception) {
            return "";
        }
    }
}
