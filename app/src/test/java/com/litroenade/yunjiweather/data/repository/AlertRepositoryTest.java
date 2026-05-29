package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.data.local.WarningDao;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AlertRepositoryTest {

    @Test
    public void refreshWarnings_returnsLocalCacheWithoutRemoteProvider() {
        FakeWarningDao warningDao = new FakeWarningDao();
        warningDao.insertAll(Collections.singletonList(warning("cached-warning")));
        AlertRepository repository = new AlertRepository(warningDao);

        WarningRefreshResult result = repository.refreshWarnings("openmeteo:1816670");

        assertEquals(WarningSource.LOCAL_CACHE, result.getSource());
        assertEquals(1, result.getWarnings().size());
        assertEquals("cached-warning", result.getWarnings().get(0).warningId);
    }

    @Test
    public void refreshWarnings_returnsEmptyLocalCacheWhenNoWarningsExist() {
        AlertRepository repository = new AlertRepository(new FakeWarningDao());

        WarningRefreshResult result = repository.refreshWarnings("openmeteo:1816670");

        assertEquals(WarningSource.LOCAL_CACHE, result.getSource());
        assertEquals(0, result.getWarnings().size());
    }

    private static WarningEntity warning(String warningId) {
        return new WarningEntity(
                warningId,
                "openmeteo:1816670",
                "暴雨蓝色预警",
                "暴雨",
                "蓝色",
                "未来两小时可能出现短时强降雨。",
                1_700_000_000_000L,
                false,
                false
        );
    }

    private static final class FakeWarningDao implements WarningDao {
        private final List<WarningEntity> warnings = new ArrayList<>();

        @Override
        public void insertAll(List<WarningEntity> warningList) {
            for (WarningEntity warning : warningList) {
                WarningEntity oldWarning = findByWarningId(warning.locationId, warning.warningId);
                if (oldWarning != null) {
                    warnings.remove(oldWarning);
                }
                warnings.add(warning);
            }
        }

        @Override
        public List<WarningEntity> findByLocationId(String locationId) {
            List<WarningEntity> result = new ArrayList<>();
            for (WarningEntity warning : warnings) {
                if (warning.locationId.equals(locationId)) {
                    result.add(warning);
                }
            }
            return result;
        }

        @Override
        public WarningEntity findByWarningId(String locationId, String warningId) {
            for (WarningEntity warning : warnings) {
                if (warning.locationId.equals(locationId)
                        && warning.warningId.equals(warningId)) {
                    return warning;
                }
            }
            return null;
        }

        @Override
        public List<WarningEntity> findUnnotifiedWarnings() {
            List<WarningEntity> result = new ArrayList<>();
            for (WarningEntity warning : warnings) {
                if (!warning.isNotified) {
                    result.add(warning);
                }
            }
            return result;
        }

        @Override
        public void markNotified(String locationId, String warningId) {
            WarningEntity warning = findByWarningId(locationId, warningId);
            if (warning != null) {
                warning.isNotified = true;
            }
        }

        @Override
        public void markRead(String locationId, String warningId) {
            WarningEntity warning = findByWarningId(locationId, warningId);
            if (warning != null) {
                warning.isRead = true;
            }
        }

        @Override
        public void deleteMissingByLocation(String locationId, List<String> activeWarningIds) {
            warnings.removeIf(warning -> warning.locationId.equals(locationId)
                    && !activeWarningIds.contains(warning.warningId));
        }

        @Override
        public void deleteByLocationId(String locationId) {
            warnings.removeIf(warning -> warning.locationId.equals(locationId));
        }

        @Override
        public int count() {
            return warnings.size();
        }
    }
}
