package com.litroenade.yunjiweather.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Stable catalog for the personalization slots shown in Settings. Keep order changes
 * intentional because the UI and tests rely on this sequence.
 */
public final class VisualThemeCatalog {

    private static final List<VisualTheme> THEMES = Collections.unmodifiableList(Arrays.asList(
            new VisualTheme(
                    VisualThemeUtils.THEME_SKY,
                    "默认主题",
                    "使用云迹天气默认配色和首页模块。"
            ),
            new VisualTheme(
                    VisualThemeUtils.THEME_PANORAMA,
                    "全景天气",
                    "沉浸式动态天气，强化真实光影、雨雪和风场层次。"
            ),
            new VisualTheme(
                    VisualThemeUtils.THEME_FANTASY,
                    "幻想乡",
                    "空位：后续用于幻想风格皮肤。",
                    true,
                    false
            ),
            new VisualTheme(
                    VisualThemeUtils.THEME_CUSTOM_1,
                    "空位",
                    "预留给后续自建主题。",
                    true,
                    false
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
        VisualTheme theme = findByKey(themeKey);
        return theme != null && theme.isSelectable();
    }
}
