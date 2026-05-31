package com.litroenade.yunjiweather.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.litroenade.yunjiweather.data.model.CustomThemeAsset;
import com.litroenade.yunjiweather.data.model.CustomThemeCropAnchor;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;

import org.junit.Test;

public class WeatherWidgetSnapshotFactoryTest {

    @Test
    public void fromHomeWeatherUsesRealHomeWeatherData() {
        WeatherWidgetSnapshot snapshot = WeatherWidgetSnapshotFactory.fromHomeWeather(
                homeWeather(),
                "05-31 13:25",
                new CustomThemeAsset(
                        "cloudy",
                        "file:///cloudy.gif",
                        CustomThemeAsset.MEDIA_GIF,
                        CustomThemeCropAnchor.TOP,
                        "Cloudy"
                )
        );

        assertTrue(snapshot.isAvailable());
        assertEquals("Beijing", snapshot.getCityName());
        assertEquals("31°", snapshot.getTemperatureText());
        assertEquals("Cloudy", snapshot.getConditionText());
        assertEquals("35° / 25°", snapshot.getRangeText());
        assertEquals("05-31 13:25", snapshot.getUpdateText());
        assertEquals("file:///cloudy.gif", snapshot.getCustomBackgroundUri());
        assertEquals(CustomThemeCropAnchor.TOP, snapshot.getCustomBackgroundCropAnchor());
        assertEquals(CustomThemeAsset.MEDIA_GIF, snapshot.getCustomBackgroundMediaType());
    }

    @Test
    public void unavailableSnapshotKeepsCityAndClearFallbackText() {
        WeatherWidgetSnapshot snapshot = WeatherWidgetSnapshotFactory.unavailable("Shanghai");

        assertEquals("Shanghai", snapshot.getCityName());
        assertEquals("\u6253\u5f00\u67e5\u770b\u5b9e\u65f6\u5929\u6c14", snapshot.getTemperatureText());
        assertEquals("\u6682\u65e0\u7f13\u5b58", snapshot.getConditionText());
    }

    private static HomeWeatherData homeWeather() {
        return new HomeWeatherData(
                "Beijing",
                "101010100",
                "31",
                "Cloudy",
                "32",
                "35",
                "25",
                "44",
                "NW",
                "2",
                "8.5",
                "1008",
                "12",
                "104",
                1716600000000L,
                "Short sleeve",
                "Limit outdoor activity",
                "153",
                "Unhealthy",
                "O3"
        );
    }
}
