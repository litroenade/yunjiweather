package com.litroenade.yunjiweather.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.data.local.WarningDao;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class WarningStoreTest {

    @Test
    public void replaceByLocationDelegatesToWarningDao() {
        FakeWarningDao warningDao = new FakeWarningDao();
        WarningStore store = new WarningStore(warningDao);
        List<WarningEntity> warnings = new ArrayList<>();
        warnings.add(warning("w-1", false, false));

        store.replaceByLocation("101010100", warnings);

        assertEquals("101010100", warningDao.replacedLocationId);
        assertSame(warnings, warningDao.replacedWarnings);
    }

    @Test
    public void markReadAndNotifiedDelegateByCompositeWarningKey() {
        FakeWarningDao warningDao = new FakeWarningDao();
        WarningStore store = new WarningStore(warningDao);

        store.markRead("101010100", "w-1");
        store.markNotified("101010100", "w-2");

        assertEquals("101010100:w-1", warningDao.readKey);
        assertEquals("101010100:w-2", warningDao.notifiedKey);
    }

    @Test
    public void findMethodsDelegateToWarningDao() {
        FakeWarningDao warningDao = new FakeWarningDao();
        WarningEntity warning = warning("w-1", false, false);
        warningDao.byLocation.add(warning);
        warningDao.byWarningId = warning;
        warningDao.unnotified.add(warning);
        WarningStore store = new WarningStore(warningDao);

        assertSame(warningDao.byLocation, store.findByLocationId("101010100"));
        assertSame(warning, store.findByWarningId("101010100", "w-1"));
        assertSame(warningDao.unnotified, store.findUnnotifiedWarnings());
        assertEquals("101010100", warningDao.findLocationId);
        assertEquals("101010100:w-1", warningDao.findWarningKey);
    }

    @Test
    public void constructorRejectsNullDaoWithStableParameterName() {
        try {
            new WarningStore(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException exception) {
            assertEquals("warningDao", exception.getMessage());
        }
    }

    private static WarningEntity warning(String warningId, boolean isRead, boolean isNotified) {
        return new WarningEntity(
                warningId,
                "101010100",
                "暴雨黄色预警",
                "暴雨",
                "黄色",
                "预计短时强降雨。",
                1716600000000L,
                isRead,
                isNotified
        );
    }

    private static final class FakeWarningDao implements WarningDao {
        private String replacedLocationId;
        private List<WarningEntity> replacedWarnings;
        private String readKey;
        private String notifiedKey;
        private final List<WarningEntity> byLocation = new ArrayList<>();
        private final List<WarningEntity> unnotified = new ArrayList<>();
        private WarningEntity byWarningId;
        private String findLocationId;
        private String findWarningKey;

        @Override
        public void insertAll(List<WarningEntity> warnings) {
        }

        @Override
        public void replaceByLocation(String locationId, List<WarningEntity> warnings) {
            replacedLocationId = locationId;
            replacedWarnings = warnings;
        }

        @Override
        public List<WarningEntity> findByLocationId(String locationId) {
            findLocationId = locationId;
            return byLocation;
        }

        @Override
        public WarningEntity findByWarningId(String locationId, String warningId) {
            findWarningKey = locationId + ":" + warningId;
            return byWarningId;
        }

        @Override
        public List<WarningEntity> findUnnotifiedWarnings() {
            return unnotified;
        }

        @Override
        public void markNotified(String locationId, String warningId) {
            notifiedKey = locationId + ":" + warningId;
        }

        @Override
        public void markRead(String locationId, String warningId) {
            readKey = locationId + ":" + warningId;
        }

        @Override
        public void deleteMissingByLocation(String locationId, List<String> activeWarningIds) {
        }

        @Override
        public void deleteByLocationId(String locationId) {
        }

        @Override
        public int count() {
            return 0;
        }
    }
}
