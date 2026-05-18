package com.litroenade.yunjiweather.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CityListUtilsTest {

    @Test
    public void addCity_rejectsDuplicateCityAfterTrim() {
        List<String> cities = Arrays.asList("北京", "上海");

        CityListUtils.AddResult result = CityListUtils.addCity(cities, " 北京 ");

        assertFalse(result.isAdded());
        assertEquals(Arrays.asList("北京", "上海"), result.getCities());
    }

    @Test
    public void addCity_appendsNewCity() {
        CityListUtils.AddResult result = CityListUtils.addCity(Arrays.asList("北京"), "广州");

        assertTrue(result.isAdded());
        assertEquals(Arrays.asList("北京", "广州"), result.getCities());
    }

    @Test
    public void resolveDefaultAfterRemove_switchesToFirstCityWhenDefaultRemoved() {
        String defaultCity = CityListUtils.resolveDefaultAfterRemove(
                Arrays.asList("上海", "广州"),
                "北京",
                "北京"
        );

        assertEquals("上海", defaultCity);
    }
}
