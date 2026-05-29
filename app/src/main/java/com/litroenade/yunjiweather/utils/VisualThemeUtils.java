package com.litroenade.yunjiweather.utils;

/**
 * 统一收敛个性化主题标识，兼容旧版本写入的主题值。
 */
public final class VisualThemeUtils {

    public static final String THEME_SKY = "sky";
    public static final String THEME_PANORAMA = "panorama";
    public static final String THEME_FANTASY = "fantasy";
    public static final String THEME_CUSTOM_1 = "custom_1";

    private static final String LEGACY_THEME_REAL_WEATHER = "real_weather";
    private static final String LEGACY_THEME_SAKURA = "sakura";
    private static final String LEGACY_THEME_CUSTOM_2 = "custom_2";

    private VisualThemeUtils() {
    }

    public static void validateThemeKey(String themeKey) {
        if (!isSupportedTheme(themeKey)) {
            throw new IllegalArgumentException("不支持的个性化主题：" + themeKey);
        }
    }

    public static String normalizeThemeKey(String themeKey) {
        // 四槽位主题模型上线前的旧值统一迁移到当前可展示主题。
        if (THEME_FANTASY.equals(themeKey)
                || LEGACY_THEME_REAL_WEATHER.equals(themeKey)
                || LEGACY_THEME_SAKURA.equals(themeKey)) {
            return THEME_PANORAMA;
        }
        if (LEGACY_THEME_CUSTOM_2.equals(themeKey)) {
            return THEME_SKY;
        }
        return VisualThemeCatalog.getThemeOrDefault(themeKey).getKey();
    }

    public static boolean isSupportedTheme(String themeKey) {
        return VisualThemeCatalog.isSupportedTheme(themeKey);
    }
}
