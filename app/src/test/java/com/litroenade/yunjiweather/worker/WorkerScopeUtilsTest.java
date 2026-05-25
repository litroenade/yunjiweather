package com.litroenade.yunjiweather.worker;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class WorkerScopeUtilsTest {

    @Test
    public void workNames_areStableWithoutUserScope() {
        assertEquals("weather_alert_check", WorkerScopeUtils.weatherAlertWorkName());
        assertEquals("daily_weather_reminder", WorkerScopeUtils.dailyWeatherWorkName());
        assertFalse(WorkerScopeUtils.weatherAlertWorkName().contains("user"));
        assertFalse(WorkerScopeUtils.dailyWeatherWorkName().contains("user"));
    }
}
