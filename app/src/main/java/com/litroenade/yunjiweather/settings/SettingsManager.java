package com.litroenade.yunjiweather.settings;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.litroenade.yunjiweather.auth.AuthSessionManager;
import com.litroenade.yunjiweather.utils.CityListUtils;
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils;

public final class SettingsManager {

    private static final String PREF_NAME_PREFIX = "yunji_weather_settings_user_";
    private static final String KEY_CITIES = "cities";
    private static final String KEY_DEFAULT_CITY = "default_city";
    private static final String KEY_WARNING_ENABLED = "warning_enabled";
    private static final String KEY_ANIMATION_ENABLED = "animation_enabled";
    private static final String KEY_DARK_MODE_ENABLED = "dark_mode_enabled";
    private static final String KEY_TEMPERATURE_UNIT = "temperature_unit";
    private static final String KEY_WIND_UNIT = "wind_unit";
    private static final String KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled";
    private static final String DEFAULT_CITY = "北京";

    private final SharedPreferences preferences;

    public SettingsManager(Context context) {
        long userId = new AuthSessionManager(context).requireUserId();
        preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME_PREFIX + userId, Context.MODE_PRIVATE);
        ensureDefaultCity();
    }

    public List<String> getCities() {
        String rawCities = preferences.getString(KEY_CITIES, DEFAULT_CITY);
        if (rawCities == null || rawCities.trim().isEmpty()) {
            return Collections.singletonList(DEFAULT_CITY);
        }
        return CityListUtils.normalizeCities(Arrays.asList(rawCities.split(",")));
    }

    public boolean addCity(String cityName) {
        CityListUtils.AddResult result = CityListUtils.addCity(getCities(), cityName);
        saveCities(result.getCities());
        return result.isAdded();
    }

    public void removeCity(String cityName) {
        String normalized = CityListUtils.normalizeCity(cityName);
        List<String> cities = getCities();
        cities.remove(normalized);
        if (cities.isEmpty()) {
            cities.add(DEFAULT_CITY);
        }
        saveCities(cities);
        preferences.edit()
                .putString(KEY_DEFAULT_CITY, CityListUtils.resolveDefaultAfterRemove(cities, getDefaultCity(), normalized))
                .apply();
    }

    public String getDefaultCity() {
        return preferences.getString(KEY_DEFAULT_CITY, DEFAULT_CITY);
    }

    public void setDefaultCity(String cityName) {
        String normalized = CityListUtils.normalizeCity(cityName);
        if (!getCities().contains(normalized)) {
            addCity(normalized);
        }
        preferences.edit().putString(KEY_DEFAULT_CITY, normalized).apply();
    }

    public boolean isWarningEnabled() {
        return preferences.getBoolean(KEY_WARNING_ENABLED, true);
    }

    public void setWarningEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_WARNING_ENABLED, enabled).apply();
    }

    public boolean isAnimationEnabled() {
        return preferences.getBoolean(KEY_ANIMATION_ENABLED, true);
    }

    public void setAnimationEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_ANIMATION_ENABLED, enabled).apply();
    }

    public boolean isDarkModeEnabled() {
        return preferences.getBoolean(KEY_DARK_MODE_ENABLED, false);
    }

    public void setDarkModeEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_DARK_MODE_ENABLED, enabled).apply();
    }

    public String getTemperatureUnit() {
        String unit = preferences.getString(KEY_TEMPERATURE_UNIT, WeatherDisplayUtils.TEMPERATURE_CELSIUS);
        validateTemperatureUnit(unit);
        return unit;
    }

    public void setTemperatureUnit(String unit) {
        validateTemperatureUnit(unit);
        preferences.edit().putString(KEY_TEMPERATURE_UNIT, unit).apply();
    }

    public String getWindUnit() {
        String unit = preferences.getString(KEY_WIND_UNIT, WeatherDisplayUtils.WIND_SCALE);
        validateWindUnit(unit);
        return unit;
    }

    public void setWindUnit(String unit) {
        validateWindUnit(unit);
        preferences.edit().putString(KEY_WIND_UNIT, unit).apply();
    }

    public boolean isDailyReminderEnabled() {
        return preferences.getBoolean(KEY_DAILY_REMINDER_ENABLED, false);
    }

    public void setDailyReminderEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_DAILY_REMINDER_ENABLED, enabled).apply();
    }

    public void clearWeatherPreferences() {
        preferences.edit()
                .putString(KEY_CITIES, DEFAULT_CITY)
                .putString(KEY_DEFAULT_CITY, DEFAULT_CITY)
                .apply();
    }

    private void ensureDefaultCity() {
        if (!preferences.contains(KEY_CITIES)) {
            preferences.edit()
                    .putString(KEY_CITIES, DEFAULT_CITY)
                    .putString(KEY_DEFAULT_CITY, DEFAULT_CITY)
                    .putBoolean(KEY_WARNING_ENABLED, true)
                    .putBoolean(KEY_ANIMATION_ENABLED, true)
                    .putBoolean(KEY_DARK_MODE_ENABLED, false)
                    .putString(KEY_TEMPERATURE_UNIT, WeatherDisplayUtils.TEMPERATURE_CELSIUS)
                    .putString(KEY_WIND_UNIT, WeatherDisplayUtils.WIND_SCALE)
                    .putBoolean(KEY_DAILY_REMINDER_ENABLED, false)
                    .apply();
        }
    }

    private static void validateTemperatureUnit(String unit) {
        if (!WeatherDisplayUtils.TEMPERATURE_CELSIUS.equals(unit)
                && !WeatherDisplayUtils.TEMPERATURE_FAHRENHEIT.equals(unit)) {
            throw new IllegalArgumentException("Unsupported temperature unit: " + unit);
        }
    }

    private static void validateWindUnit(String unit) {
        if (!WeatherDisplayUtils.WIND_SCALE.equals(unit)
                && !WeatherDisplayUtils.WIND_METER_PER_SECOND.equals(unit)) {
            throw new IllegalArgumentException("Unsupported wind unit: " + unit);
        }
    }

    private void saveCities(List<String> cities) {
        Set<String> uniqueCities = new LinkedHashSet<>(cities);
        preferences.edit().putString(KEY_CITIES, String.join(",", uniqueCities)).apply();
    }
}
