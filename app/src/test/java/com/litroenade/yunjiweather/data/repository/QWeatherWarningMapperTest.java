package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.data.api.model.QWeatherWarningResponse;
import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.data.local.WarningDao;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QWeatherWarningMapperTest {

    @Test
    public void map_preservesReadAndNotifiedFlagsAndFallsBackToRawFields() throws IOException {
        FakeWarningDao warningDao = new FakeWarningDao();
        WarningEntity existing = warning("warning-1");
        existing.isRead = true;
        existing.isNotified = true;
        warningDao.insertAll(Collections.singletonList(existing));
        QWeatherWarningResponse.Warning remote = remoteWarning("warning-1");
        remote.typeName = "";
        remote.severityColor = "";

        List<WarningEntity> warnings = new QWeatherWarningMapper().map(
                "101010100",
                Collections.singletonList(remote),
                new WarningStore(warningDao)
        );

        assertEquals(1, warnings.size());
        assertEquals("rain", warnings.get(0).type);
        assertEquals("Blue", warnings.get(0).level);
        assertTrue(warnings.get(0).isRead);
        assertTrue(warnings.get(0).isNotified);
    }

    @Test(expected = IOException.class)
    public void map_nullWarningListThrows() throws IOException {
        new QWeatherWarningMapper().map("101010100", null, new WarningStore(new FakeWarningDao()));
    }

    @Test(expected = IOException.class)
    public void map_missingWarningIdThrows() throws IOException {
        QWeatherWarningResponse.Warning remote = remoteWarning("");
        new QWeatherWarningMapper().map(
                "101010100",
                Collections.singletonList(remote),
                new WarningStore(new FakeWarningDao())
        );
    }

    private static QWeatherWarningResponse.Warning remoteWarning(String warningId) {
        QWeatherWarningResponse.Warning warning = new QWeatherWarningResponse.Warning();
        warning.id = warningId;
        warning.pubTime = "2023-11-15T06:13+08:00";
        warning.title = "Storm warning";
        warning.severity = "Blue";
        warning.severityColor = "Blue";
        warning.type = "rain";
        warning.typeName = "Rain";
        warning.text = "Severe storm warning content";
        return warning;
    }

    private static WarningEntity warning(String warningId) {
        return new WarningEntity(
                warningId,
                "101010100",
                "Storm warning",
                "Storm",
                "Red",
                "Severe storm warning content",
                1_700_000_000_000L,
                false,
                false
        );
    }

    private static final class FakeWarningDao implements WarningDao {
        private final List<WarningEntity> warnings = new ArrayList<>();

        @Override
        public void insertAll(List<WarningEntity> warningList) {
            warnings.addAll(warningList);
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
            return Collections.emptyList();
        }

        @Override
        public void markNotified(String locationId, String warningId) {
        }

        @Override
        public void markRead(String locationId, String warningId) {
        }

        @Override
        public void deleteMissingByLocation(String locationId, List<String> activeWarningIds) {
        }

        @Override
        public void deleteByLocationId(String locationId) {
        }

        @Override
        public int count() {
            return warnings.size();
        }
    }
}
