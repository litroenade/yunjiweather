package com.litroenade.yunjiweather.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.litroenade.yunjiweather.utils.HomeBlock;
import com.litroenade.yunjiweather.utils.VisualThemeUtils;
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Owns device-local preferences and repairs old values at read time. Preference keys are
 * treated as compatibility data because older app versions may have written them already.
 */
public final class SettingsManager {

    private static final String PREF_NAME = "yunji_weather_settings";
    private static final String KEY_WARNING_ENABLED = "warning_enabled";
    private static final String KEY_ANIMATION_ENABLED = "animation_enabled";
    private static final String KEY_DARK_MODE_ENABLED = "dark_mode_enabled";
    private static final String KEY_TEMPERATURE_UNIT = "temperature_unit";
    private static final String KEY_WIND_UNIT = "wind_unit";
    private static final String KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled";
    private static final String KEY_VISUAL_THEME = "visual_theme";
    private static final String KEY_DEVELOPER_TOOLS_ENABLED = "developer_tools_enabled";
    private static final String KEY_HOME_BLOCK_ORDER_PREFIX = "home_block_order_";
    private static final String KEY_HOME_BLOCK_DISABLED_PREFIX = "home_block_disabled_";

    private final SharedPreferences preferences;

    public SettingsManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        ensureDefaultSettings();
    }

    public SettingsManager(SharedPreferences preferences) {
        this.preferences = preferences;
        ensureDefaultSettings();
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

    public boolean isDeveloperToolsEnabled() {
        return preferences.getBoolean(KEY_DEVELOPER_TOOLS_ENABLED, false);
    }

    public void setDeveloperToolsEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_DEVELOPER_TOOLS_ENABLED, enabled).apply();
    }

    public String getTemperatureUnit() {
        String unit = getStringSetting(KEY_TEMPERATURE_UNIT, WeatherDisplayUtils.TEMPERATURE_CELSIUS);
        String normalizedUnit = WeatherDisplayUtils.normalizeTemperatureUnit(unit);
        repairStringIfNeeded(KEY_TEMPERATURE_UNIT, unit, normalizedUnit);
        return normalizedUnit;
    }

    public void setTemperatureUnit(String unit) {
        validateTemperatureUnit(unit);
        preferences.edit().putString(KEY_TEMPERATURE_UNIT, unit).apply();
    }

    public String getWindUnit() {
        String unit = getStringSetting(KEY_WIND_UNIT, WeatherDisplayUtils.WIND_SCALE);
        String normalizedUnit = WeatherDisplayUtils.normalizeWindUnit(unit);
        repairStringIfNeeded(KEY_WIND_UNIT, unit, normalizedUnit);
        return normalizedUnit;
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

    public String getVisualTheme() {
        String themeKey = getStringSetting(KEY_VISUAL_THEME, VisualThemeUtils.THEME_SKY);
        String normalizedThemeKey = VisualThemeUtils.normalizeThemeKey(themeKey);
        repairStringIfNeeded(KEY_VISUAL_THEME, themeKey, normalizedThemeKey);
        return normalizedThemeKey;
    }

    public void setVisualTheme(String themeKey) {
        VisualThemeUtils.validateThemeKey(themeKey);
        preferences.edit().putString(KEY_VISUAL_THEME, themeKey).apply();
    }

    public List<HomeBlock> getHomeBlockOrder(String themeKey) {
        String normalizedThemeKey = VisualThemeUtils.normalizeThemeKey(themeKey);
        String preferenceKey = homeBlockOrderKey(normalizedThemeKey);
        String encodedOrder = getStringSetting(preferenceKey, "");
        List<HomeBlock> decodedOrder = decodeHomeBlockOrder(encodedOrder);
        String repairedOrder = encodeHomeBlockOrder(decodedOrder);
        repairStringIfNeeded(preferenceKey, encodedOrder, repairedOrder);
        return decodedOrder;
    }

    public boolean isHomeBlockEnabled(String themeKey, HomeBlock block) {
        return !getDisabledHomeBlocks(themeKey).contains(block);
    }

    public void setHomeBlockEnabled(String themeKey, HomeBlock block, boolean enabled) {
        String normalizedThemeKey = VisualThemeUtils.normalizeThemeKey(themeKey);
        Set<HomeBlock> disabledBlocks = getDisabledHomeBlocks(normalizedThemeKey);
        if (enabled) {
            disabledBlocks.remove(block);
        } else {
            disabledBlocks.add(block);
        }
        preferences.edit()
                .putString(homeBlockDisabledKey(normalizedThemeKey), encodeHomeBlockSet(disabledBlocks))
                .apply();
    }

    public void moveHomeBlock(String themeKey, HomeBlock block, int direction) {
        if (direction == 0) {
            return;
        }
        String normalizedThemeKey = VisualThemeUtils.normalizeThemeKey(themeKey);
        List<HomeBlock> order = new ArrayList<>(getHomeBlockOrder(normalizedThemeKey));
        int currentIndex = order.indexOf(block);
        if (currentIndex < 0) {
            return;
        }
        int targetIndex = Math.max(0, Math.min(order.size() - 1, currentIndex + direction));
        if (targetIndex == currentIndex) {
            return;
        }
        order.remove(currentIndex);
        order.add(targetIndex, block);
        preferences.edit()
                .putString(homeBlockOrderKey(normalizedThemeKey), encodeHomeBlockOrder(order))
                .apply();
    }

    public void resetHomeBlockLayout(String themeKey) {
        String normalizedThemeKey = VisualThemeUtils.normalizeThemeKey(themeKey);
        preferences.edit()
                .remove(homeBlockOrderKey(normalizedThemeKey))
                .remove(homeBlockDisabledKey(normalizedThemeKey))
                .apply();
    }

    private void ensureDefaultSettings() {
        SharedPreferences.Editor editor = preferences.edit();
        boolean changed = false;
        if (!preferences.contains(KEY_WARNING_ENABLED)) {
            editor.putBoolean(KEY_WARNING_ENABLED, true);
            changed = true;
        }
        if (!preferences.contains(KEY_ANIMATION_ENABLED)) {
            editor.putBoolean(KEY_ANIMATION_ENABLED, true);
            changed = true;
        }
        if (!preferences.contains(KEY_DARK_MODE_ENABLED)) {
            editor.putBoolean(KEY_DARK_MODE_ENABLED, false);
            changed = true;
        }
        if (!preferences.contains(KEY_TEMPERATURE_UNIT)) {
            editor.putString(KEY_TEMPERATURE_UNIT, WeatherDisplayUtils.TEMPERATURE_CELSIUS);
            changed = true;
        }
        if (!preferences.contains(KEY_WIND_UNIT)) {
            editor.putString(KEY_WIND_UNIT, WeatherDisplayUtils.WIND_SCALE);
            changed = true;
        }
        if (!preferences.contains(KEY_DAILY_REMINDER_ENABLED)) {
            editor.putBoolean(KEY_DAILY_REMINDER_ENABLED, false);
            changed = true;
        }
        if (!preferences.contains(KEY_VISUAL_THEME)) {
            editor.putString(KEY_VISUAL_THEME, VisualThemeUtils.THEME_SKY);
            changed = true;
        }
        if (!preferences.contains(KEY_DEVELOPER_TOOLS_ENABLED)) {
            editor.putBoolean(KEY_DEVELOPER_TOOLS_ENABLED, false);
            changed = true;
        }
        if (changed) {
            editor.apply();
        }
    }

    private static void validateTemperatureUnit(String unit) {
        if (!WeatherDisplayUtils.TEMPERATURE_CELSIUS.equals(unit)
                && !WeatherDisplayUtils.TEMPERATURE_FAHRENHEIT.equals(unit)) {
            throw new IllegalArgumentException("不支持的温度单位：" + unit);
        }
    }

    private static void validateWindUnit(String unit) {
        if (!WeatherDisplayUtils.WIND_SCALE.equals(unit)
                && !WeatherDisplayUtils.WIND_METER_PER_SECOND.equals(unit)) {
            throw new IllegalArgumentException("不支持的风速单位：" + unit);
        }
    }

    private String getStringSetting(String key, String defaultValue) {
        try {
            return preferences.getString(key, defaultValue);
        } catch (ClassCastException exception) {
            preferences.edit().putString(key, defaultValue).apply();
            return defaultValue;
        }
    }

    private void repairStringIfNeeded(String key, String currentValue, String normalizedValue) {
        if (!normalizedValue.equals(currentValue)) {
            preferences.edit().putString(key, normalizedValue).apply();
        }
    }

    private static String homeBlockOrderKey(String themeKey) {
        return KEY_HOME_BLOCK_ORDER_PREFIX + themeKey;
    }

    private static String homeBlockDisabledKey(String themeKey) {
        return KEY_HOME_BLOCK_DISABLED_PREFIX + themeKey;
    }

    private Set<HomeBlock> getDisabledHomeBlocks(String themeKey) {
        String normalizedThemeKey = VisualThemeUtils.normalizeThemeKey(themeKey);
        String preferenceKey = homeBlockDisabledKey(normalizedThemeKey);
        String encodedBlocks = getStringSetting(preferenceKey, "");
        Set<HomeBlock> disabledBlocks = decodeHomeBlockSet(encodedBlocks);
        String repairedBlocks = encodeHomeBlockSet(disabledBlocks);
        repairStringIfNeeded(preferenceKey, encodedBlocks, repairedBlocks);
        return disabledBlocks;
    }

    private static List<HomeBlock> decodeHomeBlockOrder(String encodedOrder) {
        List<HomeBlock> result = new ArrayList<>();
        Set<HomeBlock> seen = new HashSet<>();
        if (encodedOrder != null && !encodedOrder.trim().isEmpty()) {
            String[] keys = encodedOrder.split(",");
            for (String rawKey : keys) {
                HomeBlock block = HomeBlock.fromKey(rawKey.trim());
                if (block != null && seen.add(block)) {
                    result.add(block);
                }
            }
        }
        for (HomeBlock block : HomeBlock.defaultOrder()) {
            if (seen.add(block)) {
                result.add(block);
            }
        }
        return result;
    }

    private static Set<HomeBlock> decodeHomeBlockSet(String encodedBlocks) {
        Set<HomeBlock> result = new HashSet<>();
        if (encodedBlocks == null || encodedBlocks.trim().isEmpty()) {
            return result;
        }
        String[] keys = encodedBlocks.split(",");
        for (String rawKey : keys) {
            HomeBlock block = HomeBlock.fromKey(rawKey.trim());
            if (block != null) {
                result.add(block);
            }
        }
        return result;
    }

    private static String encodeHomeBlockOrder(List<HomeBlock> order) {
        StringBuilder builder = new StringBuilder();
        for (HomeBlock block : order) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(block.getKey());
        }
        return builder.toString();
    }

    private static String encodeHomeBlockSet(Set<HomeBlock> blocks) {
        StringBuilder builder = new StringBuilder();
        for (HomeBlock block : HomeBlock.defaultOrder()) {
            if (blocks.contains(block)) {
                if (builder.length() > 0) {
                    builder.append(',');
                }
                builder.append(block.getKey());
            }
        }
        return builder.toString();
    }

}
