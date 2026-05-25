package com.litroenade.yunjiweather.notification;

import com.litroenade.yunjiweather.data.entity.WarningEntity;

import java.util.ArrayList;
import java.util.List;

public final class NotificationCandidateSelector {

    public List<WarningEntity> selectUnnotified(List<WarningEntity> warnings) {
        List<WarningEntity> candidates = new ArrayList<>();
        if (warnings == null) {
            return candidates;
        }
        for (WarningEntity warning : warnings) {
            if (warning != null && !warning.isNotified) {
                candidates.add(warning);
            }
        }
        return candidates;
    }
}
