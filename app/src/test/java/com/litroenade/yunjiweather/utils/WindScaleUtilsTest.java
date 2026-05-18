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

    @Test(expected = IllegalArgumentException.class)
    public void toWindScale_rejectsNegativeSpeed() {
        WindScaleUtils.toWindScale(-0.1d);
    }
}
