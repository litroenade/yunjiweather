package com.litroenade.yunjiweather.data.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private static final String OPEN_METEO_FORECAST_BASE_URL = "https://api.open-meteo.com/";
    private static final String OPEN_METEO_AIR_QUALITY_BASE_URL = "https://air-quality-api.open-meteo.com/";
    private static final String OPEN_METEO_GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/";

    private ApiClient() {
    }

    public static OpenMeteoApiService createOpenMeteoForecastService() {
        return createOpenMeteoService(OPEN_METEO_FORECAST_BASE_URL);
    }

    public static OpenMeteoApiService createOpenMeteoAirQualityService() {
        return createOpenMeteoService(OPEN_METEO_AIR_QUALITY_BASE_URL);
    }

    public static OpenMeteoApiService createOpenMeteoGeocodingService() {
        return createOpenMeteoService(OPEN_METEO_GEOCODING_BASE_URL);
    }

    private static OpenMeteoApiService createOpenMeteoService(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenMeteoApiService.class);
    }
}
