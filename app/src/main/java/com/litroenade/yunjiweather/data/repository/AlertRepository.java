package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.data.api.ApiConfig;
import com.litroenade.yunjiweather.data.api.WeatherApiService;
import com.litroenade.yunjiweather.data.api.model.QWeatherWarningResponse;
import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.data.local.WarningDao;
import com.litroenade.yunjiweather.utils.DateTimeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public final class AlertRepository {

    private static final String SUCCESS_CODE = "200";

    private final long ownerUserId;
    private final WeatherApiService apiService;
    private final WarningDao warningDao;

    public AlertRepository(long ownerUserId, WeatherApiService apiService, WarningDao warningDao) {
        this.ownerUserId = ownerUserId;
        this.apiService = apiService;
        this.warningDao = warningDao;
    }

    public List<WarningEntity> refreshWarnings(String locationId) throws IOException {
        if (!ApiConfig.isConfigured()) {
            return warningDao.findByLocationId(ownerUserId, locationId);
        }
        Response<QWeatherWarningResponse> response = apiService.getWeatherWarning(locationId, "zh").execute();
        QWeatherWarningResponse body = response.body();
        if (!response.isSuccessful() || body == null || !SUCCESS_CODE.equals(body.code)) {
            throw new IOException("天气预警接口请求失败");
        }
        List<WarningEntity> warnings = mapWarnings(locationId, body.warning);
        if (!warnings.isEmpty()) {
            warningDao.insertAll(warnings);
        }
        return warningDao.findByLocationId(ownerUserId, locationId);
    }

    public List<WarningEntity> findUnnotifiedWarnings() {
        return warningDao.findUnnotifiedWarnings(ownerUserId);
    }

    public List<WarningEntity> findByLocationId(String locationId) {
        return warningDao.findByLocationId(ownerUserId, locationId);
    }

    public void markRead(String locationId, String warningId) {
        warningDao.markRead(ownerUserId, locationId, warningId);
    }

    public void markNotified(String locationId, String warningId) {
        warningDao.markNotified(ownerUserId, locationId, warningId);
    }

    private List<WarningEntity> mapWarnings(String locationId, List<QWeatherWarningResponse.Warning> warnings) throws IOException {
        List<WarningEntity> result = new ArrayList<>();
        if (warnings == null) {
            return result;
        }
        for (QWeatherWarningResponse.Warning warning : warnings) {
            if (warning == null || warning.id == null || warning.id.trim().isEmpty()) {
                continue;
            }
            WarningEntity oldWarning = warningDao.findByWarningId(ownerUserId, locationId, warning.id);
            String typeText = warning.typeName == null || warning.typeName.trim().isEmpty()
                    ? requireText(warning.type, "warning.type")
                    : warning.typeName;
            String severityText = warning.severityColor == null || warning.severityColor.trim().isEmpty()
                    ? requireText(warning.severity, "warning.severity")
                    : warning.severityColor;
            result.add(new WarningEntity(
                    ownerUserId,
                    warning.id,
                    locationId,
                    requireText(warning.title, "warning.title"),
                    typeText,
                    severityText,
                    requireText(warning.text, "warning.text"),
                    parsePublishTime(warning.pubTime),
                    oldWarning != null && oldWarning.isRead,
                    oldWarning != null && oldWarning.isNotified
            ));
        }
        return result;
    }

    private long parsePublishTime(String value) throws IOException {
        return DateTimeUtils.parseQWeatherTime(requireText(value, "warning.pubTime"));
    }

    private String requireText(String value, String fieldName) throws IOException {
        if (value == null || value.trim().isEmpty()) {
            throw new IOException("天气预警接口缺少字段：" + fieldName);
        }
        return value;
    }
}
