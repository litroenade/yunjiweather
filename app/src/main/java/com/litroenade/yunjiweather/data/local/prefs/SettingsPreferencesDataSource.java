package com.litroenade.yunjiweather.data.local.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.litroenade.yunjiweather.data.model.CustomThemeAsset;
import com.litroenade.yunjiweather.data.model.CustomThemeCropAnchor;
import com.litroenade.yunjiweather.data.model.CustomThemeProfile;
import com.litroenade.yunjiweather.data.model.CustomThemeProfileCodec;
import com.litroenade.yunjiweather.data.model.CustomThemeRule;
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
    private static final String KEY_CUSTOM_THEME_PROFILE = "custom_theme_profile";
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
        return getCustomThemeImageUris().getOrDefault(CustomThemeWeatherKey.FALLBACK, "");
    }

    public void setCustomThemeImageUri(String imageUri) {
        setCustomThemeImage(CustomThemeWeatherKey.FALLBACK, imageUri, getCustomThemeCropAnchor());
    }

    public String getCustomThemeCropAnchor() {
        String legacyAnchor = getStringSetting(KEY_CUSTOM_THEME_CROP_ANCHOR, CustomThemeCropAnchor.CENTER);
        String normalizedAnchor = getCustomThemeCropAnchors()
                .getOrDefault(CustomThemeWeatherKey.FALLBACK, CustomThemeCropAnchor.CENTER);
        repairStringIfNeeded(KEY_CUSTOM_THEME_CROP_ANCHOR, legacyAnchor, normalizedAnchor);
        return normalizedAnchor;
    }

    public void setCustomThemeCropAnchor(String cropAnchor) {
        setCustomThemeImage(CustomThemeWeatherKey.FALLBACK, getCustomThemeImageUri(), cropAnchor);
    }

    public Map<String, String> getCustomThemeImageUris() {
        return customThemeImageUrisFromProfile(getCustomThemeProfile());
    }

    public Map<String, String> getCustomThemeCropAnchors() {
        return customThemeCropAnchorsFromProfile(getCustomThemeProfile());
    }

    public CustomThemeProfile getCustomThemeProfile() {
        String encodedProfile = getStringSetting(KEY_CUSTOM_THEME_PROFILE, "");
        CustomThemeProfile profile = CustomThemeProfileCodec.decode(encodedProfile);
        if (!profile.isEmpty()) {
            String repairedProfile = CustomThemeProfileCodec.encode(profile);
            repairStringIfNeeded(KEY_CUSTOM_THEME_PROFILE, encodedProfile, repairedProfile);
            return profile;
        }
        return legacyCustomThemeProfile();
    }

    public void setCustomThemeProfile(CustomThemeProfile profile) {
        CustomThemeProfile normalizedProfile = profile == null ? CustomThemeProfile.empty() : profile;
        SharedPreferences.Editor editor = preferences.edit()
                .putString(KEY_CUSTOM_THEME_PROFILE, CustomThemeProfileCodec.encode(normalizedProfile));
        clearLegacyCustomThemeKeys(editor);
        Map<String, String> imageUris = customThemeImageUrisFromProfile(normalizedProfile);
        Map<String, String> cropAnchors = customThemeCropAnchorsFromProfile(normalizedProfile);
        for (String weatherKey : imageUris.keySet()) {
            editor.putString(customThemeImageUriKey(weatherKey), imageUris.get(weatherKey))
                    .putString(customThemeCropAnchorKey(weatherKey),
                            cropAnchors.getOrDefault(weatherKey, CustomThemeCropAnchor.CENTER));
        }
        if (imageUris.containsKey(CustomThemeWeatherKey.FALLBACK)) {
            editor.putString(KEY_CUSTOM_THEME_IMAGE_URI, imageUris.get(CustomThemeWeatherKey.FALLBACK))
                    .putString(KEY_CUSTOM_THEME_CROP_ANCHOR,
                            cropAnchors.getOrDefault(CustomThemeWeatherKey.FALLBACK, CustomThemeCropAnchor.CENTER));
        }
        editor.apply();
    }

    private Map<String, String> getLegacyCustomThemeCropAnchors() {
        Map<String, String> result = new HashMap<>();
        for (String weatherKey : CustomThemeWeatherKey.orderedKeys()) {
            result.put(weatherKey, getCustomThemeCropAnchor(weatherKey));
        }
        return result;
    }

    public void setCustomThemeImage(String weatherKey, String imageUri, String cropAnchor) {
        CustomThemeWeatherKey.validate(weatherKey);
        CustomThemeCropAnchor.validate(cropAnchor);
        String normalizedWeatherKey = CustomThemeWeatherKey.normalize(weatherKey);
        String normalizedImageUri = imageUri == null ? "" : imageUri.trim();
        String normalizedCropAnchor = CustomThemeCropAnchor.normalize(cropAnchor);
        setCustomThemeProfile(upsertLegacyWeatherAsset(
                getCustomThemeProfile(),
                normalizedWeatherKey,
                normalizedImageUri,
                normalizedCropAnchor
        ));
    }

    public void clearCustomThemeImage(String weatherKey) {
        CustomThemeWeatherKey.validate(weatherKey);
        String normalizedWeatherKey = CustomThemeWeatherKey.normalize(weatherKey);
        setCustomThemeProfile(upsertLegacyWeatherAsset(
                getCustomThemeProfile(),
                normalizedWeatherKey,
                "",
                CustomThemeCropAnchor.CENTER
        ));
    }

    public void clearCustomThemeImages() {
        SharedPreferences.Editor editor = preferences.edit()
                .remove(KEY_CUSTOM_THEME_PROFILE)
                .remove(KEY_CUSTOM_THEME_IMAGE_URI)
                .remove(KEY_CUSTOM_THEME_CROP_ANCHOR);
        clearLegacyCustomThemeKeys(editor);
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
        if (VisualThemeUtils.THEME_CUSTOM_1.equals(normalizedThemeKey)) {
            List<String> profileOrder = getCustomThemeProfile().getHomeModuleOrder();
            if (!profileOrder.isEmpty()) {
                return decodeHomeModuleOrder(encodeStringList(profileOrder), sanitizedAvailableKeys);
            }
        }
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
        String normalizedThemeKey = VisualThemeUtils.normalizeThemeKey(themeKey);
        if (VisualThemeUtils.THEME_CUSTOM_1.equals(normalizedThemeKey)) {
            return !getCustomThemeProfile().getDisabledHomeModules().contains(normalizeModuleKey(moduleKey));
        }
        return !getDisabledHomeModules(themeKey).contains(normalizeModuleKey(moduleKey));
    }

    public void setHomeModuleEnabled(String themeKey, String moduleKey, boolean enabled) {
        String normalizedThemeKey = VisualThemeUtils.normalizeThemeKey(themeKey);
        String normalizedModuleKey = normalizeModuleKey(moduleKey);
        if (VisualThemeUtils.THEME_CUSTOM_1.equals(normalizedThemeKey)) {
            CustomThemeProfile profile = getCustomThemeProfile();
            Set<String> disabledModules = new LinkedHashSet<>(profile.getDisabledHomeModules());
            if (enabled) {
                disabledModules.remove(normalizedModuleKey);
            } else {
                disabledModules.add(normalizedModuleKey);
            }
            setCustomThemeProfile(CustomThemeProfile.create(
                    profile.getAssets(),
                    profile.getRules(),
                    profile.getHomeModuleOrder(),
                    disabledModules
            ));
            return;
        }
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
        moveHomeModuleTo(normalizedThemeKey, normalizedModuleKey, targetIndex, availableModuleKeys);
    }

    public void moveHomeModuleTo(
            String themeKey,
            String moduleKey,
            int targetIndex,
            List<String> availableModuleKeys
    ) {
        String normalizedThemeKey = VisualThemeUtils.normalizeThemeKey(themeKey);
        String normalizedModuleKey = normalizeModuleKey(moduleKey);
        List<String> order = new ArrayList<>(getHomeModuleOrder(normalizedThemeKey, availableModuleKeys));
        int currentIndex = order.indexOf(normalizedModuleKey);
        if (currentIndex < 0) {
            return;
        }
        int normalizedTargetIndex = Math.max(0, Math.min(order.size() - 1, targetIndex));
        if (normalizedTargetIndex == currentIndex) {
            return;
        }
        order.remove(currentIndex);
        order.add(normalizedTargetIndex, normalizedModuleKey);
        if (VisualThemeUtils.THEME_CUSTOM_1.equals(normalizedThemeKey)) {
            CustomThemeProfile profile = getCustomThemeProfile();
            setCustomThemeProfile(CustomThemeProfile.create(
                    profile.getAssets(),
                    profile.getRules(),
                    order,
                    profile.getDisabledHomeModules()
            ));
            return;
        }
        preferences.edit()
                .putString(homeModuleOrderKey(normalizedThemeKey), encodeStringList(order))
                .apply();
    }

    public void resetHomeBlockLayout(String themeKey) {
        String normalizedThemeKey = VisualThemeUtils.normalizeThemeKey(themeKey);
        if (VisualThemeUtils.THEME_CUSTOM_1.equals(normalizedThemeKey)) {
            CustomThemeProfile profile = getCustomThemeProfile();
            setCustomThemeProfile(CustomThemeProfile.create(
                    profile.getAssets(),
                    profile.getRules(),
                    new ArrayList<>(),
                    new LinkedHashSet<>()
            ));
            return;
        }
        preferences.edit()
                .remove(homeModuleOrderKey(normalizedThemeKey))
                .remove(homeModuleDisabledKey(normalizedThemeKey))
                .apply();
    }

    private CustomThemeProfile legacyCustomThemeProfile() {
        List<CustomThemeAsset> assets = new ArrayList<>();
        List<CustomThemeRule> rules = new ArrayList<>();
        for (String weatherKey : CustomThemeWeatherKey.orderedKeys()) {
            String imageUri = getCustomThemeImageUri(weatherKey);
            if (imageUri.isEmpty()) {
                continue;
            }
            String assetId = legacyAssetId(weatherKey);
            assets.add(new CustomThemeAsset(
                    assetId,
                    imageUri,
                    mediaTypeFromUri(imageUri),
                    getCustomThemeCropAnchor(weatherKey),
                    CustomThemeWeatherKey.displayName(weatherKey)
            ));
            rules.add(legacyRule(weatherKey, assetId));
        }
        return CustomThemeProfile.create(assets, rules, new ArrayList<>(), new LinkedHashSet<>());
    }

    private static Map<String, String> customThemeImageUrisFromProfile(CustomThemeProfile profile) {
        Map<String, String> result = new HashMap<>();
        Map<String, Integer> priorities = new HashMap<>();
        Map<String, CustomThemeAsset> assetsById = profile.assetsById();
        for (CustomThemeRule rule : profile.getRules()) {
            CustomThemeAsset asset = assetsById.get(rule.getAssetId());
            if (asset == null || asset.isEmpty()) {
                continue;
            }
            String weatherKey = projectedWeatherKey(rule);
            int priority = rule.getPriority();
            if (!result.containsKey(weatherKey) || priority > priorities.get(weatherKey)) {
                result.put(weatherKey, asset.getUri());
                priorities.put(weatherKey, priority);
            }
        }
        return result;
    }

    private static Map<String, String> customThemeCropAnchorsFromProfile(CustomThemeProfile profile) {
        Map<String, String> result = new HashMap<>();
        Map<String, Integer> priorities = new HashMap<>();
        Map<String, CustomThemeAsset> assetsById = profile.assetsById();
        for (CustomThemeRule rule : profile.getRules()) {
            CustomThemeAsset asset = assetsById.get(rule.getAssetId());
            if (asset == null || asset.isEmpty()) {
                continue;
            }
            String weatherKey = projectedWeatherKey(rule);
            int priority = rule.getPriority();
            if (!result.containsKey(weatherKey) || priority > priorities.get(weatherKey)) {
                result.put(weatherKey, asset.getCropAnchor());
                priorities.put(weatherKey, priority);
            }
        }
        if (!result.containsKey(CustomThemeWeatherKey.FALLBACK)) {
            result.put(CustomThemeWeatherKey.FALLBACK, CustomThemeCropAnchor.CENTER);
        }
        return result;
    }

    private static CustomThemeProfile upsertLegacyWeatherAsset(
            CustomThemeProfile profile,
            String weatherKey,
            String imageUri,
            String cropAnchor
    ) {
        String normalizedWeatherKey = CustomThemeWeatherKey.normalize(weatherKey);
        String assetId = legacyAssetId(normalizedWeatherKey);
        List<CustomThemeAsset> assets = new ArrayList<>();
        for (CustomThemeAsset asset : profile.getAssets()) {
            if (!assetId.equals(asset.getId())) {
                assets.add(asset);
            }
        }
        List<CustomThemeRule> rules = new ArrayList<>();
        for (CustomThemeRule rule : profile.getRules()) {
            if (!assetId.equals(rule.getAssetId())) {
                rules.add(rule);
            }
        }
        if (imageUri != null && !imageUri.trim().isEmpty()) {
            assets.add(new CustomThemeAsset(
                    assetId,
                    imageUri.trim(),
                    mediaTypeFromUri(imageUri),
                    cropAnchor,
                    CustomThemeWeatherKey.displayName(normalizedWeatherKey)
            ));
            rules.add(legacyRule(normalizedWeatherKey, assetId));
        }
        return CustomThemeProfile.create(assets, rules, profile.getHomeModuleOrder(), profile.getDisabledHomeModules());
    }

    private static CustomThemeRule legacyRule(String weatherKey, String assetId) {
        if (CustomThemeWeatherKey.FALLBACK.equals(weatherKey)) {
            return CustomThemeRule.fallback(assetId);
        }
        if (CustomThemeWeatherKey.NIGHT.equals(weatherKey)) {
            return new CustomThemeRule(
                    assetId,
                    CustomThemeWeatherKey.FALLBACK,
                    CustomThemeRule.LIGHT_NIGHT,
                    CustomThemeRule.NO_TIME,
                    CustomThemeRule.NO_TIME,
                    40
            );
        }
        if (CustomThemeWeatherKey.RAIN_NIGHT.equals(weatherKey)) {
            return new CustomThemeRule(
                    assetId,
                    CustomThemeWeatherKey.RAIN,
                    CustomThemeRule.LIGHT_NIGHT,
                    CustomThemeRule.NO_TIME,
                    CustomThemeRule.NO_TIME,
                    80
            );
        }
        if (CustomThemeWeatherKey.SNOW_NIGHT.equals(weatherKey)) {
            return new CustomThemeRule(
                    assetId,
                    CustomThemeWeatherKey.SNOW,
                    CustomThemeRule.LIGHT_NIGHT,
                    CustomThemeRule.NO_TIME,
                    CustomThemeRule.NO_TIME,
                    80
            );
        }
        if (CustomThemeWeatherKey.DAWN.equals(weatherKey)) {
            return new CustomThemeRule(
                    assetId,
                    CustomThemeWeatherKey.FALLBACK,
                    CustomThemeRule.LIGHT_ANY,
                    5 * 60,
                    8 * 60,
                    50
            );
        }
        if (CustomThemeWeatherKey.DUSK.equals(weatherKey)) {
            return new CustomThemeRule(
                    assetId,
                    CustomThemeWeatherKey.FALLBACK,
                    CustomThemeRule.LIGHT_ANY,
                    17 * 60,
                    20 * 60,
                    50
            );
        }
        return new CustomThemeRule(
                assetId,
                weatherKey,
                CustomThemeRule.LIGHT_ANY,
                CustomThemeRule.NO_TIME,
                CustomThemeRule.NO_TIME,
                20
        );
    }

    private static String projectedWeatherKey(CustomThemeRule rule) {
        if (CustomThemeWeatherKey.RAIN.equals(rule.getWeatherKey())
                && CustomThemeRule.LIGHT_NIGHT.equals(rule.getLightMode())) {
            return CustomThemeWeatherKey.RAIN_NIGHT;
        }
        if (CustomThemeWeatherKey.SNOW.equals(rule.getWeatherKey())
                && CustomThemeRule.LIGHT_NIGHT.equals(rule.getLightMode())) {
            return CustomThemeWeatherKey.SNOW_NIGHT;
        }
        if (CustomThemeWeatherKey.FALLBACK.equals(rule.getWeatherKey())
                && rule.getStartMinute() == 5 * 60
                && rule.getEndMinute() == 8 * 60) {
            return CustomThemeWeatherKey.DAWN;
        }
        if (CustomThemeWeatherKey.FALLBACK.equals(rule.getWeatherKey())
                && rule.getStartMinute() == 17 * 60
                && rule.getEndMinute() == 20 * 60) {
            return CustomThemeWeatherKey.DUSK;
        }
        if (CustomThemeWeatherKey.FALLBACK.equals(rule.getWeatherKey())
                && CustomThemeRule.LIGHT_NIGHT.equals(rule.getLightMode())) {
            return CustomThemeWeatherKey.NIGHT;
        }
        return rule.getWeatherKey();
    }

    private static String legacyAssetId(String weatherKey) {
        return "legacy-" + CustomThemeWeatherKey.normalize(weatherKey);
    }

    private static String mediaTypeFromUri(String imageUri) {
        String normalized = imageUri == null ? "" : imageUri.trim().toLowerCase();
        return normalized.endsWith(".gif") ? CustomThemeAsset.MEDIA_GIF : CustomThemeAsset.MEDIA_IMAGE;
    }

    private static void clearLegacyCustomThemeKeys(SharedPreferences.Editor editor) {
        for (String weatherKey : CustomThemeWeatherKey.orderedKeys()) {
            editor.remove(customThemeImageUriKey(weatherKey))
                    .remove(customThemeCropAnchorKey(weatherKey));
        }
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
