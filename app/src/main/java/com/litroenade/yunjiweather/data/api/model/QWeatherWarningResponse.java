package com.litroenade.yunjiweather.data.api.model;

import java.util.List;

public final class QWeatherWarningResponse {
    public String code;
    public String updateTime;
    public List<Warning> warning;

    public static final class Warning {
        public String id;
        public String pubTime;
        public String title;
        public String severity;
        public String severityColor;
        public String type;
        public String typeName;
        public String text;
    }
}
