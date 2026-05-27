package com.litroenade.yunjiweather.utils;

public final class VisualTheme {

    private final String key;
    private final String displayName;
    private final String shortDescription;

    public VisualTheme(
            String key,
            String displayName,
            String shortDescription
    ) {
        this.key = key;
        this.displayName = displayName;
        this.shortDescription = shortDescription;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getShortDescription() {
        return shortDescription;
    }
}
