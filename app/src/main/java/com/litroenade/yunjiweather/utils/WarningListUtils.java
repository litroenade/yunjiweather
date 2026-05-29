package com.litroenade.yunjiweather.utils;

import com.litroenade.yunjiweather.data.entity.WarningEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class WarningListUtils {

    private WarningListUtils() {
    }

    public static List<WarningEntity> markRead(List<WarningEntity> warnings, String warningId) {
        Objects.requireNonNull(warnings, "warnings");
        String targetWarningId = requireText(warningId, "warningId");
        List<WarningEntity> result = new ArrayList<>();
        for (WarningEntity warning : warnings) {
            WarningEntity copiedWarning = copyWarning(Objects.requireNonNull(warning, "warning"));
            if (targetWarningId.equals(copiedWarning.warningId)) {
                copiedWarning.isRead = true;
            }
            result.add(copiedWarning);
        }
        return result;
    }

    public static String createLocalCacheText(String cityName, List<WarningEntity> cachedWarnings) {
        String targetCityName = requireText(cityName, "cityName");
        Objects.requireNonNull(cachedWarnings, "cachedWarnings");
        if (cachedWarnings.isEmpty()) {
            return targetCityName + " 暂无本地预警缓存。";
        }
        return "已显示 " + targetCityName + " " + cachedWarnings.size() + " 条本地预警缓存。";
    }

    private static WarningEntity copyWarning(WarningEntity warning) {
        WarningEntity copiedWarning = new WarningEntity(
                warning.warningId,
                warning.locationId,
                warning.title,
                warning.type,
                warning.level,
                warning.content,
                warning.publishTime,
                warning.isRead,
                warning.isNotified
        );
        copiedWarning.id = warning.id;
        return copiedWarning;
    }

    private static String requireText(String value, String fieldName) {
        String text = Objects.requireNonNull(value, fieldName);
        if (text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }
        return text;
    }
}
