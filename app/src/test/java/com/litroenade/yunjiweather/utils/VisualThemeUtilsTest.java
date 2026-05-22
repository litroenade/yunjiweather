package com.litroenade.yunjiweather.utils;

import com.litroenade.yunjiweather.R;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VisualThemeUtilsTest {

    @Test
    public void resolveAppBackground_returnsSkyBackground() {
        assertEquals(R.drawable.bg_app_soft, VisualThemeUtils.resolveAppBackground(VisualThemeUtils.THEME_SKY));
    }

    @Test
    public void resolveAppBackground_returnsFantasyBackground() {
        assertEquals(R.drawable.bg_app_fantasy_night, VisualThemeUtils.resolveAppBackground(VisualThemeUtils.THEME_FANTASY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveAppBackground_rejectsUnsupportedTheme() {
        VisualThemeUtils.resolveAppBackground("official-touhou-image");
    }

    @Test
    public void isSupportedTheme_returnsFalseForExternalImageKey() {
        assertEquals(false, VisualThemeUtils.isSupportedTheme("external-fanart"));
    }
}
