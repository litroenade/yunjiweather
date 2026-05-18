package com.litroenade.yunjiweather.data.api.model;

public final class QWeatherNowResponse {
    public String code;
    public String updateTime;
    public Now now;

    public static final class Now {
        public String obsTime;
        public String temp;
        public String feelsLike;
        public String icon;
        public String text;
        public String windDir;
        public String windScale;
        public String windSpeed;
        public String humidity;
        public String pressure;
        public String vis;
    }
}
