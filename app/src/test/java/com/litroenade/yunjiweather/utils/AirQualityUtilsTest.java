package com.litroenade.yunjiweather.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AirQualityUtilsTest {

    @Test
    public void toUsAqiCategory_returnsGoodAtUpperBoundary() {
        assertEquals("优", AirQualityUtils.toUsAqiCategory(50));
    }

    @Test
    public void toUsAqiCategory_returnsModerateAfterGoodBoundary() {
        assertEquals("良", AirQualityUtils.toUsAqiCategory(51));
    }

    @Test
    public void toUsAqiCategory_returnsSensitiveGroupTextAtBoundary() {
        assertEquals("对敏感人群不健康", AirQualityUtils.toUsAqiCategory(101));
    }

    @Test
    public void toUsAqiCategory_returnsHazardousAboveThreeHundred() {
        assertEquals("危险", AirQualityUtils.toUsAqiCategory(301));
    }

    @Test
    public void findPrimaryPollutant_returnsNameOfLargestSubIndex() {
        assertEquals("PM2.5", AirQualityUtils.findPrimaryPollutant(86.0d, 40.0d, 18.0d, 55.0d, 10.0d, 6.0d));
    }

    @Test(expected = IllegalArgumentException.class)
    public void toUsAqiCategory_rejectsNegativeValue() {
        AirQualityUtils.toUsAqiCategory(-1);
    }
}
