package com.litroenade.yunjiweather.data.api.model;

import java.util.List;

public final class QWeatherCityLookupResponse {
    public String code;
    public List<Location> location;

    public static final class Location {
        public String name;
        public String id;
        public String lat;
        public String lon;
        public String adm1;
        public String country;
    }
}
