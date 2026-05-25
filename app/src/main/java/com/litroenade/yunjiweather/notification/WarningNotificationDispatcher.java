package com.litroenade.yunjiweather.notification;

import com.litroenade.yunjiweather.data.entity.WarningEntity;

import java.util.List;
import java.util.Objects;

public final class WarningNotificationDispatcher {

    private final NotificationCandidateSelector candidateSelector;

    public WarningNotificationDispatcher(NotificationCandidateSelector candidateSelector) {
        this.candidateSelector = Objects.requireNonNull(candidateSelector, "candidateSelector");
    }

    public int dispatch(List<WarningEntity> warnings, WarningNotifier notifier, Marker marker) {
        Objects.requireNonNull(notifier, "notifier");
        Objects.requireNonNull(marker, "marker");
        int sentCount = 0;
        for (WarningEntity warning : candidateSelector.selectUnnotified(warnings)) {
            if (notifier.notify(warning)) {
                marker.markNotified(warning.locationId, warning.warningId);
                warning.isNotified = true;
                sentCount++;
            }
        }
        return sentCount;
    }

    public interface Marker {
        void markNotified(String locationId, String warningId);
    }
}
