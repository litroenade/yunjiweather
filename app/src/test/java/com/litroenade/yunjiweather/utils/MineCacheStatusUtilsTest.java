package com.litroenade.yunjiweather.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MineCacheStatusUtilsTest {

    @Test
    public void formatDataUpdateTime_returnsEmptyStateWhenNoCache() {
        String text = MineCacheStatusUtils.formatDataUpdateTime(null);

        assertEquals("数据更新时间：暂无缓存", text);
    }

    @Test
    public void formatDataUpdateTime_returnsFormattedMinuteTime() {
        long updateTime = 1_700_000_000_000L;

        String text = MineCacheStatusUtils.formatDataUpdateTime(updateTime);

        assertEquals("数据更新时间：2023-11-15 06:13", text);
    }
}
