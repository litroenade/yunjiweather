package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.data.api.WeatherApiService;
import com.litroenade.yunjiweather.data.api.model.QWeatherAirQualityResponse;
import com.litroenade.yunjiweather.data.api.model.QWeatherCityLookupResponse;
import com.litroenade.yunjiweather.data.api.model.QWeatherDailyResponse;
import com.litroenade.yunjiweather.data.api.model.QWeatherHourlyResponse;
import com.litroenade.yunjiweather.data.api.model.QWeatherIndicesResponse;
import com.litroenade.yunjiweather.data.api.model.QWeatherNowResponse;
import com.litroenade.yunjiweather.data.api.model.QWeatherWarningResponse;
import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.data.local.WarningDao;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Request;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;

public class AlertRepositoryTest {

    @Test
    public void refreshWarnings_removesWarningsMissingFromLatestRemoteResponse() throws IOException {
        FakeWarningDao warningDao = new FakeWarningDao();
        warningDao.insertAll(Collections.singletonList(warning("old-warning")));
        FakeWeatherApiService apiService = new FakeWeatherApiService(warningResponse(remoteWarning("new-warning")));
        AlertRepository repository = new AlertRepository(7L, apiService, warningDao);

        List<WarningEntity> warnings = repository.refreshWarnings("101010100");

        assertEquals(1, warnings.size());
        assertEquals("new-warning", warnings.get(0).warningId);
        assertEquals(1, warningDao.findByLocationId(7L, "101010100").size());
        assertEquals("new-warning", warningDao.findByLocationId(7L, "101010100").get(0).warningId);
    }

    private static WarningEntity warning(String warningId) {
        return new WarningEntity(
                7L,
                warningId,
                "101010100",
                "暴雨蓝色预警",
                "暴雨",
                "蓝色",
                "未来两小时可能出现短时强降雨。",
                1_700_000_000_000L,
                false,
                false
        );
    }

    private static QWeatherWarningResponse warningResponse(QWeatherWarningResponse.Warning warning) {
        QWeatherWarningResponse response = new QWeatherWarningResponse();
        response.code = "200";
        response.warning = Collections.singletonList(warning);
        return response;
    }

    private static QWeatherWarningResponse.Warning remoteWarning(String warningId) {
        QWeatherWarningResponse.Warning warning = new QWeatherWarningResponse.Warning();
        warning.id = warningId;
        warning.pubTime = "2023-11-15T06:13+08:00";
        warning.title = "暴雨蓝色预警";
        warning.severity = "Blue";
        warning.severityColor = "蓝色";
        warning.type = "暴雨";
        warning.typeName = "暴雨";
        warning.text = "未来两小时可能出现短时强降雨。";
        return warning;
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

    private static final class FakeWeatherApiService implements WeatherApiService {
        private final QWeatherWarningResponse response;

        private FakeWeatherApiService(QWeatherWarningResponse response) {
            this.response = response;
        }

        @Override
        public Call<QWeatherCityLookupResponse> searchCity(String keyword, String range, int number, String language) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Call<QWeatherNowResponse> getNowWeather(String locationId, String language, String unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Call<QWeatherDailyResponse> getDailyWeather(String locationId, String language, String unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Call<QWeatherHourlyResponse> getHourlyWeather(String locationId, String language, String unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Call<QWeatherAirQualityResponse> getCurrentAirQuality(String latitude, String longitude, String language) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Call<QWeatherIndicesResponse> getLifeIndices(String locationId, String type, String language) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Call<QWeatherWarningResponse> getWeatherWarning(String locationId, String language) {
            return new FakeCall<>(response);
        }
    }

    private static final class FakeCall<T> implements Call<T> {
        private final T body;

        private FakeCall(T body) {
            this.body = body;
        }

        @Override
        public Response<T> execute() {
            return Response.success(body);
        }

        @Override
        public void enqueue(Callback<T> callback) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isExecuted() {
            return false;
        }

        @Override
        public void cancel() {
        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public Call<T> clone() {
            return new FakeCall<>(body);
        }

        @Override
        public Request request() {
            return new Request.Builder().url("https://example.com/").build();
        }

        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }
    }
}
