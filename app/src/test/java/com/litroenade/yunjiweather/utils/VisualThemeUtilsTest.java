package com.litroenade.yunjiweather.utils;

import android.content.SharedPreferences;

import com.litroenade.yunjiweather.settings.SettingsManager;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VisualThemeUtilsTest {

    @Test
    public void isSupportedTheme_returnsFalseForExternalImageKey() {
        assertEquals(false, VisualThemeUtils.isSupportedTheme("external-fanart"));
    }

    @Test
    public void catalog_returnsThemesInStableOrder() {
        List<VisualTheme> themes = VisualThemeCatalog.getThemes();

        assertEquals(5, themes.size());
        assertEquals(VisualThemeUtils.THEME_SKY, themes.get(0).getKey());
        assertEquals(VisualThemeUtils.THEME_FANTASY, themes.get(1).getKey());
        assertEquals(VisualThemeUtils.THEME_SAKURA, themes.get(2).getKey());
        assertEquals(VisualThemeUtils.THEME_CUSTOM_1, themes.get(3).getKey());
        assertEquals(VisualThemeUtils.THEME_CUSTOM_2, themes.get(4).getKey());
        assertEquals(true, themes.get(3).isCustomSlot());
        assertEquals(true, themes.get(4).isCustomSlot());
    }

    @Test
    public void catalog_containsSakuraThemeModel() {
        VisualTheme sakura = VisualThemeCatalog.findByKey(VisualThemeUtils.THEME_SAKURA);

        assertNotNull(sakura);
        assertEquals("樱雨粉", sakura.getDisplayName());
        assertEquals("使用樱色与雨纹层次，让设置页更柔和。", sakura.getShortDescription());
    }

    @Test
    public void settingsManager_repairsUnsupportedVisualThemeToDefault() {
        MemorySharedPreferences preferences = new MemorySharedPreferences();
        preferences.edit().putString("visual_theme", "external-fanart").apply();

        SettingsManager settingsManager = new SettingsManager(preferences);

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

        SettingsManager settingsManager = new SettingsManager(preferences);

        assertEquals(WeatherDisplayUtils.TEMPERATURE_CELSIUS, settingsManager.getTemperatureUnit());
        assertEquals(WeatherDisplayUtils.TEMPERATURE_CELSIUS, preferences.getString("temperature_unit", ""));
        assertEquals(WeatherDisplayUtils.WIND_SCALE, settingsManager.getWindUnit());
        assertEquals(WeatherDisplayUtils.WIND_SCALE, preferences.getString("wind_unit", ""));
    }

    @Test
    public void settingsManager_repairsWrongTypedVisualThemeToDefault() {
        MemorySharedPreferences preferences = new MemorySharedPreferences();
        preferences.edit().putBoolean("visual_theme", true).apply();

        SettingsManager settingsManager = new SettingsManager(preferences);

        assertEquals(VisualThemeUtils.THEME_SKY, settingsManager.getVisualTheme());
        assertEquals(VisualThemeUtils.THEME_SKY, preferences.getString("visual_theme", ""));
    }

    @Test
    public void settingsManager_repairsWrongTypedUnitsToDefaults() {
        MemorySharedPreferences preferences = new MemorySharedPreferences();
        preferences.edit()
                .putBoolean("temperature_unit", true)
                .putInt("wind_unit", 2)
                .apply();

        SettingsManager settingsManager = new SettingsManager(preferences);

        assertEquals(WeatherDisplayUtils.TEMPERATURE_CELSIUS, settingsManager.getTemperatureUnit());
        assertEquals(WeatherDisplayUtils.TEMPERATURE_CELSIUS, preferences.getString("temperature_unit", ""));
        assertEquals(WeatherDisplayUtils.WIND_SCALE, settingsManager.getWindUnit());
        assertEquals(WeatherDisplayUtils.WIND_SCALE, preferences.getString("wind_unit", ""));
    }

    @Test
    public void settingsManager_persistsDeveloperToolsEnabled() {
        SettingsManager settingsManager = new SettingsManager(new MemorySharedPreferences());

        assertEquals(false, settingsManager.isDeveloperToolsEnabled());

        settingsManager.setDeveloperToolsEnabled(true);
        assertEquals(true, settingsManager.isDeveloperToolsEnabled());

        settingsManager.setDeveloperToolsEnabled(false);
        assertEquals(false, settingsManager.isDeveloperToolsEnabled());
    }

    @Test
    public void settingsManager_persistsHomeBlockLayoutPerTheme() {
        SettingsManager settingsManager = new SettingsManager(new MemorySharedPreferences());

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
        assertEquals(true, settingsManager.isHomeBlockEnabled(VisualThemeUtils.THEME_FANTASY, HomeBlock.HOURLY_FORECAST));
    }

    @Test
    public void settingsManager_resetsHomeBlockLayoutForTheme() {
        SettingsManager settingsManager = new SettingsManager(new MemorySharedPreferences());
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
        new SettingsManager(new MemorySharedPreferences()).setTemperatureUnit("K");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setWindUnit_rejectsUnsupportedUnit() {
        new SettingsManager(new MemorySharedPreferences()).setWindUnit("KNOT");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setVisualTheme_rejectsUnsupportedTheme() {
        new SettingsManager(new MemorySharedPreferences()).setVisualTheme("external-fanart");
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
