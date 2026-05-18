package com.litroenade.yunjiweather.data.api.model;

import java.util.List;

public final class QWeatherHourlyResponse {
    public String code;
    public String updateTime;
    public List<Hourly> hourly;

    public static final class Hourly {
        public String fxTime;
        public String temp;
        public String icon;
        public String text;
        public String windDir;
        public String windScale;
        public String humidity;
    }
}
