package com.litroenade.yunjiweather.data.api;

import com.litroenade.yunjiweather.data.entity.CityEntity;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CityLookupGatewayTest {

    @Test
    public void searchCity_returnsPresetCityBeforeRemoteLookup() throws IOException {
        CityLookupGateway gateway = new CityLookupGateway(null);

        CityEntity city = gateway.searchCity(" 北京 ", true, 9, 1234L);

        assertEquals("北京", city.cityName);
        assertEquals("openmeteo:1816670", city.locationId);
        assertTrue(city.isDefault);
        assertEquals(0, city.sortOrder);
        assertEquals(1234L, city.createTime);
    }

    @Test
    public void reverseLookup_returnsCoordinateCity() {
        CityLookupGateway gateway = new CityLookupGateway(null);

        CityEntity city = gateway.reverseLookup(39.9042, 116.4074, 5678L);

        assertEquals("当前位置", city.cityName);
        assertEquals("openmeteo:39.9042,116.4074", city.locationId);
        assertEquals("定位坐标", city.province);
        assertEquals("GPS", city.country);
        assertTrue(city.isDefault);
        assertEquals(5678L, city.updateTime);
    }
}
