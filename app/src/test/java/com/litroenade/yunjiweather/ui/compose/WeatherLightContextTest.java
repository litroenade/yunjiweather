package com.litroenade.yunjiweather.ui.compose;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WeatherLightContextTest {

    @Test
    public void fromMinuteOfDay_returnsDawnAroundSunrise() {
        WeatherLightContext context = WeatherLightContext.fromMinuteOfDay("06:00", "18:00", 360);

        assertEquals(WeatherLightContext.Phase.DAWN, context.getPhase());
        assertFalse(context.isNight());
        assertTrue(context.getWarmth() > 0.70f);
    }

    @Test
    public void fromMinuteOfDay_returnsDayWithMiddayExposure() {
        WeatherLightContext context = WeatherLightContext.fromMinuteOfDay("06:00", "18:00", 720);

        assertEquals(WeatherLightContext.Phase.DAY, context.getPhase());
        assertEquals(0.5f, context.getDayProgress(), 0.01f);
        assertTrue(context.getExposure() > 0.85f);
    }

    @Test
    public void fromMinuteOfDay_returnsDuskAroundSunset() {
        WeatherLightContext context = WeatherLightContext.fromMinuteOfDay("06:00", "18:00", 1090);

        assertEquals(WeatherLightContext.Phase.DUSK, context.getPhase());
        assertTrue(context.getWarmth() > 0.70f);
    }

    @Test
    public void fromMinuteOfDay_fallsBackWhenSunTimesAreMissing() {
        WeatherLightContext context = WeatherLightContext.fromMinuteOfDay("", "", 1320);

        assertEquals(WeatherLightContext.Phase.NIGHT, context.getPhase());
        assertTrue(context.isNight());
    }
}
