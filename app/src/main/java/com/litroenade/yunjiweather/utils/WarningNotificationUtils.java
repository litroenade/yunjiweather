package com.litroenade.yunjiweather.utils;

import java.util.LinkedHashSet;
import java.util.Set;

public final class WarningNotificationUtils {

    private WarningNotificationUtils() {
    }

    public static boolean shouldNotify(Set<String> notifiedWarningIds, String warningId) {
        String normalizedWarningId = normalizeWarningId(warningId);
        if (notifiedWarningIds == null) {
            return true;
        }
        return !notifiedWarningIds.contains(normalizedWarningId);
    }

    public static Set<String> recordNotified(Set<String> notifiedWarningIds, String warningId) {
        String normalizedWarningId = normalizeWarningId(warningId);
        Set<String> result = new LinkedHashSet<>();
        if (notifiedWarningIds != null) {
            result.addAll(notifiedWarningIds);
        }
        result.add(normalizedWarningId);
        return result;
    }

    private static String normalizeWarningId(String warningId) {
        if (warningId == null || warningId.trim().isEmpty()) {
            throw new IllegalArgumentException("warningId must not be empty");
        }
        return warningId.trim();
    }
}
