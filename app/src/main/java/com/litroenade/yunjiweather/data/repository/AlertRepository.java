package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.data.local.WarningDao;

import java.util.List;

/**
 * 预警只维护本地可信状态。
 * 移除远端官方预警源后，刷新不会伪造安全信息，只返回设备已有缓存。
 */
public final class AlertRepository {

    private final WarningStore warningStore;

    public AlertRepository(WarningDao warningDao) {
        this.warningStore = new WarningStore(warningDao);
    }

    AlertRepository(WarningStore warningStore) {
        this.warningStore = warningStore;
    }

    public WarningRefreshResult refreshWarnings(String locationId) {
        return new WarningRefreshResult(
                locationId,
                warningStore.findByLocationId(locationId),
                WarningSource.LOCAL_CACHE
        );
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
