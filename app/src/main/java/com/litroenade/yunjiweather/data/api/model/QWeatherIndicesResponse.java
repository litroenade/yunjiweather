package com.litroenade.yunjiweather.data.api.model;

import java.util.List;

public final class QWeatherIndicesResponse {
    public String code;
    public String updateTime;
    public List<Daily> daily;

    public static final class Daily {
        public String date;
        public String type;
        public String name;
        public String level;
        public String category;
        public String text;
    }
}
