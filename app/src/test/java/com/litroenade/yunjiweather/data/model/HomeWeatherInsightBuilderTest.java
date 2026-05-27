package com.litroenade.yunjiweather.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.litroenade.yunjiweather.data.entity.WarningEntity;

import org.junit.Test;

import java.util.Collections;

public class HomeWeatherInsightBuilderTest {

    @Test
    public void buildPrioritizesCurrentCityWarningSummary() {
        HomeWeatherData weather = homeWeather();
        WarningEntity warning = new WarningEntity(
                "w-1",
                "101010100",
                "暴雨黄色预警",
                "暴雨",
                "黄色",
                "预计短时强降雨。",
                1716600000000L,
                false,
                false
        );

        HomeWeatherInsight insight = HomeWeatherInsightBuilder.build(
                weather,
                Collections.singletonList(warning)
        );

        assertTrue(insight.isPrimaryOpensAlerts());
        assertEquals("天气预警", insight.getPrimaryTitle());
        assertEquals("暴雨黄色预警 · 黄色", insight.getPrimarySubtitle());
        assertEquals("本地建议", insight.getSecondaryTitle());
    }

    @Test
    public void buildUsesLocalWeatherAdviceWhenNoWarningExists() {
        HomeWeatherInsight insight = HomeWeatherInsightBuilder.build(
                homeWeather(),
                Collections.emptyList()
        );

        assertFalse(insight.isPrimaryOpensAlerts());
        assertEquals("空气质量与出行", insight.getPrimaryTitle());
        assertEquals("AQI 42 优，适合出行。", insight.getPrimarySubtitle());
        assertEquals("穿衣建议", insight.getSecondaryTitle());
        assertEquals("适合穿薄外套。", insight.getSecondarySubtitle());
    }

    @Test
    public void buildHighlightsUnhealthyAirQualityBeforeGenericAdvice() {
        HomeWeatherInsight insight = HomeWeatherInsightBuilder.build(
                homeWeather("多云", "180", "不健康", "适合出行。"),
                Collections.emptyList()
        );

        assertFalse(insight.isPrimaryOpensAlerts());
        assertEquals("空气质量提醒", insight.getPrimaryTitle());
        assertEquals("AQI 180 不健康，建议减少长时间户外活动。", insight.getPrimarySubtitle());
        assertEquals("敏感人群提示", insight.getSecondaryTitle());
        assertEquals("老人、儿童和心肺敏感人群建议减少户外停留。", insight.getSecondarySubtitle());
    }

    @Test
    public void buildHighlightsRainAndSnowBeforeGenericAdvice() {
        HomeWeatherInsight insight = HomeWeatherInsightBuilder.build(
                homeWeather("小雨", "42", "优", "适合出行，建议关注实时天气变化。"),
                Collections.emptyList()
        );

        assertFalse(insight.isPrimaryOpensAlerts());
        assertEquals("天气变化提醒", insight.getPrimaryTitle());
        assertEquals("小雨，外出建议携带雨具并关注路面湿滑。", insight.getPrimarySubtitle());
        assertEquals("出行建议", insight.getSecondaryTitle());
    }

    @Test
    public void buildHandlesQWeatherWindScaleRangeWithoutCrashing() {
        HomeWeatherInsight insight = HomeWeatherInsightBuilder.build(
                homeWeather("多云", "42", "优", "适合出行。", "1-3"),
                Collections.emptyList()
        );

        assertFalse(insight.isPrimaryOpensAlerts());
        assertEquals("空气质量与出行", insight.getPrimaryTitle());
    }

    @Test
    public void buildHandlesDecimalAirQualityDisplayWithoutCrashing() {
        HomeWeatherInsight insight = HomeWeatherInsightBuilder.build(
                homeWeather("多云", "180.0", "不健康", "适合出行。"),
                Collections.emptyList()
        );

        assertFalse(insight.isPrimaryOpensAlerts());
        assertEquals("空气质量提醒", insight.getPrimaryTitle());
        assertEquals("AQI 180 不健康，建议减少长时间户外活动。", insight.getPrimarySubtitle());
    }

    private static HomeWeatherData homeWeather() {
        return homeWeather("多云", "42", "优", "适合出行。");
    }

    private static HomeWeatherData homeWeather(
            String condition,
            String airQualityIndex,
            String airQualityCategory,
            String travelAdvice
    ) {
        return homeWeather(condition, airQualityIndex, airQualityCategory, travelAdvice, "2");
    }

    private static HomeWeatherData homeWeather(
            String condition,
            String airQualityIndex,
            String airQualityCategory,
            String travelAdvice,
            String windScale
    ) {
        return new HomeWeatherData(
                "北京",
                "101010100",
                "19",
                condition,
                "18",
                "24",
                "17",
                "70",
                "东北风",
                windScale,
                "8",
                "1008",
                "6.0",
                "101",
                1716600000000L,
                "适合穿薄外套。",
                travelAdvice,
                airQualityIndex,
                airQualityCategory,
                "无"
        );
    }
}
