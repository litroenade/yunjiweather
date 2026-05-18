package com.litroenade.yunjiweather.utils;

import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WarningNotificationUtilsTest {

    @Test
    public void shouldNotify_returnsTrueForNewWarningId() {
        Set<String> notifiedIds = new LinkedHashSet<>();
        notifiedIds.add("rain-001");

        assertTrue(WarningNotificationUtils.shouldNotify(notifiedIds, "wind-002"));
    }

    @Test
    public void shouldNotify_returnsFalseForDuplicateWarningId() {
        Set<String> notifiedIds = new LinkedHashSet<>();
        notifiedIds.add("rain-001");

        assertFalse(WarningNotificationUtils.shouldNotify(notifiedIds, "rain-001"));
    }

    @Test
    public void recordNotified_addsWarningIdOnce() {
        Set<String> notifiedIds = new LinkedHashSet<>();
        notifiedIds.add("rain-001");

        Set<String> result = WarningNotificationUtils.recordNotified(notifiedIds, "rain-001");

        assertTrue(result.contains("rain-001"));
        assertFalse(WarningNotificationUtils.shouldNotify(result, "rain-001"));
    }
}
