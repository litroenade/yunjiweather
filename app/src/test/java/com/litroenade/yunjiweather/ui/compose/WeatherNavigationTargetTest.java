package com.litroenade.yunjiweather.ui.compose;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class WeatherNavigationTargetTest {

    @Test
    public void searchCity_staysInSheet() {
        assertSame(WeatherNavigationTarget.Surface.SHEET, WeatherNavigationTarget.SEARCH_CITY.getSurface());
        assertFalse(WeatherNavigationTarget.SEARCH_CITY.isFullPage());
    }

    @Test
    public void menuDestinations_openAsFullPages() {
        assertFullPage(WeatherNavigationTarget.MANAGE_CITIES);
        assertFullPage(WeatherNavigationTarget.DESKTOP_WEATHER);
        assertFullPage(WeatherNavigationTarget.PERSONALIZATION);
        assertFullPage(WeatherNavigationTarget.SETTINGS);
        assertFullPage(WeatherNavigationTarget.ALERTS);
        assertFullPage(WeatherNavigationTarget.LIFE_INDEX);
    }

    private static void assertFullPage(WeatherNavigationTarget target) {
        assertSame(WeatherNavigationTarget.Surface.FULL_PAGE, target.getSurface());
        assertTrue(target.isFullPage());
    }
}
