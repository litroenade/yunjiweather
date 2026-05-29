package com.litroenade.yunjiweather.data.model;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class LifeIndexDefaultsTest {

    @Test
    public void createFallbackItems_returnsTenRequiredSuggestions() {
        List<LifeIndexItem> items = LifeIndexDefaults.createFallbackItems();

        assertEquals(10, items.size());
        assertEquals("穿衣", items.get(0).getName());
        assertEquals("出行", items.get(1).getName());
        assertEquals("运动", items.get(2).getName());
        assertEquals("洗车", items.get(3).getName());
        assertEquals("紫外线", items.get(4).getName());
        assertEquals("感冒", items.get(5).getName());
        assertEquals("空气", items.get(6).getName());
        assertEquals("舒适度", items.get(7).getName());
        assertEquals("晾晒", items.get(8).getName());
        assertEquals("旅游", items.get(9).getName());
    }

    @Test
    public void completeWithFallbacks_preservesRemoteItemAndAddsMissingSuggestions() {
        LifeIndexItem remoteClothing = new LifeIndexItem(
                "穿衣指数",
                "炎热",
                "建议穿短袖并及时补水。",
                "高温天气需要注意防晒。"
        );

        List<LifeIndexItem> items = LifeIndexDefaults.completeWithFallbacks(Collections.singletonList(remoteClothing));

        assertEquals(10, items.size());
        assertEquals("穿衣指数", items.get(0).getName());
        assertEquals("炎热", items.get(0).getLevel());
        assertEquals("建议穿短袖并及时补水。", items.get(0).getAdvice());
        assertEquals("出行", items.get(1).getName());
        assertEquals("旅游", items.get(9).getName());
    }

    @Test
    public void createWeatherDrivenItems_changesAdviceWithCurrentWeather() {
        HomeWeatherData coldRain = createWeather("4", "中雨", "5", "180", "1");
        HomeWeatherData hotSunny = createWeather("35", "晴", "2", "45", "9");

        List<LifeIndexItem> coldItems = LifeIndexDefaults.createWeatherDrivenItems(coldRain);
        List<LifeIndexItem> hotItems = LifeIndexDefaults.createWeatherDrivenItems(hotSunny);

        assertEquals(10, coldItems.size());
        assertEquals(10, hotItems.size());
        assertNotEquals(coldItems.get(0).getAdvice(), hotItems.get(0).getAdvice());
        assertNotEquals(coldItems.get(3).getLevel(), hotItems.get(3).getLevel());
        assertNotEquals(coldItems.get(4).getLevel(), hotItems.get(4).getLevel());
    }

    private HomeWeatherData createWeather(
            String temperature,
            String condition,
            String windScale,
            String airQualityIndex,
            String uvIndex
    ) {
        return new HomeWeatherData(
                "Beijing",
                "openmeteo:1816670",
                temperature,
                condition,
                temperature,
                "36",
                "3",
                "70",
                "东北风",
                windScale,
                "18",
                "1010",
                "8",
                "100",
                1000L,
                "cached clothing",
                "cached travel",
                airQualityIndex,
                "良",
                "PM2.5",
                uvIndex,
                "06:00",
                "18:00",
                Collections.emptyList(),
                Collections.emptyList()
        );
    }
}
