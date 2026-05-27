package com.litroenade.yunjiweather.utils;

import static org.junit.Assert.assertEquals;

import com.litroenade.yunjiweather.data.model.HomeWeatherData;

import org.junit.Test;

public class WeatherShareUtilsTest {

    @Test
    public void buildShareTextIncludesCurrentWeatherAndUnits() {
        HomeWeatherData data = new HomeWeatherData(
                "上海",
                "101020100",
                "24",
                "雾",
                "25",
                "29",
                "24",
                "93",
                "东风",
                "2",
                "18",
                "1008",
                "5.6",
                "501",
                1716600000000L,
                "早晚偏凉，建议加外套。",
                "能见度偏低，出行注意安全。",
                "26",
                "优",
                "无"
        );

        assertEquals(
                "上海 24°，雾，29° / 24°，空气优，东风 2级。来自云迹天气。",
                WeatherShareUtils.buildShareText(
                        data,
                        WeatherDisplayUtils.TEMPERATURE_CELSIUS,
                        WeatherDisplayUtils.WIND_SCALE
                )
        );
    }
}
