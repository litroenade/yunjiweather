package com.litroenade.yunjiweather.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ThemeModeUtilsTest {

    @Test
    public void resolveNightMode_usesProvidedModeValues() {
        assertEquals(2, ThemeModeUtils.resolveNightMode(true, 2, 1));
        assertEquals(1, ThemeModeUtils.resolveNightMode(false, 2, 1));
    }

    @Test
    public void shouldApplyNightMode_ignoresSameMode() {
        assertFalse(ThemeModeUtils.shouldApplyNightMode(2, 2));
        assertTrue(ThemeModeUtils.shouldApplyNightMode(1, 2));
    }
}
