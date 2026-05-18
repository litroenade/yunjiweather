package com.litroenade.yunjiweather.data.api;

import com.litroenade.yunjiweather.data.api.model.QWeatherAirQualityResponse;
import com.litroenade.yunjiweather.data.api.model.QWeatherDailyResponse;
import com.litroenade.yunjiweather.data.api.model.QWeatherCityLookupResponse;
import com.litroenade.yunjiweather.data.api.model.QWeatherHourlyResponse;
import com.litroenade.yunjiweather.data.api.model.QWeatherIndicesResponse;
import com.litroenade.yunjiweather.data.api.model.QWeatherNowResponse;
import com.litroenade.yunjiweather.data.api.model.QWeatherWarningResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WeatherApiService {

    @GET("geo/v2/city/lookup")
    Call<QWeatherCityLookupResponse> searchCity(
            @Query("location") String keyword,
            @Query("range") String range,
            @Query("number") int number,
            @Query("lang") String language
    );

    @GET("v7/weather/now")
    Call<QWeatherNowResponse> getNowWeather(
            @Query("location") String locationId,
            @Query("lang") String language,
            @Query("unit") String unit
    );

    @GET("v7/weather/3d")
    Call<QWeatherDailyResponse> getDailyWeather(
            @Query("location") String locationId,
            @Query("lang") String language,
            @Query("unit") String unit
    );

    @GET("v7/weather/24h")
    Call<QWeatherHourlyResponse> getHourlyWeather(
            @Query("location") String locationId,
            @Query("lang") String language,
            @Query("unit") String unit
    );

    @GET("airquality/v1/current/{latitude}/{longitude}")
    Call<QWeatherAirQualityResponse> getCurrentAirQuality(
            @Path("latitude") String latitude,
            @Path("longitude") String longitude,
            @Query("lang") String language
    );

    @GET("v7/indices/1d")
    Call<QWeatherIndicesResponse> getLifeIndices(
            @Query("location") String locationId,
            @Query("type") String type,
            @Query("lang") String language
    );

    @GET("v7/warning/now")
    Call<QWeatherWarningResponse> getWeatherWarning(
            @Query("location") String locationId,
            @Query("lang") String language
    );
}
