package com.litroenade.yunjiweather.data.api.model;

import java.util.List;

public final class QWeatherDailyResponse {
    public String code;
    public String updateTime;
    public List<Daily> daily;

    public static final class Daily {
        public String fxDate;
        public String tempMax;
        public String tempMin;
        public String sunrise;
        public String sunset;
        public String iconDay;
        public String textDay;
        public String windDirDay;
        public String windScaleDay;
        public String humidity;
        public String pressure;
        public String vis;
        public String uvIndex;
    }
}
