package com.litroenade.yunjiweather.widget;

import com.litroenade.yunjiweather.data.model.CustomThemeProfile;
import com.litroenade.yunjiweather.utils.VisualThemeUtils;

public final class WidgetThemeSettings {

    private final String visualThemeKey;
    private final CustomThemeProfile customThemeProfile;

    public WidgetThemeSettings(String visualThemeKey, CustomThemeProfile customThemeProfile) {
        this.visualThemeKey = VisualThemeUtils.normalizeThemeKey(visualThemeKey);
        this.customThemeProfile = customThemeProfile == null ? CustomThemeProfile.empty() : customThemeProfile;
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
}
