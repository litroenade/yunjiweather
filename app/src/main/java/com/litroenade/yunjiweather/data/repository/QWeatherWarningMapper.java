package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.data.api.model.QWeatherWarningResponse;
import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.utils.DateTimeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class QWeatherWarningMapper {

    public List<WarningEntity> map(String locationId, List<QWeatherWarningResponse.Warning> warnings, WarningStore warningStore)
            throws IOException {
        if (warnings == null) {
            throw new IOException("QWeather warning list missing");
        }
        List<WarningEntity> result = new ArrayList<>();
        for (QWeatherWarningResponse.Warning warning : warnings) {
            if (warning == null) {
                throw new IOException("QWeather warning item missing");
            }
            String warningId = requireText(warning.id, "warning.id");
            WarningEntity oldWarning = warningStore.findByWarningId(locationId, warningId);
            String typeText = hasText(warning.typeName)
                    ? warning.typeName
                    : requireText(warning.type, "warning.type");
            String severityText = hasText(warning.severityColor)
                    ? warning.severityColor
                    : requireText(warning.severity, "warning.severity");
            result.add(new WarningEntity(
                    warningId,
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

    private static long parsePublishTime(String value) throws IOException {
        return DateTimeUtils.parseQWeatherTime(requireText(value, "warning.pubTime"));
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String requireText(String value, String fieldName) throws IOException {
        if (!hasText(value)) {
            throw new IOException("QWeather warning response missing field: " + fieldName);
        }
        return value;
    }
}
