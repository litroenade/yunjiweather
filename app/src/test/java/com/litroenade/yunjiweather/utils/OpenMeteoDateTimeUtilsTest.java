package com.litroenade.yunjiweather.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OpenMeteoDateTimeUtilsTest {

    @Test
    public void parseOpenMeteoLocalTime_usesChinaTimeZoneForLocalTimestamp() {
        assertEquals(1_699_999_980_000L, DateTimeUtils.parseOpenMeteoLocalTime("2023-11-15T06:13"));
    }

    @Test
    public void formatOpenMeteoHour_returnsHourMinuteText() {
        assertEquals("06:13", DateTimeUtils.formatOpenMeteoHour("2023-11-15T06:13"));
    }

    @Test
    public void formatOpenMeteoDate_returnsMonthDayText() {
        assertEquals("11/15", DateTimeUtils.formatOpenMeteoDate("2023-11-15"));
    }
}
