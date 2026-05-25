package com.litroenade.yunjiweather.notification;

import com.litroenade.yunjiweather.data.entity.WarningEntity;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WarningNotificationDispatcherTest {

    @Test
    public void dispatch_marksOnlySuccessfullySentWarnings() {
        WarningEntity sent = warning("sent-warning", false);
        WarningEntity rejected = warning("rejected-warning", false);
        RecordingMarker marker = new RecordingMarker();
        WarningNotificationDispatcher dispatcher = new WarningNotificationDispatcher(new NotificationCandidateSelector());

        int sentCount = dispatcher.dispatch(
                Arrays.asList(sent, rejected),
                warning -> warning.warningId.equals("sent-warning"),
                marker
        );

        assertEquals(1, sentCount);
        assertEquals(1, marker.markedCount);
        assertEquals("sent-warning", marker.lastWarningId);
        assertTrue(sent.isNotified);
        assertEquals(false, rejected.isNotified);
    }

    @Test
    public void dispatch_skipsAlreadyNotifiedWarnings() {
        WarningEntity alreadyNotified = warning("already-notified", true);
        RecordingMarker marker = new RecordingMarker();
        WarningNotificationDispatcher dispatcher = new WarningNotificationDispatcher(new NotificationCandidateSelector());

        int sentCount = dispatcher.dispatch(
                Collections.singletonList(alreadyNotified),
                warning -> {
                    throw new AssertionError("Already notified warning should not be sent again");
                },
                marker
        );

        assertEquals(0, sentCount);
        assertEquals(0, marker.markedCount);
    }

    private static WarningEntity warning(String warningId, boolean notified) {
        return new WarningEntity(
                warningId,
                "101010100",
                "Storm warning",
                "Storm",
                "Red",
                "Severe storm warning content",
                1_700_000_000_000L,
                false,
                notified
        );
    }

    private static final class RecordingMarker implements WarningNotificationDispatcher.Marker {
        private int markedCount;
        private String lastWarningId;

        @Override
        public void markNotified(String locationId, String warningId) {
            markedCount++;
            lastWarningId = warningId;
        }
    }
}
