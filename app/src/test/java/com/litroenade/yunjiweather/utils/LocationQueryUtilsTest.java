package com.litroenade.yunjiweather.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LocationQueryUtilsTest {

    @Test
    public void formatQWeatherLocationQuery_returnsLongitudeThenLatitudeWithTwoDecimals() {
        String query = LocationQueryUtils.formatQWeatherLocationQuery(39.9042, 116.4074);

        assertEquals("116.41,39.90", query);
    }

    @Test(expected = IllegalArgumentException.class)
    public void formatQWeatherLocationQuery_rejectsInvalidLatitude() {
        LocationQueryUtils.formatQWeatherLocationQuery(120.0, 116.4074);
    }

    @Test(expected = IllegalArgumentException.class)
    public void formatQWeatherLocationQuery_rejectsInvalidLongitude() {
        LocationQueryUtils.formatQWeatherLocationQuery(39.9042, 200.0);
    }
}
