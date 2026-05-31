package com.litroenade.yunjiweather.widget;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WeatherWidgetLayoutModeTest {

    @Test
    public void fromSizeReturnsCompactForNarrowWidget() {
        assertEquals(WeatherWidgetLayoutMode.COMPACT, WeatherWidgetLayoutMode.fromSize(180, 96));
    }

    @Test
    public void fromSizeReturnsStandardForMediumWidget() {
        assertEquals(WeatherWidgetLayoutMode.STANDARD, WeatherWidgetLayoutMode.fromSize(240, 140));
    }

    @Test
    public void fromSizeReturnsStandardWhenLauncherDoesNotReportOptions() {
        assertEquals(WeatherWidgetLayoutMode.STANDARD, WeatherWidgetLayoutMode.fromSize(0, 0));
    }

    @Test
    public void fromSizeDoesNotExpandNarrowTallWidget() {
        assertEquals(WeatherWidgetLayoutMode.COMPACT, WeatherWidgetLayoutMode.fromSize(180, 160));
    }

    @Test
    public void fromSizeReturnsExpandedForLargeWidget() {
        assertEquals(WeatherWidgetLayoutMode.EXPANDED, WeatherWidgetLayoutMode.fromSize(320, 160));
    }
}
