package com.litroenade.yunjiweather.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DateTimeUtilsTest {

    @Test
    public void isCacheExpired_returnsFalseBeforeExpireTime() {
        assertFalse(DateTimeUtils.isCacheExpired(1_000L, 1_999L));
    }

    @Test
    public void isCacheExpired_returnsTrueAtExpireBoundary() {
        assertTrue(DateTimeUtils.isCacheExpired(2_000L, 2_000L));
    }

    @Test
    public void formatCacheUpdateTime_returnsChineseMinuteText() {
        String text = DateTimeUtils.formatCacheUpdateTime(1_700_000_000_000L);

        assertEquals("缓存更新时间：2023-11-15 06:13", text);
    }

    @Test
    public void formatMinuteTime_returnsChineseMinuteText() {
        String text = DateTimeUtils.formatMinuteTime(1_700_000_000_000L);

        assertEquals("2023-11-15 06:13", text);
    }

    @Test
    public void parseOpenMeteoLocalTime_returnsEpochMillisInChinaTimeZone() {
        long time = DateTimeUtils.parseOpenMeteoLocalTime("2020-06-30T22:00");

        assertEquals(1_593_525_600_000L, time);
    }
}
