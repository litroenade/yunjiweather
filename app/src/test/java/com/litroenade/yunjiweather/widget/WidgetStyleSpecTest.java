package com.litroenade.yunjiweather.widget;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WidgetStyleSpecTest {

    @Test
    public void specsExposePinnedSizesAndPreviewSizesForEveryMode() {
        WidgetStyleSpec compact = WidgetStyleSpec.forMode(WeatherWidgetLayoutMode.COMPACT);
        WidgetStyleSpec standard = WidgetStyleSpec.forMode(WeatherWidgetLayoutMode.STANDARD);
        WidgetStyleSpec expanded = WidgetStyleSpec.forMode(WeatherWidgetLayoutMode.EXPANDED);

        assertEquals(140, compact.getMinWidthDp());
        assertEquals(140, compact.getMinHeightDp());
        assertEquals(220, standard.getMinWidthDp());
        assertEquals(140, standard.getMinHeightDp());
        assertEquals(300, expanded.getMinWidthDp());
        assertEquals(150, expanded.getMinHeightDp());

        assertTrue(compact.getPreviewWidthDp() < standard.getPreviewWidthDp());
        assertTrue(standard.getPreviewWidthDp() < expanded.getPreviewWidthDp());
        assertEquals(compact.getPreviewWidthDp(), compact.getPreviewHeightDp());
        assertTrue(expanded.isAdviceVisible());
    }

    @Test
    public void layoutModeBreakpointsUseWidgetSpecs() {
        assertEquals(WeatherWidgetLayoutMode.COMPACT, WidgetStyleSpec.modeForSize(180, 96));
        assertEquals(WeatherWidgetLayoutMode.STANDARD, WidgetStyleSpec.modeForSize(220, 140));
        assertEquals(WeatherWidgetLayoutMode.EXPANDED, WidgetStyleSpec.modeForSize(300, 150));
        assertEquals(WeatherWidgetLayoutMode.STANDARD, WeatherWidgetLayoutMode.fromSize(220, 140));
    }
}
