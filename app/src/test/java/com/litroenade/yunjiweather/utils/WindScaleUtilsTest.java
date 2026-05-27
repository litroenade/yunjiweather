package com.litroenade.yunjiweather.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WindScaleUtilsTest {

    @Test
    public void toWindScale_returnsCalmAtZeroSpeed() {
        assertEquals(0, WindScaleUtils.toWindScale(0.0d));
    }

    @Test
    public void toWindScale_returnsLevelOneAtBoundarySpeed() {
        assertEquals(1, WindScaleUtils.toWindScale(1.0d));
    }

    @Test
    public void toWindScale_returnsLevelThreeForModerateSpeed() {
        assertEquals(3, WindScaleUtils.toWindScale(19.0d));
    }

    @Test
    public void toWindScale_returnsLevelSixAtStrongWindBoundary() {
        assertEquals(6, WindScaleUtils.toWindScale(39.0d));
    }

    @Test
    public void parseDisplayScale_returnsFirstLevelForQWeatherRange() {
        assertEquals(1, WindScaleUtils.parseDisplayScale("1-3"));
        assertEquals(5, WindScaleUtils.parseDisplayScale("5-6"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseDisplayScale_rejectsBlankText() {
        WindScaleUtils.parseDisplayScale(" ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void toWindScale_rejectsNegativeSpeed() {
        WindScaleUtils.toWindScale(-0.1d);
    }
}
