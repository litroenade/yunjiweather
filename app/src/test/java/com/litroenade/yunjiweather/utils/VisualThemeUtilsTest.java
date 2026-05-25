package com.litroenade.yunjiweather.utils;

import android.content.SharedPreferences;

import com.litroenade.yunjiweather.R;
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
    public void resolveAppBackground_returnsSkyBackground() {
        assertEquals(R.drawable.bg_app_soft, VisualThemeUtils.resolveAppBackground(VisualThemeUtils.THEME_SKY));
    }

    @Test
    public void resolveAppBackground_returnsFantasyBackground() {
        assertEquals(R.drawable.bg_app_fantasy_night, VisualThemeUtils.resolveAppBackground(VisualThemeUtils.THEME_FANTASY));
    }

    @Test
    public void resolveHomeBackground_usesWeatherBackgroundForSkyTheme() {
        assertEquals(
                R.drawable.bg_weather_rain,
                VisualThemeUtils.resolveHomeBackground(VisualThemeUtils.THEME_SKY, R.drawable.bg_weather_rain)
        );
    }

    @Test
    public void resolveHomeBackground_usesThemeBackgroundForFantasyTheme() {
        assertEquals(
                R.drawable.bg_app_fantasy_night,
                VisualThemeUtils.resolveHomeBackground(VisualThemeUtils.THEME_FANTASY, R.drawable.bg_weather_rain)
        );
    }

    @Test
    public void resolveHomeBackground_usesThemeBackgroundForSakuraTheme() {
        assertEquals(
                R.drawable.bg_app_sakura_rain,
                VisualThemeUtils.resolveHomeBackground(VisualThemeUtils.THEME_SAKURA, R.drawable.bg_weather_snow)
        );
    }

    @Test
    public void resolveHomeForeground_usesDarkTextForLightSkyWeatherBackgrounds() {
        assertEquals(
                R.color.weather_text_primary,
                VisualThemeUtils.resolveHomePrimaryTextColor(VisualThemeUtils.THEME_SKY, "100")
        );
        assertEquals(
                R.color.weather_text_secondary,
                VisualThemeUtils.resolveHomeSecondaryTextColor(VisualThemeUtils.THEME_SKY, "100")
        );
        assertEquals(
                R.color.weather_text_primary,
                VisualThemeUtils.resolveHomePrimaryTextColor(VisualThemeUtils.THEME_SKY, "101")
        );
        assertEquals(
                R.color.weather_text_secondary,
                VisualThemeUtils.resolveHomeSecondaryTextColor(VisualThemeUtils.THEME_SKY, "101")
        );
        assertEquals(
                R.color.weather_text_primary,
                VisualThemeUtils.resolveHomePrimaryTextColor(VisualThemeUtils.THEME_SKY, "401")
        );
        assertEquals(
                R.color.weather_text_secondary,
                VisualThemeUtils.resolveHomeSecondaryTextColor(VisualThemeUtils.THEME_SKY, "401")
        );
        assertEquals(
                R.color.weather_text_primary,
                VisualThemeUtils.resolveHomePrimaryTextColor(VisualThemeUtils.THEME_SKY, "305")
        );
        assertEquals(
                R.color.weather_text_secondary,
                VisualThemeUtils.resolveHomeSecondaryTextColor(VisualThemeUtils.THEME_SKY, "305")
        );
    }

    @Test
    public void resolveHomeForeground_usesLightTextForDarkSkyWeatherBackgrounds() {
        assertEquals(
                R.color.weather_text_inverse,
                VisualThemeUtils.resolveHomePrimaryTextColor(VisualThemeUtils.THEME_SKY, "150")
        );
        assertEquals(
                R.color.weather_header_secondary,
                VisualThemeUtils.resolveHomeSecondaryTextColor(VisualThemeUtils.THEME_SKY, "150")
        );
    }

    @Test
    public void resolveHomeForeground_usesThemeTextForFixedThemes() {
        assertEquals(
                R.color.weather_text_inverse,
                VisualThemeUtils.resolveHomePrimaryTextColor(VisualThemeUtils.THEME_FANTASY, "100")
        );
        assertEquals(
                R.color.weather_text_inverse,
                VisualThemeUtils.resolveHomeSecondaryTextColor(VisualThemeUtils.THEME_FANTASY, "100")
        );
        assertEquals(
                R.color.weather_text_primary,
                VisualThemeUtils.resolveHomePrimaryTextColor(VisualThemeUtils.THEME_SAKURA, "305")
        );
        assertEquals(
                R.color.weather_text_secondary,
                VisualThemeUtils.resolveHomeSecondaryTextColor(VisualThemeUtils.THEME_SAKURA, "305")
        );
    }

    @Test
    public void resolveAppBackground_fallsBackToDefaultThemeForUnsupportedTheme() {
        assertEquals(R.drawable.bg_app_soft, VisualThemeUtils.resolveAppBackground("official-touhou-image"));
    }

    @Test
    public void isSupportedTheme_returnsFalseForExternalImageKey() {
        assertEquals(false, VisualThemeUtils.isSupportedTheme("external-fanart"));
    }

    @Test
    public void catalog_returnsThemesInStableOrder() {
        List<VisualTheme> themes = VisualThemeCatalog.getThemes();

        assertEquals(3, themes.size());
        assertEquals(VisualThemeUtils.THEME_SKY, themes.get(0).getKey());
        assertEquals(VisualThemeUtils.THEME_FANTASY, themes.get(1).getKey());
        assertEquals(VisualThemeUtils.THEME_SAKURA, themes.get(2).getKey());
    }

    @Test
    public void catalog_containsSakuraThemeModel() {
        VisualTheme sakura = VisualThemeCatalog.findByKey(VisualThemeUtils.THEME_SAKURA);

        assertNotNull(sakura);
        assertEquals("樱雨粉", sakura.getDisplayName());
        assertEquals(R.drawable.bg_app_sakura_rain, sakura.getBackgroundRes());
        assertEquals(R.color.weather_accent, sakura.getAccentColorRes());
        assertEquals(R.color.weather_text_primary, sakura.getHomePrimaryTextColorRes());
        assertEquals(R.color.weather_text_secondary, sakura.getHomeSecondaryTextColorRes());
    }

    @Test
    public void catalog_usesThemeDefaultHomeTextColors() {
        VisualTheme sky = VisualThemeCatalog.findByKey(VisualThemeUtils.THEME_SKY);
        VisualTheme fantasy = VisualThemeCatalog.findByKey(VisualThemeUtils.THEME_FANTASY);

        assertNotNull(sky);
        assertNotNull(fantasy);
        assertEquals(R.color.weather_text_primary, sky.getHomePrimaryTextColorRes());
        assertEquals(R.color.weather_text_secondary, sky.getHomeSecondaryTextColorRes());
        assertEquals(R.color.weather_text_inverse, fantasy.getHomePrimaryTextColorRes());
        assertEquals(R.color.weather_text_inverse, fantasy.getHomeSecondaryTextColorRes());
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
