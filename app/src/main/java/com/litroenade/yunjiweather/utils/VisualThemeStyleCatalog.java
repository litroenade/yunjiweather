package com.litroenade.yunjiweather.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class VisualThemeStyleCatalog {

    public static final String STYLE_DEFAULT = "default";
    public static final String STYLE_SOFT_MIST = "soft_mist";
    public static final String STYLE_SUNSET = "sunset";
    public static final String STYLE_DEEP_SEA = "deep_sea";

    private static final List<VisualThemeStyle> STYLES = Collections.unmodifiableList(Arrays.asList(
            new VisualThemeStyle(STYLE_DEFAULT, "跟随主题", "使用当前主题的默认色彩和天气背景。"),
            new VisualThemeStyle(STYLE_SOFT_MIST, "柔雾", "降低对比度，背景更轻、更适合浅色阅读。"),
            new VisualThemeStyle(STYLE_SUNSET, "暖霞", "加入暖色层次，适合傍晚和柔和氛围。"),
            new VisualThemeStyle(STYLE_DEEP_SEA, "深海", "强化冷色和暗色层次，适合沉浸式天气页。")
    ));

    private VisualThemeStyleCatalog() {
    }

    public static List<VisualThemeStyle> getStyles() {
        return STYLES;
    }

    public static VisualThemeStyle defaultStyle() {
        return STYLES.get(0);
    }

    public static VisualThemeStyle findByKey(String styleKey) {
        for (VisualThemeStyle style : STYLES) {
            if (style.getKey().equals(styleKey)) {
                return style;
            }
        }
        return null;
    }

    public static VisualThemeStyle getStyleOrDefault(String styleKey) {
        VisualThemeStyle style = findByKey(styleKey);
        if (style == null) {
            return defaultStyle();
        }
        return style;
    }

    public static boolean isSupportedStyle(String styleKey) {
        return findByKey(styleKey) != null;
    }
}
