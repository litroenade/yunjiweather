package com.litroenade.yunjiweather.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AirQualityUtilsTest {

    @Test
    public void activityAdviceMatchesAqiHealthRange() {
        assertEquals("空气很好，适合户外活动。", AirQualityUtils.activityAdviceForUsAqi(42));
        assertEquals("建议减少长时间户外活动。", AirQualityUtils.activityAdviceForUsAqi(180));
        assertEquals("尽量留在室内并关闭门窗。", AirQualityUtils.activityAdviceForUsAqi(320));
    }

    @Test
    public void sensitiveGroupAdviceMatchesAqiHealthRange() {
        assertEquals("敏感人群可正常安排日常活动。", AirQualityUtils.sensitiveGroupAdviceForUsAqi(42));
        assertEquals("老人、儿童和心肺敏感人群建议减少户外停留。", AirQualityUtils.sensitiveGroupAdviceForUsAqi(180));
    }

    @Test
    public void parseUsAqiDisplayRoundsDecimalDisplayValue() {
        assertEquals(42, AirQualityUtils.parseUsAqiDisplay("42.0"));
        assertEquals(181, AirQualityUtils.parseUsAqiDisplay("180.6"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseUsAqiDisplayRejectsBlankValue() {
        AirQualityUtils.parseUsAqiDisplay(" ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void activityAdviceRejectsNegativeAqi() {
        AirQualityUtils.activityAdviceForUsAqi(-1);
    }
}
