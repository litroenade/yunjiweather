package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.data.entity.WarningEntity;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class WarningRefreshResult {

    private final String locationId;
    private final List<WarningEntity> warnings;
    private final WarningSource source;

    public WarningRefreshResult(String locationId, List<WarningEntity> warnings, WarningSource source) {
        this.locationId = Objects.requireNonNull(locationId, "locationId");
        this.warnings = Collections.unmodifiableList(Objects.requireNonNull(warnings, "warnings"));
        this.source = Objects.requireNonNull(source, "source");
    }

    public String getLocationId() {
        return locationId;
    }

    public List<WarningEntity> getWarnings() {
        return warnings;
    }

    public WarningSource getSource() {
        return source;
    }
}
