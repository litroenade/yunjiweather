package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.data.api.WeatherApiService;
import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.data.local.WarningDao;

import java.io.IOException;
import java.util.List;

public final class AlertRepository {

    private final AlertRemoteGateway remoteGateway;
    private final WarningStore warningStore;

    public AlertRepository(WeatherApiService apiService, WarningDao warningDao) {
        this.warningStore = new WarningStore(warningDao);
        this.remoteGateway = apiService == null
                ? null
                : new QWeatherAlertRemoteGateway(apiService, warningStore, new QWeatherWarningMapper());
    }

    AlertRepository(AlertRemoteGateway remoteGateway, WarningStore warningStore) {
        this.remoteGateway = remoteGateway;
        this.warningStore = warningStore;
    }

    public WarningRefreshResult refreshWarnings(String locationId) throws IOException {
        if (remoteGateway == null) {
            return new WarningRefreshResult(
                    locationId,
                    warningStore.findByLocationId(locationId),
                    WarningSource.CACHE_NO_API
            );
        }
        return remoteGateway.refresh(locationId);
    }

    public List<WarningEntity> findUnnotifiedWarnings() {
        return warningStore.findUnnotifiedWarnings();
    }

    public List<WarningEntity> findByLocationId(String locationId) {
        return warningStore.findByLocationId(locationId);
    }

    public void markRead(String locationId, String warningId) {
        warningStore.markRead(locationId, warningId);
    }

    public void markNotified(String locationId, String warningId) {
        warningStore.markNotified(locationId, warningId);
    }
}
