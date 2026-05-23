package com.litroenade.yunjiweather.data.api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ApiConfigTest {

    @Test
    public void normalizeQWeatherBaseUrlAddsHttpsAndTrailingSlash() {
        assertEquals(
                "https://devapi.qweather.com/",
                ApiConfig.normalizeQWeatherBaseUrl("devapi.qweather.com")
        );
    }

    @Test
    public void normalizeQWeatherBaseUrlRejectsHttpUrl() {
        assertEquals("", ApiConfig.normalizeQWeatherBaseUrl("http://example.com/weather"));
    }

    @Test
    public void normalizeQWeatherBaseUrlRejectsInvalidHost() {
        assertEquals("", ApiConfig.normalizeQWeatherBaseUrl("https://bad host"));
    }

    @Test
    public void normalizeQWeatherBaseUrlRejectsUnsupportedScheme() {
        assertEquals("", ApiConfig.normalizeQWeatherBaseUrl("file://devapi.qweather.com"));
    }
}
