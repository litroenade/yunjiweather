package com.litroenade.yunjiweather.utils;

import android.content.SharedPreferences;

import com.litroenade.yunjiweather.data.local.prefs.SettingsPreferencesDataSource;
import com.litroenade.yunjiweather.data.model.CustomThemeAsset;
import com.litroenade.yunjiweather.data.model.CustomThemeCropAnchor;
import com.litroenade.yunjiweather.data.model.CustomThemeProfile;
import com.litroenade.yunjiweather.data.model.CustomThemeRule;
import com.litroenade.yunjiweather.data.model.CustomThemeWeatherKey;
import com.litroenade.yunjiweather.ui.compose.home.modules.HomeModuleCatalog;
import com.litroenade.yunjiweather.ui.compose.theme.mixins.ThemeMixinCatalog;
import com.litroenade.yunjiweather.ui.compose.theme.profiles.ThemeProfileCatalog;
import com.litroenade.yunjiweather.ui.compose.theme.skins.ThemeSkinCatalog;

import org.junit.Test;

import java.util.Collections;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class VisualThemeUtilsTest {

    @Test
    public void isSupportedTheme_returnsFalseForExternalImageKey() {
        assertEquals(false, VisualThemeUtils.isSupportedTheme("external-fanart"));
    }

    @Test
    public void catalog_returnsThemesInStableOrder() {
        List<VisualTheme> themes = VisualThemeCatalog.getThemes();

        assertEquals(3, themes.size());
        assertEquals(VisualThemeUtils.THEME_SKY, themes.get(0).getKey());
        assertEquals(VisualThemeUtils.THEME_PANORAMA, themes.get(1).getKey());
        assertEquals(VisualThemeUtils.THEME_CUSTOM_1, themes.get(2).getKey());
        assertEquals(false, themes.get(0).isCustomSlot());
        assertEquals(false, themes.get(1).isCustomSlot());
        assertEquals(true, themes.get(2).isCustomSlot());
        assertEquals(true, themes.get(0).isSelectable());
        assertEquals(true, themes.get(1).isSelectable());
        assertEquals(true, themes.get(2).isSelectable());
    }

    @Test
    public void catalog_containsPanoramaThemeModel() {
        VisualTheme panorama = VisualThemeCatalog.findByKey(VisualThemeUtils.THEME_PANORAMA);

        assertNotNull(panorama);
        assertEquals("全景天气", panorama.getDisplayName());
        assertEquals("沉浸式动态天气，强化真实光影、雨雪和风场层次。", panorama.getShortDescription());
    }

    @Test
    public void catalog_treatsFantasyAsLegacyThemeKey() {
        VisualTheme fantasy = VisualThemeCatalog.findByKey(VisualThemeUtils.THEME_FANTASY);

        assertNull(fantasy);
        assertEquals(false, VisualThemeUtils.isSupportedTheme(VisualThemeUtils.THEME_FANTASY));
        assertEquals(VisualThemeUtils.THEME_PANORAMA, VisualThemeUtils.normalizeThemeKey(VisualThemeUtils.THEME_FANTASY));
    }

    @Test
    public void skinCatalog_mapsThemeKeysToDedicatedFolders() {
        assertEquals("official", ThemeSkinCatalog.getThemeFolder(VisualThemeUtils.THEME_SKY));
        assertEquals("panorama", ThemeSkinCatalog.getThemeFolder(VisualThemeUtils.THEME_PANORAMA));
        assertEquals("panorama", ThemeSkinCatalog.getThemeFolder(VisualThemeUtils.THEME_FANTASY));
        assertEquals("custom", ThemeSkinCatalog.getThemeFolder(VisualThemeUtils.THEME_CUSTOM_1));
    }

    @Test
    public void panoramaSkinHasStrongerHomeImmersionThanDefaultTheme() {
        assertEquals(true, ThemeSkinCatalog.getSkin(VisualThemeUtils.THEME_PANORAMA).getHomeImmersion() >
                ThemeSkinCatalog.getSkin(VisualThemeUtils.THEME_SKY).getHomeImmersion());
        assertEquals(true, ThemeSkinCatalog.getSkin(VisualThemeUtils.THEME_PANORAMA).getHeroAnimationScale() >
                ThemeSkinCatalog.getSkin(VisualThemeUtils.THEME_SKY).getHeroAnimationScale());
    }

    @Test
    public void panoramaSkinOverridesWeatherAnimationLightingAndParticles() {
        assertEquals(true, ThemeSkinCatalog.getSkin(VisualThemeUtils.THEME_PANORAMA).getSunGlowScale() >
                ThemeSkinCatalog.getSkin(VisualThemeUtils.THEME_SKY).getSunGlowScale());
        assertEquals(true, ThemeSkinCatalog.getSkin(VisualThemeUtils.THEME_PANORAMA).getCloudShapeScale() >
                ThemeSkinCatalog.getSkin(VisualThemeUtils.THEME_SKY).getCloudShapeScale());
        assertEquals(true, ThemeSkinCatalog.getSkin(VisualThemeUtils.THEME_PANORAMA).getPrecipitationOpacityMultiplier() >
                ThemeSkinCatalog.getSkin(VisualThemeUtils.THEME_SKY).getPrecipitationOpacityMultiplier());
        assertEquals(true, ThemeSkinCatalog.getSkin(VisualThemeUtils.THEME_PANORAMA).getWeatherAnimationSpeed() >
                ThemeSkinCatalog.getSkin(VisualThemeUtils.THEME_SKY).getWeatherAnimationSpeed());
    }

    @Test
    public void panoramaSkinOverridesNightSceneStars() {
        assertEquals(true, ThemeSkinCatalog.getSkin(VisualThemeUtils.THEME_SKY).getNightStarDensity() > 0f);
        assertEquals(true, ThemeSkinCatalog.getSkin(VisualThemeUtils.THEME_PANORAMA).getNightStarDensity() >
                ThemeSkinCatalog.getSkin(VisualThemeUtils.THEME_SKY).getNightStarDensity());
        assertEquals(true, ThemeSkinCatalog.getSkin(VisualThemeUtils.THEME_PANORAMA).getNightStarGlowScale() >
                ThemeSkinCatalog.getSkin(VisualThemeUtils.THEME_SKY).getNightStarGlowScale());
    }

    @Test
    public void customSkinIsRuntimeSelectable() {
        assertEquals(true, ThemeSkinCatalog.getSkin(VisualThemeUtils.THEME_CUSTOM_1).isRuntimeSelectable());
    }

    @Test
    public void settingsManager_repairsUnsupportedVisualThemeToDefault() {
        MemorySharedPreferences preferences = new MemorySharedPreferences();
        preferences.edit().putString("visual_theme", "external-fanart").apply();

        SettingsPreferencesDataSource settingsManager = new SettingsPreferencesDataSource(preferences);

        assertEquals(VisualThemeUtils.THEME_SKY, settingsManager.getVisualTheme());
        assertEquals(VisualThemeUtils.THEME_SKY, preferences.getString("visual_theme", ""));
    }

    @Test
    public void settingsManager_repairsUnsupportedUnitsToDefaults() {
        MemorySharedPreferences preferences = new MemorySharedPreferences();
        preferences.edit()
                .putString("temperature_unit", "K")
                .putString("wind_unit", "KNOT")
                .apply();

        SettingsPreferencesDataSource settingsManager = new SettingsPreferencesDataSource(preferences);

        assertEquals(WeatherDisplayUtils.TEMPERATURE_CELSIUS, settingsManager.getTemperatureUnit());
        assertEquals(WeatherDisplayUtils.TEMPERATURE_CELSIUS, preferences.getString("temperature_unit", ""));
        assertEquals(WeatherDisplayUtils.WIND_SCALE, settingsManager.getWindUnit());
        assertEquals(WeatherDisplayUtils.WIND_SCALE, preferences.getString("wind_unit", ""));
    }

    @Test
    public void settingsManager_repairsWrongTypedVisualThemeToDefault() {
        MemorySharedPreferences preferences = new MemorySharedPreferences();
        preferences.edit().putBoolean("visual_theme", true).apply();

        SettingsPreferencesDataSource settingsManager = new SettingsPreferencesDataSource(preferences);

        assertEquals(VisualThemeUtils.THEME_SKY, settingsManager.getVisualTheme());
        assertEquals(VisualThemeUtils.THEME_SKY, preferences.getString("visual_theme", ""));
    }

    @Test
    public void settingsManager_migratesLegacySakuraThemeToPanoramaWeather() {
        MemorySharedPreferences preferences = new MemorySharedPreferences();
        preferences.edit().putString("visual_theme", "sakura").apply();

        SettingsPreferencesDataSource settingsManager = new SettingsPreferencesDataSource(preferences);

        assertEquals(VisualThemeUtils.THEME_PANORAMA, settingsManager.getVisualTheme());
        assertEquals(VisualThemeUtils.THEME_PANORAMA, preferences.getString("visual_theme", ""));
    }

    @Test
    public void settingsManager_migratesLegacyFantasyThemeToPanoramaWeather() {
        MemorySharedPreferences preferences = new MemorySharedPreferences();
        preferences.edit().putString("visual_theme", "fantasy").apply();

        SettingsPreferencesDataSource settingsManager = new SettingsPreferencesDataSource(preferences);

        assertEquals(VisualThemeUtils.THEME_PANORAMA, settingsManager.getVisualTheme());
        assertEquals(VisualThemeUtils.THEME_PANORAMA, preferences.getString("visual_theme", ""));
    }

    @Test
    public void settingsManager_migratesLegacyRealWeatherThemeToPanoramaWeather() {
        MemorySharedPreferences preferences = new MemorySharedPreferences();
        preferences.edit().putString("visual_theme", "real_weather").apply();

        SettingsPreferencesDataSource settingsManager = new SettingsPreferencesDataSource(preferences);

        assertEquals(VisualThemeUtils.THEME_PANORAMA, settingsManager.getVisualTheme());
        assertEquals(VisualThemeUtils.THEME_PANORAMA, preferences.getString("visual_theme", ""));
    }

    @Test
    public void settingsManager_migratesLegacySecondCustomThemeToDefaultTheme() {
        MemorySharedPreferences preferences = new MemorySharedPreferences();
        preferences.edit().putString("visual_theme", "custom_2").apply();

        SettingsPreferencesDataSource settingsManager = new SettingsPreferencesDataSource(preferences);

        assertEquals(VisualThemeUtils.THEME_SKY, settingsManager.getVisualTheme());
        assertEquals(VisualThemeUtils.THEME_SKY, preferences.getString("visual_theme", ""));
    }

    @Test
    public void settingsManager_keepsCustomThemeSlotAsRuntimeTheme() {
        MemorySharedPreferences preferences = new MemorySharedPreferences();
        preferences.edit().putString("visual_theme", VisualThemeUtils.THEME_CUSTOM_1).apply();

        SettingsPreferencesDataSource settingsManager = new SettingsPreferencesDataSource(preferences);

        assertEquals(VisualThemeUtils.THEME_CUSTOM_1, settingsManager.getVisualTheme());
        assertEquals(VisualThemeUtils.THEME_CUSTOM_1, preferences.getString("visual_theme", ""));
    }

    @Test
    public void settingsManager_persistsCustomThemeImageAndCropAnchor() {
        SettingsPreferencesDataSource settingsManager = new SettingsPreferencesDataSource(new MemorySharedPreferences());

        assertEquals("", settingsManager.getCustomThemeImageUri());
        assertEquals(CustomThemeCropAnchor.CENTER, settingsManager.getCustomThemeCropAnchor());

        settingsManager.setCustomThemeImageUri("content://yunji/custom-theme");
        settingsManager.setCustomThemeCropAnchor(CustomThemeCropAnchor.TOP);

        assertEquals("content://yunji/custom-theme", settingsManager.getCustomThemeImageUri());
        assertEquals(CustomThemeCropAnchor.TOP, settingsManager.getCustomThemeCropAnchor());
    }

    @Test
    public void settingsManager_persistsCompleteCustomThemeProfile() {
        SettingsPreferencesDataSource settingsManager = new SettingsPreferencesDataSource(new MemorySharedPreferences());
        CustomThemeProfile profile = CustomThemeProfile.create(
                Arrays.asList(
                        new CustomThemeAsset("fallback", "file:///fallback.jpg", CustomThemeAsset.MEDIA_IMAGE, CustomThemeCropAnchor.CENTER, "默认图"),
                        new CustomThemeAsset("rain-night", "file:///rain-night.gif", CustomThemeAsset.MEDIA_GIF, CustomThemeCropAnchor.BOTTOM, "雨夜")
                ),
                Arrays.asList(
                        CustomThemeRule.fallback("fallback"),
                        new CustomThemeRule("rain-night", CustomThemeWeatherKey.RAIN, CustomThemeRule.LIGHT_NIGHT, 18 * 60, 23 * 60, 80)
                ),
                Arrays.asList(HomeBlock.WEATHER_METRICS.getKey(), HomeBlock.HOURLY_FORECAST.getKey()),
                Collections.singleton(HomeBlock.AIR_SUN.getKey())
        );

        settingsManager.setCustomThemeProfile(profile);

        CustomThemeProfile restored = settingsManager.getCustomThemeProfile();
        assertEquals(2, restored.getAssets().size());
        assertEquals(CustomThemeAsset.MEDIA_GIF, restored.getAssets().get(1).getMediaType());
        assertEquals(CustomThemeRule.LIGHT_NIGHT, restored.getRules().get(1).getLightMode());
        assertEquals(Arrays.asList(HomeBlock.WEATHER_METRICS.getKey(), HomeBlock.HOURLY_FORECAST.getKey()),
                restored.getHomeModuleOrder());
        assertEquals(true, restored.getDisabledHomeModules().contains(HomeBlock.AIR_SUN.getKey()));
    }

    @Test
    public void settingsManager_buildsCustomThemeProfileFromLegacyWeatherSlots() {
        MemorySharedPreferences preferences = new MemorySharedPreferences();
        preferences.edit()
                .putString("custom_theme_image_uri_fallback", "file:///fallback.jpg")
                .putString("custom_theme_crop_anchor_fallback", CustomThemeCropAnchor.TOP)
                .putString("custom_theme_image_uri_rain", "file:///rain.gif")
                .putString("custom_theme_crop_anchor_rain", CustomThemeCropAnchor.BOTTOM)
                .apply();
        SettingsPreferencesDataSource settingsManager = new SettingsPreferencesDataSource(preferences);

        CustomThemeProfile profile = settingsManager.getCustomThemeProfile();

        assertEquals(2, profile.getAssets().size());
        assertEquals("legacy-fallback", profile.getAssets().get(0).getId());
        assertEquals("file:///fallback.jpg", profile.getAssets().get(0).getUri());
        assertEquals(CustomThemeCropAnchor.TOP, profile.getAssets().get(0).getCropAnchor());
        assertEquals("legacy-rain", profile.getAssets().get(1).getId());
        assertEquals(CustomThemeWeatherKey.RAIN, profile.getRules().get(1).getWeatherKey());
    }

    @Test
    public void settingsManager_repairsUnsupportedCustomThemeCropAnchorToCenter() {
        MemorySharedPreferences preferences = new MemorySharedPreferences();
        preferences.edit().putString("custom_theme_crop_anchor", "diagonal").apply();

        SettingsPreferencesDataSource settingsManager = new SettingsPreferencesDataSource(preferences);

        assertEquals(CustomThemeCropAnchor.CENTER, settingsManager.getCustomThemeCropAnchor());
        assertEquals(CustomThemeCropAnchor.CENTER, preferences.getString("custom_theme_crop_anchor", ""));
    }

    @Test
    public void settingsManager_repairsWrongTypedUnitsToDefaults() {
        MemorySharedPreferences preferences = new MemorySharedPreferences();
        preferences.edit()
                .putBoolean("temperature_unit", true)
                .putInt("wind_unit", 2)
                .apply();

        SettingsPreferencesDataSource settingsManager = new SettingsPreferencesDataSource(preferences);

        assertEquals(WeatherDisplayUtils.TEMPERATURE_CELSIUS, settingsManager.getTemperatureUnit());
        assertEquals(WeatherDisplayUtils.TEMPERATURE_CELSIUS, preferences.getString("temperature_unit", ""));
        assertEquals(WeatherDisplayUtils.WIND_SCALE, settingsManager.getWindUnit());
        assertEquals(WeatherDisplayUtils.WIND_SCALE, preferences.getString("wind_unit", ""));
    }

    @Test
    public void settingsManager_persistsDeveloperToolsEnabled() {
        SettingsPreferencesDataSource settingsManager = new SettingsPreferencesDataSource(new MemorySharedPreferences());

        assertEquals(false, settingsManager.isDeveloperToolsEnabled());

        settingsManager.setDeveloperToolsEnabled(true);
        assertEquals(true, settingsManager.isDeveloperToolsEnabled());

        settingsManager.setDeveloperToolsEnabled(false);
        assertEquals(false, settingsManager.isDeveloperToolsEnabled());
    }

    @Test
    public void settingsManager_persistsHomeBlockLayoutPerTheme() {
        SettingsPreferencesDataSource settingsManager = new SettingsPreferencesDataSource(new MemorySharedPreferences());

        List<HomeBlock> defaultOrder = settingsManager.getHomeBlockOrder(VisualThemeUtils.THEME_SKY);
        assertEquals(7, defaultOrder.size());
        assertEquals(HomeBlock.WEATHER_METRICS, defaultOrder.get(0));
        assertEquals(HomeBlock.DAILY_FORECAST, defaultOrder.get(6));
        assertEquals(true, settingsManager.isHomeBlockEnabled(VisualThemeUtils.THEME_SKY, HomeBlock.HOURLY_FORECAST));

        settingsManager.setHomeBlockEnabled(VisualThemeUtils.THEME_SKY, HomeBlock.HOURLY_FORECAST, false);
        settingsManager.moveHomeBlock(VisualThemeUtils.THEME_SKY, HomeBlock.DAILY_FORECAST, -1);

        List<HomeBlock> updatedSkyOrder = settingsManager.getHomeBlockOrder(VisualThemeUtils.THEME_SKY);
        assertEquals(HomeBlock.DAILY_FORECAST, updatedSkyOrder.get(5));
        assertEquals(HomeBlock.HOURLY_FORECAST, updatedSkyOrder.get(6));
        assertEquals(false, settingsManager.isHomeBlockEnabled(VisualThemeUtils.THEME_SKY, HomeBlock.HOURLY_FORECAST));
        assertEquals(true, settingsManager.isHomeBlockEnabled(VisualThemeUtils.THEME_PANORAMA, HomeBlock.HOURLY_FORECAST));
    }

    @Test
    public void homeModuleCatalog_addsThemeModulesAfterBuiltInHomeBlocks() {
        List<String> defaultKeys = HomeModuleCatalog.getAvailableModuleKeys(VisualThemeUtils.THEME_SKY);
        List<String> panoramaKeys = HomeModuleCatalog.getAvailableModuleKeys(VisualThemeUtils.THEME_PANORAMA);

        assertEquals(7, defaultKeys.size());
        assertEquals(HomeBlock.WEATHER_METRICS.getKey(), defaultKeys.get(0));
        assertEquals(HomeBlock.DAILY_FORECAST.getKey(), defaultKeys.get(6));
        assertEquals(8, panoramaKeys.size());
        assertEquals(HomeBlock.DAILY_FORECAST.getKey(), panoramaKeys.get(6));
        assertEquals("calendar_life", panoramaKeys.get(7));
        assertEquals("日历生活", HomeModuleCatalog.getDefinition("calendar_life").getDisplayName());
    }

    @Test
    public void themeMixinCatalog_exposesPanoramaCalendarHomeModule() {
        assertEquals(0, ThemeMixinCatalog.getHomeModules(VisualThemeUtils.THEME_SKY).size());
        assertEquals(1, ThemeMixinCatalog.getHomeModules(VisualThemeUtils.THEME_PANORAMA).size());
        assertEquals(
                "calendar_life",
                ThemeMixinCatalog.getHomeModules(VisualThemeUtils.THEME_PANORAMA).get(0).getKey()
        );
    }

    @Test
    public void themeProfileCatalogCombinesMetadataSkinAndMixins() {
        assertEquals("默认主题", ThemeProfileCatalog.getProfile(VisualThemeUtils.THEME_SKY).getVisualTheme().getDisplayName());
        assertEquals("全景天气", ThemeProfileCatalog.getProfile(VisualThemeUtils.THEME_PANORAMA).getVisualTheme().getDisplayName());
        assertEquals("panorama", ThemeProfileCatalog.getProfile(VisualThemeUtils.THEME_PANORAMA).getSkin().getFolderName());
        assertEquals(1, ThemeProfileCatalog.getProfile(VisualThemeUtils.THEME_PANORAMA).getMixins().size());
        assertEquals("official", ThemeProfileCatalog.getThemeFolder("external-fanart"));
    }

    @Test
    public void settingsManager_persistsStringHomeModuleLayoutPerTheme() {
        SettingsPreferencesDataSource settingsManager = new SettingsPreferencesDataSource(new MemorySharedPreferences());
        List<String> panoramaKeys = HomeModuleCatalog.getAvailableModuleKeys(VisualThemeUtils.THEME_PANORAMA);

        List<String> defaultOrder = settingsManager.getHomeModuleOrder(VisualThemeUtils.THEME_PANORAMA, panoramaKeys);
        assertEquals(8, defaultOrder.size());
        assertEquals(HomeBlock.WEATHER_METRICS.getKey(), defaultOrder.get(0));
        assertEquals("calendar_life", defaultOrder.get(7));
        assertEquals(true, settingsManager.isHomeModuleEnabled(VisualThemeUtils.THEME_PANORAMA, "calendar_life"));

        settingsManager.setHomeModuleEnabled(VisualThemeUtils.THEME_PANORAMA, "calendar_life", false);
        settingsManager.moveHomeModule(VisualThemeUtils.THEME_PANORAMA, "calendar_life", -1, panoramaKeys);

        List<String> updatedOrder = settingsManager.getHomeModuleOrder(VisualThemeUtils.THEME_PANORAMA, panoramaKeys);
        assertEquals("calendar_life", updatedOrder.get(6));
        assertEquals(HomeBlock.DAILY_FORECAST.getKey(), updatedOrder.get(7));
        assertEquals(false, settingsManager.isHomeModuleEnabled(VisualThemeUtils.THEME_PANORAMA, "calendar_life"));
        assertEquals(true, settingsManager.isHomeModuleEnabled(VisualThemeUtils.THEME_SKY, "calendar_life"));
    }

    @Test
    public void settingsManager_persistsCustomThemeModuleLayoutBeforeAnyImage() {
        SettingsPreferencesDataSource settingsManager = new SettingsPreferencesDataSource(new MemorySharedPreferences());
        List<String> customKeys = HomeModuleCatalog.getAvailableModuleKeys(VisualThemeUtils.THEME_CUSTOM_1);

        settingsManager.setHomeModuleEnabled(VisualThemeUtils.THEME_CUSTOM_1, HomeBlock.HOURLY_FORECAST.getKey(), false);
        settingsManager.moveHomeModule(VisualThemeUtils.THEME_CUSTOM_1, HomeBlock.DAILY_FORECAST.getKey(), -1, customKeys);

        CustomThemeProfile profile = settingsManager.getCustomThemeProfile();
        List<String> updatedOrder = settingsManager.getHomeModuleOrder(VisualThemeUtils.THEME_CUSTOM_1, customKeys);

        assertEquals(true, profile.getDisabledHomeModules().contains(HomeBlock.HOURLY_FORECAST.getKey()));
        assertEquals(false, settingsManager.isHomeModuleEnabled(VisualThemeUtils.THEME_CUSTOM_1, HomeBlock.HOURLY_FORECAST.getKey()));
        assertEquals(HomeBlock.DAILY_FORECAST.getKey(), updatedOrder.get(updatedOrder.size() - 2));
        assertEquals(HomeBlock.HOURLY_FORECAST.getKey(), updatedOrder.get(updatedOrder.size() - 1));
    }

    @Test
    public void settingsManager_resetsHomeBlockLayoutForTheme() {
        SettingsPreferencesDataSource settingsManager = new SettingsPreferencesDataSource(new MemorySharedPreferences());
        settingsManager.setHomeBlockEnabled(VisualThemeUtils.THEME_SKY, HomeBlock.WEATHER_INSIGHT, false);
        settingsManager.moveHomeBlock(VisualThemeUtils.THEME_SKY, HomeBlock.DAILY_FORECAST, -1);

        settingsManager.resetHomeBlockLayout(VisualThemeUtils.THEME_SKY);

        List<HomeBlock> resetOrder = settingsManager.getHomeBlockOrder(VisualThemeUtils.THEME_SKY);
        assertEquals(HomeBlock.WEATHER_METRICS, resetOrder.get(0));
        assertEquals(HomeBlock.DAILY_FORECAST, resetOrder.get(6));
        assertEquals(true, settingsManager.isHomeBlockEnabled(VisualThemeUtils.THEME_SKY, HomeBlock.WEATHER_INSIGHT));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setTemperatureUnit_rejectsUnsupportedUnit() {
        new SettingsPreferencesDataSource(new MemorySharedPreferences()).setTemperatureUnit("K");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setWindUnit_rejectsUnsupportedUnit() {
        new SettingsPreferencesDataSource(new MemorySharedPreferences()).setWindUnit("KNOT");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setVisualTheme_rejectsUnsupportedTheme() {
        new SettingsPreferencesDataSource(new MemorySharedPreferences()).setVisualTheme("external-fanart");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCustomThemeImage_rejectsUnsupportedWeatherKey() {
        new SettingsPreferencesDataSource(new MemorySharedPreferences()).setCustomThemeImage(
                "storm_but_not_catalogued",
                "file:///storm.jpg",
                CustomThemeCropAnchor.CENTER
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCustomThemeImage_rejectsUnsupportedCropAnchor() {
        new SettingsPreferencesDataSource(new MemorySharedPreferences()).setCustomThemeImage(
                CustomThemeWeatherKey.SUNNY,
                "file:///sunny.jpg",
                "diagonal"
        );
    }

    private static final class MemorySharedPreferences implements SharedPreferences {
        private final Map<String, Object> values = new HashMap<>();

        @Override
        public Map<String, ?> getAll() {
            return Collections.unmodifiableMap(values);
        }

        @Override
        public String getString(String key, String defaultValue) {
            Object value = values.get(key);
            if (value == null) {
                return defaultValue;
            }
            if (!(value instanceof String)) {
                throw new ClassCastException(key);
            }
            return (String) value;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Set<String> getStringSet(String key, Set<String> defaultValues) {
            Object value = values.get(key);
            if (value == null) {
                return defaultValues;
            }
            if (!(value instanceof Set)) {
                throw new ClassCastException(key);
            }
            return (Set<String>) value;
        }

        @Override
        public int getInt(String key, int defaultValue) {
            Object value = values.get(key);
            if (value == null) {
                return defaultValue;
            }
            if (!(value instanceof Integer)) {
                throw new ClassCastException(key);
            }
            return (int) value;
        }

        @Override
        public long getLong(String key, long defaultValue) {
            Object value = values.get(key);
            if (value == null) {
                return defaultValue;
            }
            if (!(value instanceof Long)) {
                throw new ClassCastException(key);
            }
            return (long) value;
        }

        @Override
        public float getFloat(String key, float defaultValue) {
            Object value = values.get(key);
            if (value == null) {
                return defaultValue;
            }
            if (!(value instanceof Float)) {
                throw new ClassCastException(key);
            }
            return (float) value;
        }

        @Override
        public boolean getBoolean(String key, boolean defaultValue) {
            Object value = values.get(key);
            if (value == null) {
                return defaultValue;
            }
            if (!(value instanceof Boolean)) {
                throw new ClassCastException(key);
            }
            return (boolean) value;
        }

        @Override
        public boolean contains(String key) {
            return values.containsKey(key);
        }

        @Override
        public Editor edit() {
            return new MemoryEditor();
        }

        @Override
        public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        }

        @Override
        public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        }

        private final class MemoryEditor implements Editor {
            private final Map<String, Object> pendingValues = new HashMap<>();
            private final Set<String> removals = new java.util.HashSet<>();
            private boolean clear;

            @Override
            public Editor putString(String key, String value) {
                pendingValues.put(key, value);
                removals.remove(key);
                return this;
            }

            @Override
            public Editor putStringSet(String key, Set<String> value) {
                pendingValues.put(key, value);
                removals.remove(key);
                return this;
            }

            @Override
            public Editor putInt(String key, int value) {
                pendingValues.put(key, value);
                removals.remove(key);
                return this;
            }

            @Override
            public Editor putLong(String key, long value) {
                pendingValues.put(key, value);
                removals.remove(key);
                return this;
            }

            @Override
            public Editor putFloat(String key, float value) {
                pendingValues.put(key, value);
                removals.remove(key);
                return this;
            }

            @Override
            public Editor putBoolean(String key, boolean value) {
                pendingValues.put(key, value);
                removals.remove(key);
                return this;
            }

            @Override
            public Editor remove(String key) {
                pendingValues.remove(key);
                removals.add(key);
                return this;
            }

            @Override
            public Editor clear() {
                clear = true;
                pendingValues.clear();
                removals.clear();
                return this;
            }

            @Override
            public boolean commit() {
                apply();
                return true;
            }

            @Override
            public void apply() {
                if (clear) {
                    values.clear();
                }
                for (String key : removals) {
                    values.remove(key);
                }
                values.putAll(pendingValues);
            }
        }
    }
}
