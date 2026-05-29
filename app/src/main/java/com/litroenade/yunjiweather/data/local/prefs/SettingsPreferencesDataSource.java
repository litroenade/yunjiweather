package com.litroenade.yunjiweather.data.local.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.litroenade.yunjiweather.data.model.CustomThemeCropAnchor;
import com.litroenade.yunjiweather.data.model.CustomThemeWeatherKey;
import com.litroenade.yunjiweather.data.repository.SettingsRepository;
import com.litroenade.yunjiweather.utils.HomeBlock;
import com.litroenade.yunjiweather.utils.VisualThemeUtils;
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 设备本地设置的数据源。
 * 读取时顺手修复旧值，保证界面和用例层不需要知道历史偏好存储兼容细节。
 */
public final class SettingsPreferencesDataSource implements SettingsRepository {

    private static final String PREF_NAME = "yunji_weather_settings";
    private static final String KEY_WARNING_ENABLED = "warning_enabled";
    private static final String KEY_ANIMATION_ENABLED = "animation_enabled";
    private static final String KEY_DARK_MODE_ENABLED = "dark_mode_enabled";
    private static final String KEY_TEMPERATURE_UNIT = "temperature_unit";
    private static final String KEY_WIND_UNIT = "wind_unit";
    private static final String KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled";
    private static final String KEY_VISUAL_THEME = "visual_theme";
    private static final String KEY_DEVELOPER_TOOLS_ENABLED = "developer_tools_enabled";
    private static final String KEY_CUSTOM_THEME_IMAGE_URI = "custom_theme_image_uri";
    private static final String KEY_CUSTOM_THEME_CROP_ANCHOR = "custom_theme_crop_anchor";
    private static final String KEY_CUSTOM_THEME_IMAGE_URI_PREFIX = "custom_theme_image_uri_";
    private static final String KEY_CUSTOM_THEME_CROP_ANCHOR_PREFIX = "custom_theme_crop_anchor_";
    private static final String KEY_HOME_BLOCK_ORDER_PREFIX = "home_block_order_";
    private static final String KEY_HOME_BLOCK_DISABLED_PREFIX = "home_block_disabled_";
    private static final String KEY_HOME_MODULE_ORDER_PREFIX = "home_module_order_";
    private static final String KEY_HOME_MODULE_DISABLED_PREFIX = "home_module_disabled_";

    private final SharedPreferences preferences;

    public SettingsPreferencesDataSource(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        ensureDefaultSettings();
    }

    public SettingsPreferencesDataSource(SharedPreferences preferences) {
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

    public String getCustomThemeImageUri() {
        return getCustomThemeImageUri(CustomThemeWeatherKey.FALLBACK);
    }

    public void setCustomThemeImageUri(String imageUri) {
        setCustomThemeImage(CustomThemeWeatherKey.FALLBACK, imageUri, getCustomThemeCropAnchor());
    }

    public String getCustomThemeCropAnchor() {
        String legacyAnchor = getStringSetting(KEY_CUSTOM_THEME_CROP_ANCHOR, CustomThemeCropAnchor.CENTER);
        String normalizedAnchor = getCustomThemeCropAnchor(CustomThemeWeatherKey.FALLBACK);
        repairStringIfNeeded(KEY_CUSTOM_THEME_CROP_ANCHOR, legacyAnchor, normalizedAnchor);
        return normalizedAnchor;
    }

    public void setCustomThemeCropAnchor(String cropAnchor) {
        setCustomThemeImage(CustomThemeWeatherKey.FALLBACK, getCustomThemeImageUri(), cropAnchor);
    }

    public Map<String, String> getCustomThemeImageUris() {
        Map<String, String> result = new HashMap<>();
        for (String weatherKey : CustomThemeWeatherKey.orderedKeys()) {
            String imageUri = getCustomThemeImageUri(weatherKey);
            if (!imageUri.isEmpty()) {
                result.put(weatherKey, imageUri);
            }
        }
        return result;
    }

    public Map<String, String> getCustomThemeCropAnchors() {
        Map<String, String> result = new HashMap<>();
        for (String weatherKey : CustomThemeWeatherKey.orderedKeys()) {
            result.put(weatherKey, getCustomThemeCropAnchor(weatherKey));
        }
        return result;
    }

    public void setCustomThemeImage(String weatherKey, String imageUri, String cropAnchor) {
        String normalizedWeatherKey = CustomThemeWeatherKey.normalize(weatherKey);
        String normalizedImageUri = imageUri == null ? "" : imageUri.trim();
        String normalizedCropAnchor = CustomThemeCropAnchor.normalize(cropAnchor);
        SharedPreferences.Editor editor = preferences.edit()
                .putString(customThemeImageUriKey(normalizedWeatherKey), normalizedImageUri)
                .putString(customThemeCropAnchorKey(normalizedWeatherKey), normalizedCropAnchor);
        if (CustomThemeWeatherKey.FALLBACK.equals(normalizedWeatherKey)) {
            editor.putString(KEY_CUSTOM_THEME_IMAGE_URI, normalizedImageUri)
                    .putString(KEY_CUSTOM_THEME_CROP_ANCHOR, normalizedCropAnchor);
        }
        editor.apply();
    }

    public void clearCustomThemeImage(String weatherKey) {
        String normalizedWeatherKey = CustomThemeWeatherKey.normalize(weatherKey);
        SharedPreferences.Editor editor = preferences.edit()
                .remove(customThemeImageUriKey(normalizedWeatherKey))
                .remove(customThemeCropAnchorKey(normalizedWeatherKey));
        if (CustomThemeWeatherKey.FALLBACK.equals(normalizedWeatherKey)) {
            editor.remove(KEY_CUSTOM_THEME_IMAGE_URI)
                    .remove(KEY_CUSTOM_THEME_CROP_ANCHOR);
        }
        editor.apply();
    }

    public void clearCustomThemeImages() {
        SharedPreferences.Editor editor = preferences.edit()
                .remove(KEY_CUSTOM_THEME_IMAGE_URI)
                .remove(KEY_CUSTOM_THEME_CROP_ANCHOR);
        for (String weatherKey : CustomThemeWeatherKey.orderedKeys()) {
            editor.remove(customThemeImageUriKey(weatherKey))
                    .remove(customThemeCropAnchorKey(weatherKey));
        }
        editor.apply();
    }

    public List<HomeBlock> getHomeBlockOrder(String themeKey) {
        List<String> builtInKeys = homeBlockKeys();
        List<String> moduleKeys = getHomeModuleOrder(themeKey, builtInKeys);
        List<HomeBlock> result = new ArrayList<>();
        for (String moduleKey : moduleKeys) {
            HomeBlock block = HomeBlock.fromKey(moduleKey);
            if (block != null) {
                result.add(block);
            }
        }
        return result;
    }

    public boolean isHomeBlockEnabled(String themeKey, HomeBlock block) {
        return isHomeModuleEnabled(themeKey, block.getKey());
    }

    public void setHomeBlockEnabled(String themeKey, HomeBlock block, boolean enabled) {
        setHomeModuleEnabled(themeKey, block.getKey(), enabled);
    }

    public void moveHomeBlock(String themeKey, HomeBlock block, int direction) {
        moveHomeModule(themeKey, block.getKey(), direction, homeBlockKeys());
    }

    public List<String> getHomeModuleOrder(String themeKey, List<String> availableModuleKeys) {
        String normalizedThemeKey = VisualThemeUtils.normalizeThemeKey(themeKey);
        List<String> sanitizedAvailableKeys = sanitizeModuleKeys(availableModuleKeys);
        String preferenceKey = homeModuleOrderKey(normalizedThemeKey);
        String encodedOrder = getStringSetting(preferenceKey, "");
        if (encodedOrder.isEmpty()) {
            encodedOrder = getStringSetting(homeBlockOrderKey(normalizedThemeKey), "");
        }
        List<String> decodedOrder = decodeHomeModuleOrder(encodedOrder, sanitizedAvailableKeys);
        String repairedOrder = encodeStringList(decodedOrder);
        repairStringIfNeeded(preferenceKey, encodedOrder, repairedOrder);
        return decodedOrder;
    }

    public boolean isHomeModuleEnabled(String themeKey, String moduleKey) {
        return !getDisabledHomeModules(themeKey).contains(normalizeModuleKey(moduleKey));
    }

    public void setHomeModuleEnabled(String themeKey, String moduleKey, boolean enabled) {
        String normalizedThemeKey = VisualThemeUtils.normalizeThemeKey(themeKey);
        String normalizedModuleKey = normalizeModuleKey(moduleKey);
        Set<String> disabledModules = getDisabledHomeModules(normalizedThemeKey);
        if (enabled) {
            disabledModules.remove(normalizedModuleKey);
        } else {
            disabledModules.add(normalizedModuleKey);
        }
        preferences.edit()
                .putString(homeModuleDisabledKey(normalizedThemeKey), encodeStringSet(disabledModules))
                .apply();
    }

    public void moveHomeModule(
            String themeKey,
            String moduleKey,
            int direction,
            List<String> availableModuleKeys
    ) {
        if (direction == 0) {
            return;
        }
        String normalizedThemeKey = VisualThemeUtils.normalizeThemeKey(themeKey);
        String normalizedModuleKey = normalizeModuleKey(moduleKey);
        List<String> order = new ArrayList<>(getHomeModuleOrder(normalizedThemeKey, availableModuleKeys));
        int currentIndex = order.indexOf(normalizedModuleKey);
        if (currentIndex < 0) {
            return;
        }
        int targetIndex = Math.max(0, Math.min(order.size() - 1, currentIndex + direction));
        if (targetIndex == currentIndex) {
            return;
        }
        order.remove(currentIndex);
        order.add(targetIndex, normalizedModuleKey);
        preferences.edit()
                .putString(homeModuleOrderKey(normalizedThemeKey), encodeStringList(order))
                .apply();
    }

    public void resetHomeBlockLayout(String themeKey) {
        String normalizedThemeKey = VisualThemeUtils.normalizeThemeKey(themeKey);
        preferences.edit()
                .remove(homeModuleOrderKey(normalizedThemeKey))
                .remove(homeModuleDisabledKey(normalizedThemeKey))
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
        if (!preferences.contains(KEY_CUSTOM_THEME_CROP_ANCHOR)) {
            editor.putString(KEY_CUSTOM_THEME_CROP_ANCHOR, CustomThemeCropAnchor.CENTER);
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

    private static String homeModuleOrderKey(String themeKey) {
        return KEY_HOME_MODULE_ORDER_PREFIX + themeKey;
    }

    private static String homeModuleDisabledKey(String themeKey) {
        return KEY_HOME_MODULE_DISABLED_PREFIX + themeKey;
    }

    private String getCustomThemeImageUri(String weatherKey) {
        String normalizedWeatherKey = CustomThemeWeatherKey.normalize(weatherKey);
        String defaultValue = CustomThemeWeatherKey.FALLBACK.equals(normalizedWeatherKey)
                ? getStringSetting(KEY_CUSTOM_THEME_IMAGE_URI, "")
                : "";
        return getStringSetting(customThemeImageUriKey(normalizedWeatherKey), defaultValue);
    }

    private String getCustomThemeCropAnchor(String weatherKey) {
        String normalizedWeatherKey = CustomThemeWeatherKey.normalize(weatherKey);
        String defaultValue = CustomThemeWeatherKey.FALLBACK.equals(normalizedWeatherKey)
                ? getStringSetting(KEY_CUSTOM_THEME_CROP_ANCHOR, CustomThemeCropAnchor.CENTER)
                : CustomThemeCropAnchor.CENTER;
        String anchor = getStringSetting(customThemeCropAnchorKey(normalizedWeatherKey), defaultValue);
        String normalizedAnchor = CustomThemeCropAnchor.normalize(anchor);
        repairStringIfNeeded(customThemeCropAnchorKey(normalizedWeatherKey), anchor, normalizedAnchor);
        return normalizedAnchor;
    }

    private static String customThemeImageUriKey(String weatherKey) {
        return KEY_CUSTOM_THEME_IMAGE_URI_PREFIX + weatherKey;
    }

    private static String customThemeCropAnchorKey(String weatherKey) {
        return KEY_CUSTOM_THEME_CROP_ANCHOR_PREFIX + weatherKey;
    }

    private Set<String> getDisabledHomeModules(String themeKey) {
        String normalizedThemeKey = VisualThemeUtils.normalizeThemeKey(themeKey);
        String preferenceKey = homeModuleDisabledKey(normalizedThemeKey);
        String encodedBlocks = getStringSetting(preferenceKey, "");
        if (encodedBlocks.isEmpty()) {
            encodedBlocks = getStringSetting(homeBlockDisabledKey(normalizedThemeKey), "");
        }
        Set<String> disabledBlocks = decodeStringSet(encodedBlocks);
        String repairedBlocks = encodeStringSet(disabledBlocks);
        repairStringIfNeeded(preferenceKey, encodedBlocks, repairedBlocks);
        return disabledBlocks;
    }

    private static List<String> decodeHomeModuleOrder(String encodedOrder, List<String> availableKeys) {
        List<String> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        Set<String> available = new HashSet<>(availableKeys);
        if (encodedOrder != null && !encodedOrder.trim().isEmpty()) {
            String[] keys = encodedOrder.split(",");
            for (String rawKey : keys) {
                String key = rawKey.trim();
                if (available.contains(key) && seen.add(key)) {
                    result.add(key);
                }
            }
        }
        for (String key : availableKeys) {
            if (seen.add(key)) {
                result.add(key);
            }
        }
        return result;
    }

    private static Set<String> decodeStringSet(String encodedBlocks) {
        Set<String> result = new LinkedHashSet<>();
        if (encodedBlocks == null || encodedBlocks.trim().isEmpty()) {
            return result;
        }
        String[] keys = encodedBlocks.split(",");
        for (String rawKey : keys) {
            String key = rawKey.trim();
            if (!key.isEmpty()) {
                result.add(key);
            }
        }
        return result;
    }

    private static String encodeStringList(List<String> order) {
        StringBuilder builder = new StringBuilder();
        for (String key : order) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(key);
        }
        return builder.toString();
    }

    private static String encodeStringSet(Set<String> blocks) {
        StringBuilder builder = new StringBuilder();
        for (String key : blocks) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(key);
        }
        return builder.toString();
    }

    private static List<String> homeBlockKeys() {
        List<String> keys = new ArrayList<>();
        for (HomeBlock block : HomeBlock.defaultOrder()) {
            keys.add(block.getKey());
        }
        return keys;
    }

    private static List<String> sanitizeModuleKeys(List<String> moduleKeys) {
        List<String> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        if (moduleKeys == null) {
            return result;
        }
        for (String moduleKey : moduleKeys) {
            String normalizedModuleKey = normalizeModuleKey(moduleKey);
            if (!normalizedModuleKey.isEmpty() && seen.add(normalizedModuleKey)) {
                result.add(normalizedModuleKey);
            }
        }
        return result;
    }

    private static String normalizeModuleKey(String moduleKey) {
        if (moduleKey == null) {
            return "";
        }
        return moduleKey.trim();
    }

}
