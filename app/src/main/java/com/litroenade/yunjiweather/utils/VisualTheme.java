package com.litroenade.yunjiweather.utils;

public final class VisualTheme {

    private final String key;
    private final String displayName;
    private final String shortDescription;
    private final boolean customSlot;
    private final boolean selectable;

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
        this(key, displayName, shortDescription, customSlot, true);
    }

    public VisualTheme(
            String key,
            String displayName,
            String shortDescription,
            boolean customSlot,
            boolean selectable
    ) {
        this.key = key;
        this.displayName = displayName;
        this.shortDescription = shortDescription;
        this.customSlot = customSlot;
        this.selectable = selectable;
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

    public boolean isSelectable() {
        return selectable;
    }
}
