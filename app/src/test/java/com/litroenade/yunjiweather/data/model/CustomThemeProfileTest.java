package com.litroenade.yunjiweather.data.model;

import com.litroenade.yunjiweather.utils.HomeBlock;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class CustomThemeProfileTest {

    @Test
    public void resolver_prefersWeatherNightAndTimeSpecificGifOverFallback() {
        CustomThemeAsset fallback = new CustomThemeAsset(
                "fallback",
                "file:///themes/default.jpg",
                CustomThemeAsset.MEDIA_IMAGE,
                CustomThemeCropAnchor.CENTER,
                "默认图"
        );
        CustomThemeAsset rainDay = new CustomThemeAsset(
                "rain-day",
                "file:///themes/rain-day.jpg",
                CustomThemeAsset.MEDIA_IMAGE,
                CustomThemeCropAnchor.TOP,
                "雨天"
        );
        CustomThemeAsset rainNight = new CustomThemeAsset(
                "rain-night",
                "file:///themes/rain-night.gif",
                CustomThemeAsset.MEDIA_GIF,
                CustomThemeCropAnchor.BOTTOM,
                "雨夜动图"
        );
        CustomThemeProfile profile = CustomThemeProfile.create(
                Arrays.asList(fallback, rainDay, rainNight),
                Arrays.asList(
                        CustomThemeRule.fallback("fallback"),
                        new CustomThemeRule("rain-day", CustomThemeWeatherKey.RAIN, CustomThemeRule.LIGHT_DAY, -1, -1, 20),
                        new CustomThemeRule("rain-night", CustomThemeWeatherKey.RAIN, CustomThemeRule.LIGHT_NIGHT, 19 * 60, 23 * 60, 80)
                ),
                Arrays.asList(HomeBlock.WEATHER_METRICS.getKey(), HomeBlock.HOURLY_FORECAST.getKey()),
                Collections.singleton(HomeBlock.DAILY_FORECAST.getKey())
        );

        CustomThemeAsset resolved = CustomThemeResolver.resolve(
                profile,
                CustomThemeWeatherKey.RAIN,
                true,
                21 * 60
        );

        assertEquals("rain-night", resolved.getId());
        assertEquals(CustomThemeAsset.MEDIA_GIF, resolved.getMediaType());
        assertEquals(CustomThemeCropAnchor.BOTTOM, resolved.getCropAnchor());
    }

    @Test
    public void resolverFallsBackWhenCombinationDoesNotMatch() {
        CustomThemeAsset fallback = new CustomThemeAsset(
                "fallback",
                "file:///themes/default.jpg",
                CustomThemeAsset.MEDIA_IMAGE,
                CustomThemeCropAnchor.CENTER,
                "默认图"
        );
        CustomThemeAsset snowNight = new CustomThemeAsset(
                "snow-night",
                "file:///themes/snow-night.gif",
                CustomThemeAsset.MEDIA_GIF,
                CustomThemeCropAnchor.CENTER,
                "雪夜"
        );
        CustomThemeProfile profile = CustomThemeProfile.create(
                Arrays.asList(fallback, snowNight),
                Arrays.asList(
                        CustomThemeRule.fallback("fallback"),
                        new CustomThemeRule("snow-night", CustomThemeWeatherKey.SNOW, CustomThemeRule.LIGHT_NIGHT, 22 * 60, 6 * 60, 70)
                ),
                Collections.emptyList(),
                Collections.emptySet()
        );

        assertEquals("snow-night", CustomThemeResolver.resolve(profile, CustomThemeWeatherKey.SNOW, true, 23 * 60).getId());
        assertEquals("snow-night", CustomThemeResolver.resolve(profile, CustomThemeWeatherKey.SNOW, true, 5 * 60).getId());
        assertEquals("fallback", CustomThemeResolver.resolve(profile, CustomThemeWeatherKey.SNOW, false, 12 * 60).getId());
        assertEquals("fallback", CustomThemeResolver.resolve(profile, CustomThemeWeatherKey.CLOUDY, true, 23 * 60).getId());
    }

    @Test
    public void codecRoundTripsAssetsRulesAndModuleLayout() {
        CustomThemeProfile profile = CustomThemeProfile.create(
                Arrays.asList(
                        new CustomThemeAsset("fallback", "file:///a.jpg", CustomThemeAsset.MEDIA_IMAGE, CustomThemeCropAnchor.TOP, "默认|图"),
                        new CustomThemeAsset("rain-night", "file:///b.gif", CustomThemeAsset.MEDIA_GIF, CustomThemeCropAnchor.BOTTOM, "雨夜")
                ),
                Arrays.asList(
                        CustomThemeRule.fallback("fallback"),
                        new CustomThemeRule("rain-night", CustomThemeWeatherKey.RAIN, CustomThemeRule.LIGHT_NIGHT, 18 * 60, 23 * 60, 90)
                ),
                Arrays.asList(HomeBlock.WEATHER_METRICS.getKey(), HomeBlock.DAILY_FORECAST.getKey(), HomeBlock.HOURLY_FORECAST.getKey()),
                Collections.singleton(HomeBlock.HOURLY_FORECAST.getKey())
        );

        String encoded = CustomThemeProfileCodec.encode(profile);
        CustomThemeProfile decoded = CustomThemeProfileCodec.decode(encoded);

        assertEquals(2, decoded.getAssets().size());
        assertEquals("默认|图", decoded.getAssets().get(0).getLabel());
        assertEquals(CustomThemeAsset.MEDIA_GIF, decoded.getAssets().get(1).getMediaType());
        assertEquals(2, decoded.getRules().size());
        assertEquals(18 * 60, decoded.getRules().get(1).getStartMinute());
        assertEquals(Arrays.asList(HomeBlock.WEATHER_METRICS.getKey(), HomeBlock.DAILY_FORECAST.getKey(), HomeBlock.HOURLY_FORECAST.getKey()),
                decoded.getHomeModuleOrder());
        assertEquals(true, decoded.getDisabledHomeModules().contains(HomeBlock.HOURLY_FORECAST.getKey()));
    }
}
