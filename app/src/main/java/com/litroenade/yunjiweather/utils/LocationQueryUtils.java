package com.litroenade.yunjiweather.utils;

import java.util.Locale;

public final class LocationQueryUtils {

    private LocationQueryUtils() {
    }

    public static String formatQWeatherLocationQuery(double latitude, double longitude) {
        validateLatitude(latitude);
        validateLongitude(longitude);
        return String.format(Locale.US, "%.2f,%.2f", longitude, latitude);
    }

    private static void validateLatitude(double latitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("latitude must be between -90 and 90");
        }
    }

    private static void validateLongitude(double longitude) {
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("longitude must be between -180 and 180");
        }
    }
}
