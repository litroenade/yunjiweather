package com.litroenade.yunjiweather.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LocalStorageSummaryUtilsTest {

    @Test
    public void formatSummary_withUserData_returnsConcreteCounts() {
        String summary = LocalStorageSummaryUtils.formatSummary(true, 2, 5, 1);

        assertEquals("本地账户：已保存\n城市数据：2 个\n天气缓存：5 条\n预警记录：1 条", summary);
    }

    @Test
    public void formatSummary_withoutUserData_returnsMissingAccountText() {
        String summary = LocalStorageSummaryUtils.formatSummary(false, 0, 0, 0);

        assertEquals("本地账户：未找到\n城市数据：0 个\n天气缓存：0 条\n预警记录：0 条", summary);
    }
}
