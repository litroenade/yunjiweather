package com.litroenade.yunjiweather.data.api.model;

@SuppressWarnings("unused")
public final class OpenMeteoAirQualityResponse {
    public String timezone;
    public Current current;

    public static final class Current {
        public String time;
        public Double us_aqi;
        public Double us_aqi_pm2_5;
        public Double us_aqi_pm10;
        public Double us_aqi_nitrogen_dioxide;
        public Double us_aqi_ozone;
        public Double us_aqi_sulphur_dioxide;
        public Double us_aqi_carbon_monoxide;
    }
}
