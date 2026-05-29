package com.litroenade.yunjiweather.utils;

import java.util.List;

/**
 * 个性化页面的稳定主题目录。
 * 顺序会直接影响界面展示和测试断言，因此新增主题时要显式维护。
 */
public final class VisualThemeCatalog {

    private static final VisualTheme DEFAULT_THEME = new VisualTheme(
            VisualThemeUtils.THEME_SKY,
            "默认主题",
            "使用云迹天气默认配色和首页模块。"
    );
    private static final VisualTheme PANORAMA_THEME = new VisualTheme(
            VisualThemeUtils.THEME_PANORAMA,
            "全景天气",
            "沉浸式动态天气，强化真实光影、雨雪和风场层次。"
    );
    private static final VisualTheme FANTASY_THEME = new VisualTheme(
            VisualThemeUtils.THEME_FANTASY,
            "幻想乡",
            "暂不开放：后续用于幻想风格皮肤。",
            true,
            false
    );
    private static final VisualTheme CUSTOM_THEME = new VisualTheme(
            VisualThemeUtils.THEME_CUSTOM_1,
            "自定义主题",
            "上传图片并选择裁剪位置。",
            true,
            true
    );

    private static final List<VisualTheme> ALL_THEMES = List.of(DEFAULT_THEME, PANORAMA_THEME, FANTASY_THEME, CUSTOM_THEME);

    private static final List<VisualTheme> THEMES = List.of(DEFAULT_THEME, PANORAMA_THEME, CUSTOM_THEME);

    private VisualThemeCatalog() {
    }

    public static List<VisualTheme> getThemes() {
        return THEMES;
    }

    public static VisualTheme defaultTheme() {
        return THEMES.get(0);
    }

    public static VisualTheme findByKey(String themeKey) {
        for (VisualTheme theme : ALL_THEMES) {
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
