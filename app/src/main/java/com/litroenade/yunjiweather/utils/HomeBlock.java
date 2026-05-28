package com.litroenade.yunjiweather.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum HomeBlock {
    WEATHER_METRICS("weather_metrics", "今日实况", "体感、湿度、风力和能见度"),
    WIND_DETAIL("wind_detail", "风和风力", "风向、风速、气压等细节"),
    AIR_SUN("air_sun", "空气与日出", "空气质量概览和日出日落"),
    ADVICE("advice", "生活建议", "穿衣和出行建议"),
    WEATHER_INSIGHT("weather_insight", "今日资讯", "预警摘要、生活指数入口"),
    HOURLY_FORECAST("hourly_forecast", "逐小时", "横向小时预报"),
    DAILY_FORECAST("daily_forecast", "未来几天", "横向多日预报");

    private static final List<HomeBlock> DEFAULT_ORDER = Collections.unmodifiableList(Arrays.asList(values()));

    private final String key;
    private final String displayName;
    private final String shortDescription;

    HomeBlock(String key, String displayName, String shortDescription) {
        this.key = key;
        this.displayName = displayName;
        this.shortDescription = shortDescription;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public static List<HomeBlock> defaultOrder() {
        return DEFAULT_ORDER;
    }

    public static HomeBlock fromKey(String key) {
        for (HomeBlock block : values()) {
            if (block.key.equals(key)) {
                return block;
            }
        }
        return null;
    }
}
