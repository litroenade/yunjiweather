package com.litroenade.yunjiweather.utils;

public final class VisualTheme {

    private final String key;
    private final String displayName;
    private final String shortDescription;
    private final boolean customSlot;

    public VisualTheme(
            String key,
            String displayName,
            String shortDescription
    ) {
        this(key, displayName, shortDescription, false);
    }

    public VisualTheme(
            String key,
            String displayName,
            String shortDescription,
            boolean customSlot
    ) {
        this.key = key;
        this.displayName = displayName;
        this.shortDescription = shortDescription;
        this.customSlot = customSlot;
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

    public boolean isCustomSlot() {
        return customSlot;
    }
}
