package com.litroenade.yunjiweather.notification;

import com.litroenade.yunjiweather.data.entity.WarningEntity;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NotificationCandidateSelectorTest {

    @Test
    public void selectUnnotified_returnsOnlyWarningsThatHaveNotBeenSent() {
        WarningEntity sent = warning("sent-warning", true);
        WarningEntity unsent = warning("unsent-warning", false);
        NotificationCandidateSelector selector = new NotificationCandidateSelector();

        List<WarningEntity> candidates = selector.selectUnnotified(Arrays.asList(sent, unsent));

        assertEquals(1, candidates.size());
        assertEquals("unsent-warning", candidates.get(0).warningId);
    }

    @Test
    public void selectUnnotified_acceptsEmptyAndNullInput() {
        NotificationCandidateSelector selector = new NotificationCandidateSelector();

        assertEquals(0, selector.selectUnnotified(Collections.emptyList()).size());
        assertEquals(0, selector.selectUnnotified(null).size());
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
}
