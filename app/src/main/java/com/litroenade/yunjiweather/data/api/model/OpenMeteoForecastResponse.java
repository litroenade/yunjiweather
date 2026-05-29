package com.litroenade.yunjiweather.data.api.model;

import java.util.List;

@SuppressWarnings("unused")
public final class OpenMeteoForecastResponse {
    public Double latitude;
    public Double longitude;
    public String timezone;
    public Current current;
    public Hourly hourly;
    public Daily daily;

    public static final class Current {
        public String time;
        public Double temperature_2m;
        public Double relative_humidity_2m;
        public Double apparent_temperature;
        public Integer is_day;
        public Integer weather_code;
        public Double pressure_msl;
        public Double wind_speed_10m;
        public Double wind_direction_10m;
        public Double visibility;
    }

    public static final class Hourly {
        public List<String> time;
        public List<Double> temperature_2m;
        public List<Integer> weather_code;
    }

    public static final class Daily {
        public List<String> time;
        public List<Integer> weather_code;
        public List<Double> temperature_2m_max;
        public List<Double> temperature_2m_min;
        public List<Double> uv_index_max;
        public List<String> sunrise;
        public List<String> sunset;
    }
}
