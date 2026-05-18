package com.litroenade.yunjiweather.data.api;

import com.litroenade.yunjiweather.data.api.model.OpenMeteoAirQualityResponse;
import com.litroenade.yunjiweather.data.api.model.OpenMeteoForecastResponse;
import com.litroenade.yunjiweather.data.api.model.OpenMeteoGeocodingResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenMeteoApiService {

    @GET("v1/forecast")
    Call<OpenMeteoForecastResponse> getForecast(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("current") String current,
            @Query("hourly") String hourly,
            @Query("daily") String daily,
            @Query("timezone") String timezone,
            @Query("wind_speed_unit") String windSpeedUnit,
            @Query("forecast_days") int forecastDays,
            @Query("forecast_hours") int forecastHours
    );

    @GET("v1/air-quality")
    Call<OpenMeteoAirQualityResponse> getAirQuality(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("current") String current,
            @Query("timezone") String timezone,
            @Query("forecast_days") int forecastDays
    );

    @GET("v1/search")
    Call<OpenMeteoGeocodingResponse> searchCity(
            @Query("name") String keyword,
            @Query("count") int count,
            @Query("language") String language,
            @Query("format") String format,
            @Query("countryCode") String countryCode
    );
}
