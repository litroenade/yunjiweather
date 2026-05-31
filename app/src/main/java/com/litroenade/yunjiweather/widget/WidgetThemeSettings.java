package com.litroenade.yunjiweather.widget;

import com.litroenade.yunjiweather.data.model.CustomThemeProfile;
import com.litroenade.yunjiweather.utils.VisualThemeUtils;
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils;

public final class WidgetThemeSettings {

    private final String visualThemeKey;
    private final CustomThemeProfile customThemeProfile;
    private final String temperatureUnit;

    public WidgetThemeSettings(String visualThemeKey, CustomThemeProfile customThemeProfile) {
        this(visualThemeKey, customThemeProfile, WeatherDisplayUtils.TEMPERATURE_CELSIUS);
    }

    public WidgetThemeSettings(String visualThemeKey, CustomThemeProfile customThemeProfile, String temperatureUnit) {
        this.visualThemeKey = VisualThemeUtils.normalizeThemeKey(visualThemeKey);
        this.customThemeProfile = customThemeProfile == null ? CustomThemeProfile.empty() : customThemeProfile;
        this.temperatureUnit = WeatherDisplayUtils.normalizeTemperatureUnit(temperatureUnit);
    }

    public static WidgetThemeSettings defaults() {
        return new WidgetThemeSettings(VisualThemeUtils.THEME_SKY, CustomThemeProfile.empty());
    }

    public String getVisualThemeKey() {
        return visualThemeKey;
    }

    public CustomThemeProfile getCustomThemeProfile() {
        return customThemeProfile;
    }

    public String getTemperatureUnit() {
        return temperatureUnit;
    }
}
