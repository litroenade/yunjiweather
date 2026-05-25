package com.litroenade.yunjiweather.data.api;

import com.litroenade.yunjiweather.data.repository.WeatherRepository;

public final class WeatherGatewayFactory {

    private static final String OPEN_METEO_LOCATION_PREFIX = "openmeteo:";

    private WeatherGatewayFactory() {
    }

    public static WeatherApiService createQWeatherServiceOrNull() {
        return ApiConfig.isConfigured() ? ApiClient.createWeatherApiService() : null;
    }

    public static OpenMeteoCitySearchGateway createOpenMeteoCitySearchGateway() {
        return new OpenMeteoCitySearchGateway(ApiClient.createOpenMeteoGeocodingService());
    }

    public static CityLookupGateway createCityLookupGateway(WeatherApiService qWeatherApiService) {
        return new CityLookupGateway(qWeatherApiService, createOpenMeteoCitySearchGateway());
    }

    public static WeatherRepository.RemoteGateway createHomeRemoteGateway(WeatherApiService qWeatherApiService) {
        WeatherRepository.RemoteGateway openMeteoGateway = createOpenMeteoGateway();
        if (qWeatherApiService == null) {
            return openMeteoGateway;
        }
        WeatherRepository.RemoteGateway qWeatherGateway = new QWeatherRemoteGateway(qWeatherApiService);
        return (locationId, cityName, latitude, longitude) -> {
            if (shouldUseQWeather(true, locationId)) {
                return qWeatherGateway.fetchHomeWeather(locationId, cityName, latitude, longitude);
            }
            return openMeteoGateway.fetchHomeWeather(locationId, cityName, latitude, longitude);
        };
    }

    public static boolean shouldUseQWeather(boolean qWeatherConfigured, String locationId) {
        if (locationId == null || locationId.trim().isEmpty()) {
            throw new IllegalArgumentException("locationId must not be empty");
        }
        return qWeatherConfigured && !locationId.startsWith(OPEN_METEO_LOCATION_PREFIX);
    }

    private static WeatherRepository.RemoteGateway createOpenMeteoGateway() {
        return new OpenMeteoRemoteGateway(
                ApiClient.createOpenMeteoForecastService(),
                ApiClient.createOpenMeteoAirQualityService()
        );
    }
}
