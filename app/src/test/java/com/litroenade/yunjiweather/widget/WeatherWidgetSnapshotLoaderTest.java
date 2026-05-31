package com.litroenade.yunjiweather.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.entity.WeatherCacheEntity;
import com.litroenade.yunjiweather.data.local.WeatherCacheTypes;
import com.litroenade.yunjiweather.data.model.CustomThemeAsset;
import com.litroenade.yunjiweather.data.model.CustomThemeCropAnchor;
import com.litroenade.yunjiweather.data.model.CustomThemeProfile;
import com.litroenade.yunjiweather.data.model.CustomThemeRule;
import com.litroenade.yunjiweather.data.model.CustomThemeWeatherKey;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.utils.VisualThemeUtils;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class WeatherWidgetSnapshotLoaderTest {

    @Test
    public void loadReturnsCachedHomeWeatherSnapshot() {
        CityEntity city = city("101010100", "北京");
        WeatherCacheEntity cache = new WeatherCacheEntity(
                city.locationId,
                city.cityName,
                WeatherCacheTypes.HOME,
                new Gson().toJson(homeWeather(city.cityName, city.locationId)),
                1716600000000L,
                1716601800000L
        );
        WeatherWidgetSnapshotLoader loader = new WeatherWidgetSnapshotLoader(
                () -> city,
                locationId -> cache,
                new Gson()
        );

        WeatherWidgetSnapshot snapshot = loader.load();

        assertTrue(snapshot.isAvailable());
        assertEquals("北京", snapshot.getCityName());
        assertEquals("19°", snapshot.getTemperatureText());
        assertEquals("多云", snapshot.getConditionText());
        assertEquals("24° / 17°", snapshot.getRangeText());
        assertEquals("外套", snapshot.getClothingValue());
        assertEquals("适宜", snapshot.getFishingValue());
        assertEquals("一般", snapshot.getSunsetValue());
        assertEquals("不易", snapshot.getColdValue());
    }

    @Test
    public void loadFallsBackToDefaultCityWhenCacheMissing() {
        WeatherWidgetSnapshotLoader loader = new WeatherWidgetSnapshotLoader(
                () -> null,
                locationId -> null,
                new Gson()
        );

        WeatherWidgetSnapshot snapshot = loader.load();

        assertFalse(snapshot.isAvailable());
        assertEquals("北京", snapshot.getCityName());
        assertEquals("打开查看实时天气", snapshot.getTemperatureText());
    }

    @Test
    public void loadFallsBackWhenOldCacheMissesRequiredFields() {
        CityEntity city = city("101010100", "北京");
        WeatherCacheEntity cache = new WeatherCacheEntity(
                city.locationId,
                city.cityName,
                WeatherCacheTypes.HOME,
                "{\"cityName\":\"北京\",\"locationId\":\"101010100\",\"temperature\":\"19\"}",
                1716600000000L,
                1716601800000L
        );
        WeatherWidgetSnapshotLoader loader = new WeatherWidgetSnapshotLoader(
                () -> city,
                locationId -> cache,
                new Gson()
        );

        WeatherWidgetSnapshot snapshot = loader.load();

        assertFalse(snapshot.isAvailable());
        assertEquals("北京", snapshot.getCityName());
        assertEquals("暂无缓存", snapshot.getConditionText());
    }

    @Test
    public void loadResolvesCustomThemeBackgroundForWidgetSnapshot() {
        CityEntity city = city("101010100", "北京");
        WeatherCacheEntity cache = new WeatherCacheEntity(
                city.locationId,
                city.cityName,
                WeatherCacheTypes.HOME,
                new Gson().toJson(homeWeather(city.cityName, city.locationId)),
                1716600000000L,
                1716601800000L
        );
        CustomThemeProfile profile = CustomThemeProfile.create(
                Arrays.asList(
                        new CustomThemeAsset("fallback", "file:///fallback.jpg", CustomThemeAsset.MEDIA_IMAGE, CustomThemeCropAnchor.CENTER, "默认图"),
                        new CustomThemeAsset("cloudy", "file:///cloudy.gif", CustomThemeAsset.MEDIA_GIF, CustomThemeCropAnchor.TOP, "多云")
                ),
                Arrays.asList(
                        CustomThemeRule.fallback("fallback"),
                        new CustomThemeRule("cloudy", CustomThemeWeatherKey.CLOUDY, CustomThemeRule.LIGHT_ANY, -1, -1, 20)
                ),
                Collections.emptyList(),
                Collections.emptySet()
        );
        WeatherWidgetSnapshotLoader loader = new WeatherWidgetSnapshotLoader(
                () -> city,
                locationId -> cache,
                new Gson(),
                () -> new WidgetThemeSettings(VisualThemeUtils.THEME_CUSTOM_1, profile)
        );

        WeatherWidgetSnapshot snapshot = loader.load();

        assertEquals("file:///cloudy.gif", snapshot.getCustomBackgroundUri());
        assertEquals(CustomThemeCropAnchor.TOP, snapshot.getCustomBackgroundCropAnchor());
        assertEquals(CustomThemeAsset.MEDIA_GIF, snapshot.getCustomBackgroundMediaType());
    }

    private static CityEntity city(String locationId, String cityName) {
        return new CityEntity(cityName, locationId, "北京", "中国", 39.9042, 116.4074, true, 0, 1L, 1L);
    }

    private static HomeWeatherData homeWeather(String cityName, String locationId) {
        return new HomeWeatherData(
                cityName,
                locationId,
                "19",
                "多云",
                "18",
                "24",
                "17",
                "70",
                "东北风",
                "2",
                "8",
                "1008",
                "6.0",
                "101",
                1716600000000L,
                "适合穿薄外套。",
                "适合出行。",
                "42",
                "优",
                "无"
        );
    }
}
