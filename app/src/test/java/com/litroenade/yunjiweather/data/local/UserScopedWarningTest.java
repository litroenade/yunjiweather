package com.litroenade.yunjiweather.data.local;

import com.litroenade.yunjiweather.data.entity.WarningEntity;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserScopedWarningTest {

    @Test
    public void sameWarningId_isolatedBetweenUsers() {
        FakeWarningDao warningDao = new FakeWarningDao();
        warningDao.insertAll(Collections.singletonList(warning(1L, "101010100", "w-1", false)));
        warningDao.insertAll(Collections.singletonList(warning(2L, "101010100", "w-1", false)));

        warningDao.markNotified(1L, "101010100", "w-1");

        assertTrue(warningDao.findByWarningId(1L, "101010100", "w-1").isNotified);
        assertFalse(warningDao.findByWarningId(2L, "101010100", "w-1").isNotified);
    }

    @Test
    public void sameWarningId_isolatedBetweenLocationsForSameUser() {
        FakeWarningDao warningDao = new FakeWarningDao();
        warningDao.insertAll(Collections.singletonList(warning(1L, "101010100", "w-1", false)));
        warningDao.insertAll(Collections.singletonList(warning(1L, "101020100", "w-1", false)));

        warningDao.markNotified(1L, "101010100", "w-1");

        assertTrue(warningDao.findByWarningId(1L, "101010100", "w-1").isNotified);
        assertFalse(warningDao.findByWarningId(1L, "101020100", "w-1").isNotified);
        assertFalse(warningDao.findByLocationId(1L, "101020100").isEmpty());
    }

    private static WarningEntity warning(long ownerUserId, String locationId, String warningId, boolean isNotified) {
        return new WarningEntity(
                ownerUserId,
                warningId,
                locationId,
                "暴雨蓝色预警",
                "暴雨",
                "蓝色",
                "未来两小时可能出现短时强降雨。",
                1_700_000_000_000L,
                false,
                isNotified
        );
    }

    private static final class FakeWarningDao implements WarningDao {
        private final List<WarningEntity> warnings = new ArrayList<>();

        @Override
        public void insertAll(List<WarningEntity> warningList) {
            for (WarningEntity warning : warningList) {
                WarningEntity oldWarning = findByWarningId(warning.ownerUserId, warning.locationId, warning.warningId);
                if (oldWarning != null) {
                    warnings.remove(oldWarning);
                }
                warnings.add(warning);
            }
        }

        @Override
        public List<WarningEntity> findByLocationId(long ownerUserId, String locationId) {
            List<WarningEntity> result = new ArrayList<>();
            for (WarningEntity warning : warnings) {
                if (warning.ownerUserId == ownerUserId && warning.locationId.equals(locationId)) {
                    result.add(warning);
                }
            }
            return result;
        }

        @Override
        public WarningEntity findByWarningId(long ownerUserId, String locationId, String warningId) {
            for (WarningEntity warning : warnings) {
                if (warning.ownerUserId == ownerUserId
                        && warning.locationId.equals(locationId)
                        && warning.warningId.equals(warningId)) {
                    return warning;
                }
            }
            return null;
        }

        @Override
        public List<WarningEntity> findUnnotifiedWarnings(long ownerUserId) {
            List<WarningEntity> result = new ArrayList<>();
            for (WarningEntity warning : warnings) {
                if (warning.ownerUserId == ownerUserId && !warning.isNotified) {
                    result.add(warning);
                }
            }
            return result;
        }

        @Override
        public void markNotified(long ownerUserId, String locationId, String warningId) {
            WarningEntity warning = findByWarningId(ownerUserId, locationId, warningId);
            if (warning != null) {
                warning.isNotified = true;
            }
        }

        @Override
        public void markRead(long ownerUserId, String locationId, String warningId) {
            WarningEntity warning = findByWarningId(ownerUserId, locationId, warningId);
            if (warning != null) {
                warning.isRead = true;
            }
        }

        @Override
        public void deleteMissingByLocation(long ownerUserId, String locationId, List<String> activeWarningIds) {
            warnings.removeIf(warning -> warning.ownerUserId == ownerUserId
                    && warning.locationId.equals(locationId)
                    && !activeWarningIds.contains(warning.warningId));
        }

        @Override
        public void deleteByLocationId(long ownerUserId, String locationId) {
            warnings.removeIf(warning -> warning.ownerUserId == ownerUserId && warning.locationId.equals(locationId));
        }

        @Override
        public int count(long ownerUserId) {
            int count = 0;
            for (WarningEntity warning : warnings) {
                if (warning.ownerUserId == ownerUserId) {
                    count++;
                }
            }
            return count;
        }
    }
}
