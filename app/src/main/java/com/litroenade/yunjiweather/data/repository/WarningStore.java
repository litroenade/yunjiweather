package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.data.local.WarningDao;

import java.util.List;
import java.util.Objects;

public final class WarningStore {

    private final WarningDao warningDao;

    public WarningStore(WarningDao warningDao) {
        this.warningDao = Objects.requireNonNull(warningDao, "warningDao");
    }

    public void replaceByLocation(String locationId, List<WarningEntity> warnings) {
        warningDao.replaceByLocation(locationId, warnings);
    }

    public List<WarningEntity> findByLocationId(String locationId) {
        return warningDao.findByLocationId(locationId);
    }

    public WarningEntity findByWarningId(String locationId, String warningId) {
        return warningDao.findByWarningId(locationId, warningId);
    }

    public List<WarningEntity> findUnnotifiedWarnings() {
        return warningDao.findUnnotifiedWarnings();
    }

    public void markRead(String locationId, String warningId) {
        warningDao.markRead(locationId, warningId);
    }

    public void markNotified(String locationId, String warningId) {
        warningDao.markNotified(locationId, warningId);
    }
}
