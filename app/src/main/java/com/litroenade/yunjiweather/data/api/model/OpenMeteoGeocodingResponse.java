package com.litroenade.yunjiweather.data.api.model;

import java.util.List;

@SuppressWarnings("unused")
public final class OpenMeteoGeocodingResponse {
    public List<Location> results;

    public static final class Location {
        public Integer id;
        public String name;
        public Double latitude;
        public Double longitude;
        public String country;
        public String admin1;
    }
}
