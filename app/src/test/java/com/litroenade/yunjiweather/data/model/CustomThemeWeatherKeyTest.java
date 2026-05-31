package com.litroenade.yunjiweather.data.model;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class CustomThemeWeatherKeyTest {

    @Test
    public void displayName_returnsReadableChineseLabelsForAllSlots() {
        assertEquals("默认图", CustomThemeWeatherKey.displayName(CustomThemeWeatherKey.FALLBACK));
        assertEquals("晴天底图", CustomThemeWeatherKey.displayName(CustomThemeWeatherKey.SUNNY));
        assertEquals("多云/阴天底图", CustomThemeWeatherKey.displayName(CustomThemeWeatherKey.CLOUDY));
        assertEquals("雨天底图", CustomThemeWeatherKey.displayName(CustomThemeWeatherKey.RAIN));
        assertEquals("雨夜底图", CustomThemeWeatherKey.displayName(CustomThemeWeatherKey.RAIN_NIGHT));
        assertEquals("雪天底图", CustomThemeWeatherKey.displayName(CustomThemeWeatherKey.SNOW));
        assertEquals("雪夜底图", CustomThemeWeatherKey.displayName(CustomThemeWeatherKey.SNOW_NIGHT));
        assertEquals("清晨底图", CustomThemeWeatherKey.displayName(CustomThemeWeatherKey.DAWN));
        assertEquals("黄昏底图", CustomThemeWeatherKey.displayName(CustomThemeWeatherKey.DUSK));
        assertEquals("夜间底图", CustomThemeWeatherKey.displayName(CustomThemeWeatherKey.NIGHT));
    }

    @Test
    public void orderedKeys_keepsFallbackThenWeatherAndTimeSlots() {
        assertEquals(
                Arrays.asList(
                        CustomThemeWeatherKey.FALLBACK,
                        CustomThemeWeatherKey.SUNNY,
                        CustomThemeWeatherKey.CLOUDY,
                        CustomThemeWeatherKey.RAIN,
                        CustomThemeWeatherKey.RAIN_NIGHT,
                        CustomThemeWeatherKey.SNOW,
                        CustomThemeWeatherKey.SNOW_NIGHT,
                        CustomThemeWeatherKey.DAWN,
                        CustomThemeWeatherKey.DUSK,
                        CustomThemeWeatherKey.NIGHT
                ),
                CustomThemeWeatherKey.orderedKeys()
        );
    }
}
