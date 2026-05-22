package com.litroenade.yunjiweather.utils;

import android.view.View;

import androidx.annotation.DrawableRes;

import com.litroenade.yunjiweather.R;

public final class VisualThemeUtils {

    public static final String THEME_SKY = "sky";
    public static final String THEME_FANTASY = "fantasy";
    public static final String THEME_SAKURA = "sakura";

    private VisualThemeUtils() {
    }

    @DrawableRes
    public static int resolveAppBackground(String themeKey) {
        validateThemeKey(themeKey);
        if (THEME_FANTASY.equals(themeKey)) {
            return R.drawable.bg_app_fantasy_night;
        }
        if (THEME_SAKURA.equals(themeKey)) {
            return R.drawable.bg_app_sakura_rain;
        }
        return R.drawable.bg_app_soft;
    }

    public static void applyAppBackground(View root, String themeKey) {
        root.setBackgroundResource(resolveAppBackground(themeKey));
    }

    public static void validateThemeKey(String themeKey) {
        if (!isSupportedTheme(themeKey)) {
            throw new IllegalArgumentException("不支持的个性化主题：" + themeKey);
        }
    }

    public static boolean isSupportedTheme(String themeKey) {
        return THEME_SKY.equals(themeKey)
                || THEME_FANTASY.equals(themeKey)
                || THEME_SAKURA.equals(themeKey);
    }
}
