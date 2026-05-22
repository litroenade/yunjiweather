package com.litroenade.yunjiweather.utils;

import com.litroenade.yunjiweather.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class VisualThemeCatalog {

    private static final List<VisualTheme> THEMES = Collections.unmodifiableList(Arrays.asList(
            new VisualTheme(
                    VisualThemeUtils.THEME_SKY,
                    "经典晴空",
                    "首页跟随晴雨雪动态变化，保留天气应用的清爽感。",
                    R.drawable.bg_app_soft,
                    R.color.weather_primary,
                    R.color.weather_text_inverse,
                    R.color.weather_text_inverse
            ),
            new VisualTheme(
                    VisualThemeUtils.THEME_FANTASY,
                    "幻想夜",
                    "以深蓝夜色和鸟居剪影为主，适合暗色展示。",
                    R.drawable.bg_app_fantasy_night,
                    R.color.weather_primary,
                    R.color.weather_text_inverse,
                    R.color.weather_text_inverse
            ),
            new VisualTheme(
                    VisualThemeUtils.THEME_SAKURA,
                    "樱雨粉",
                    "使用樱色与雨纹叠层，让设置页和日历更柔和。",
                    R.drawable.bg_app_sakura_rain,
                    R.color.weather_accent,
                    R.color.weather_text_primary,
                    R.color.weather_text_secondary
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
