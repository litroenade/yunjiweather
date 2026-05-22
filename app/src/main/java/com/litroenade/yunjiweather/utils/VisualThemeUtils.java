package com.litroenade.yunjiweather.utils;

import android.view.View;

import androidx.annotation.DrawableRes;

public final class VisualThemeUtils {

    public static final String THEME_SKY = "sky";
    public static final String THEME_FANTASY = "fantasy";
    public static final String THEME_SAKURA = "sakura";

    private VisualThemeUtils() {
    }

    @DrawableRes
    public static int resolveAppBackground(String themeKey) {
        return VisualThemeCatalog.getThemeOrDefault(themeKey).getBackgroundRes();
    }

    public static VisualTheme resolveTheme(String themeKey) {
        return VisualThemeCatalog.getThemeOrDefault(themeKey);
    }

    @DrawableRes
    public static int resolveHomeBackground(String themeKey, String iconCode) {
        String normalizedTheme = normalizeThemeKey(themeKey);
        if (THEME_SKY.equals(normalizedTheme)) {
            return WeatherIconUtils.getWeatherBackgroundRes(iconCode);
        }
        return resolveAppBackground(normalizedTheme);
    }

    @DrawableRes
    public static int resolveHomeBackground(String themeKey, @DrawableRes int weatherBackgroundRes) {
        String normalizedTheme = normalizeThemeKey(themeKey);
        if (THEME_SKY.equals(normalizedTheme)) {
            return weatherBackgroundRes;
        }
        return resolveAppBackground(normalizedTheme);
    }

    public static void applyAppBackground(View root, String themeKey) {
        root.setBackgroundResource(resolveAppBackground(themeKey));
    }

    public static void validateThemeKey(String themeKey) {
        if (!isSupportedTheme(themeKey)) {
            throw new IllegalArgumentException("不支持的个性化主题：" + themeKey);
        }
    }

    public static String normalizeThemeKey(String themeKey) {
        return VisualThemeCatalog.getThemeOrDefault(themeKey).getKey();
    }

    public static boolean isSupportedTheme(String themeKey) {
        return VisualThemeCatalog.isSupportedTheme(themeKey);
    }
}
