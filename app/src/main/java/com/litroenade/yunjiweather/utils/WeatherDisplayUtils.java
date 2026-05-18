package com.litroenade.yunjiweather.utils;

import java.util.Locale;
import java.util.Objects;

public final class WeatherDisplayUtils {

    public static final String TEMPERATURE_CELSIUS = "C";
    public static final String TEMPERATURE_FAHRENHEIT = "F";
    public static final String WIND_SCALE = "SCALE";
    public static final String WIND_METER_PER_SECOND = "MS";

    private WeatherDisplayUtils() {
    }

    public static String formatTemperature(String celsiusValue, String unit) {
        double celsius = parseRequiredDouble(celsiusValue, "celsiusValue");
        if (TEMPERATURE_FAHRENHEIT.equals(unit)) {
            long fahrenheit = Math.round(celsius * 9.0d / 5.0d + 32.0d);
            return fahrenheit + "°F";
        }
        if (!TEMPERATURE_CELSIUS.equals(unit)) {
            throw new IllegalArgumentException("Unsupported temperature unit: " + unit);
        }
        return Math.round(celsius) + "°";
    }

    public static String formatWind(String windDirection, String windScale, String windSpeedKmh, String unit) {
        String direction = requireText(windDirection, "windDirection");
        if (WIND_METER_PER_SECOND.equals(unit)) {
            double speedMs = parseRequiredDouble(windSpeedKmh, "windSpeedKmh") / 3.6d;
            return direction + " " + String.format(Locale.CHINA, "%.1f m/s", speedMs);
        }
        if (!WIND_SCALE.equals(unit)) {
            throw new IllegalArgumentException("Unsupported wind unit: " + unit);
        }
        return direction + " " + requireText(windScale, "windScale") + "级";
    }

    private static double parseRequiredDouble(String value, String fieldName) {
        String text = requireText(value, fieldName);
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " must be numeric", exception);
        }
    }

    private static String requireText(String value, String fieldName) {
        String text = Objects.requireNonNull(value, fieldName);
        if (text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }
        return text;
    }
}
