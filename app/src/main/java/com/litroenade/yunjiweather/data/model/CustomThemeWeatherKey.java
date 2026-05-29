package com.litroenade.yunjiweather.data.model;

import com.litroenade.yunjiweather.utils.WeatherIconUtils;

import java.util.Arrays;
import java.util.List;

public final class CustomThemeWeatherKey {

    public static final String FALLBACK = "fallback";
    public static final String SUNNY = "sunny";
    public static final String CLOUDY = "cloudy";
    public static final String RAIN = "rain";
    public static final String SNOW = "snow";
    public static final String NIGHT = "night";

    private static final List<String> ORDERED_KEYS = Arrays.asList(
            FALLBACK,
            SUNNY,
            CLOUDY,
            RAIN,
            SNOW,
            NIGHT
    );

    private CustomThemeWeatherKey() {
    }

    public static List<String> orderedKeys() {
        return ORDERED_KEYS;
    }

    public static String normalize(String key) {
        if (key == null) {
            return FALLBACK;
        }
        String trimmedKey = key.trim();
        return ORDERED_KEYS.contains(trimmedKey) ? trimmedKey : FALLBACK;
    }

    public static String displayName(String key) {
        switch (normalize(key)) {
            case SUNNY:
                return "晴天底图";
            case CLOUDY:
                return "多云/阴天底图";
            case RAIN:
                return "雨天底图";
            case SNOW:
                return "雪天底图";
            case NIGHT:
                return "夜间底图";
            case FALLBACK:
            default:
                return "默认图";
        }
    }

    public static String fromWeatherCategory(WeatherIconUtils.WeatherCategory category) {
        if (category == null) {
            return FALLBACK;
        }
        switch (category) {
            case SUNNY:
                return SUNNY;
            case CLOUDY:
                return CLOUDY;
            case RAIN:
                return RAIN;
            case SNOW:
                return SNOW;
            case NIGHT:
                return NIGHT;
            default:
                return FALLBACK;
        }
    }
}
