package com.litroenade.yunjiweather.data.api.model;

import java.util.List;

public final class QWeatherAirQualityResponse {
    public Metadata metadata;
    public List<AirIndex> indexes;

    public static final class Metadata {
        public String tag;
    }

    public static final class AirIndex {
        public String code;
        public String name;
        public String aqiDisplay;
        public String level;
        public String category;
        public PrimaryPollutant primaryPollutant;
        public Health health;
    }

    public static final class PrimaryPollutant {
        public String code;
        public String name;
        public String fullName;
    }

    public static final class Health {
        public String effect;
        public Advice advice;
    }

    public static final class Advice {
        public String generalPopulation;
        public String sensitivePopulation;
    }
}
