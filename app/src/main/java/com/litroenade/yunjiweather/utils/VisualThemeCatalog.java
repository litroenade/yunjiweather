package com.litroenade.yunjiweather.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class VisualThemeCatalog {

    private static final List<VisualTheme> THEMES = Collections.unmodifiableList(Arrays.asList(
            new VisualTheme(
                    VisualThemeUtils.THEME_SKY,
                    "经典晴空",
                    "跟随晴雨雪动态变化，保留天气应用的清爽感。"
            ),
            new VisualTheme(
                    VisualThemeUtils.THEME_FANTASY,
                    "幻想夜",
                    "以深色夜景和剪影层次为主，适合暗色展示。"
            ),
            new VisualTheme(
                    VisualThemeUtils.THEME_SAKURA,
                    "樱雨粉",
                    "使用樱色与雨纹层次，让设置页更柔和。"
            )
    ));

    private VisualThemeCatalog() {
    }

    public static List<VisualTheme> getThemes() {
        return THEMES;
    }

    public static VisualTheme defaultTheme() {
        return THEMES.get(0);
    }

    public static VisualTheme findByKey(String themeKey) {
        for (VisualTheme theme : THEMES) {
            if (theme.getKey().equals(themeKey)) {
                return theme;
            }
        }
        return null;
    }

    public static VisualTheme getThemeOrDefault(String themeKey) {
        VisualTheme theme = findByKey(themeKey);
        if (theme == null) {
            return defaultTheme();
        }
        return theme;
    }

    public static boolean isSupportedTheme(String themeKey) {
        return findByKey(themeKey) != null;
    }
}
