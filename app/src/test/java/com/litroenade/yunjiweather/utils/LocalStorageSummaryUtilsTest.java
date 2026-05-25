package com.litroenade.yunjiweather.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LocalStorageSummaryUtilsTest {

    @Test
    public void formatSummary_returnsConcreteCountsWithoutAccountState() {
        String summary = LocalStorageSummaryUtils.formatSummary(2, 5, 1);

        assertEquals("城市数据：2 个\n天气缓存：5 条\n预警记录：1 条", summary);
    }

    @Test
    public void formatSummary_acceptsEmptyLocalState() {
        String summary = LocalStorageSummaryUtils.formatSummary(0, 0, 0);

        assertEquals("城市数据：0 个\n天气缓存：0 条\n预警记录：0 条", summary);
    }
}
