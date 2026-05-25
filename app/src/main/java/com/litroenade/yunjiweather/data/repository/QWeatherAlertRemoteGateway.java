package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.data.api.WeatherApiService;
import com.litroenade.yunjiweather.data.api.model.QWeatherWarningResponse;
import com.litroenade.yunjiweather.data.entity.WarningEntity;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import retrofit2.Response;

public final class QWeatherAlertRemoteGateway implements AlertRemoteGateway {

    private static final String SUCCESS_CODE = "200";

    private final WeatherApiService apiService;
    private final WarningStore warningStore;
    private final QWeatherWarningMapper mapper;

    public QWeatherAlertRemoteGateway(
            WeatherApiService apiService,
            WarningStore warningStore,
            QWeatherWarningMapper mapper
    ) {
        this.apiService = Objects.requireNonNull(apiService, "apiService");
        this.warningStore = Objects.requireNonNull(warningStore, "warningStore");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public WarningRefreshResult refresh(String locationId) throws IOException {
        Response<QWeatherWarningResponse> response = apiService.getWeatherWarning(locationId, "zh").execute();
        QWeatherWarningResponse body = response.body();
        if (!response.isSuccessful() || body == null || !SUCCESS_CODE.equals(body.code)) {
            throw new IOException("QWeather warning request failed");
        }
        List<WarningEntity> warnings = mapper.map(locationId, body.warning, warningStore);
        warningStore.replaceByLocation(locationId, warnings);
        return new WarningRefreshResult(
                locationId,
                warningStore.findByLocationId(locationId),
                WarningSource.REMOTE
        );
    }
}
