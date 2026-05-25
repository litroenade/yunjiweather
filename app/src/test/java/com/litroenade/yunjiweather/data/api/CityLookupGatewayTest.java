package com.litroenade.yunjiweather.data.api;

import com.litroenade.yunjiweather.data.api.model.QWeatherCityLookupResponse;
import com.litroenade.yunjiweather.data.entity.CityEntity;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CityLookupGatewayTest {

    @Test
    public void searchCity_returnsPresetCityBeforeRemoteLookup() throws IOException {
        CityLookupGateway gateway = new CityLookupGateway(null, null);

        CityEntity city = gateway.searchCity(" 北京 ", true, 9, 1234L);

        assertEquals("北京", city.cityName);
        assertEquals("101010100", city.locationId);
        assertTrue(city.isDefault);
        assertEquals(0, city.sortOrder);
        assertEquals(1234L, city.createTime);
    }

    @Test
    public void reverseLookup_returnsCoordinateCityWhenQWeatherUnavailable() throws IOException {
        CityLookupGateway gateway = new CityLookupGateway(null, null);

        CityEntity city = gateway.reverseLookup(39.9042, 116.4074, 5678L);

        assertEquals("当前位置", city.cityName);
        assertEquals("openmeteo:39.9042,116.4074", city.locationId);
        assertEquals("定位坐标", city.province);
        assertEquals("GPS", city.country);
        assertTrue(city.isDefault);
        assertEquals(5678L, city.updateTime);
    }

    @Test
    public void mapQWeatherLocation_requiresCoordinateFields() {
        QWeatherCityLookupResponse.Location location = new QWeatherCityLookupResponse.Location();
        location.name = "杭州";
        location.id = "101210101";
        location.adm1 = "浙江";
        location.country = "中国";
        location.lat = "30.2741";
        location.lon = "";

        try {
            CityLookupGateway.mapQWeatherLocation(location, true, 1, 100L, "城市搜索接口");
        } catch (IOException exception) {
            assertEquals("城市搜索接口缺少字段：location.lon", exception.getMessage());
            return;
        }
        throw new AssertionError("Expected IOException for missing longitude");
    }
}
