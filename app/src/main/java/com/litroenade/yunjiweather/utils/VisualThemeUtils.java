package com.litroenade.yunjiweather.utils;

public final class VisualThemeUtils {

    public static final String THEME_SKY = "sky";
    public static final String THEME_FANTASY = "fantasy";
    public static final String THEME_SAKURA = "sakura";

    private VisualThemeUtils() {
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
