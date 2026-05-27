package com.litroenade.yunjiweather.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SunriseSunsetProgressTest {

    @Test
    public void calculateReturnsBeforeSunriseAtStart() {
        SunProgressState state = SunriseSunsetProgress.calculate("06:00", "18:00", "05:30");

        assertTrue(state.isAvailable());
        assertEquals(0f, state.getProgress(), 0.0001f);
        assertEquals("日出前", state.getPhaseText());
    }

    @Test
    public void calculateReturnsMiddayProgress() {
        SunProgressState state = SunriseSunsetProgress.calculate("06:00", "18:00", "12:00");

        assertTrue(state.isAvailable());
        assertEquals(0.5f, state.getProgress(), 0.0001f);
        assertEquals("白昼中", state.getPhaseText());
    }

    @Test
    public void calculateReturnsAfterSunsetAtEnd() {
        SunProgressState state = SunriseSunsetProgress.calculate("06:00", "18:00", "21:00");

        assertTrue(state.isAvailable());
        assertEquals(1f, state.getProgress(), 0.0001f);
        assertEquals("已日落", state.getPhaseText());
    }

    @Test
    public void calculateReturnsUnavailableForInvalidTime() {
        SunProgressState state = SunriseSunsetProgress.calculate("bad", "18:00", "12:00");

        assertFalse(state.isAvailable());
        assertEquals(0f, state.getProgress(), 0.0001f);
        assertEquals("暂无日照数据", state.getPhaseText());
    }
}
