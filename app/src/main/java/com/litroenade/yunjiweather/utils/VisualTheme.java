package com.litroenade.yunjiweather.utils;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

public final class VisualTheme {

    private final String key;
    private final String displayName;
    private final String shortDescription;
    private final int backgroundRes;
    private final int accentColorRes;
    private final int homePrimaryTextColorRes;
    private final int homeSecondaryTextColorRes;

    public VisualTheme(
            String key,
            String displayName,
            String shortDescription,
            @DrawableRes int backgroundRes,
            @ColorRes int accentColorRes,
            @ColorRes int homePrimaryTextColorRes,
            @ColorRes int homeSecondaryTextColorRes
    ) {
        this.key = key;
        this.displayName = displayName;
        this.shortDescription = shortDescription;
        this.backgroundRes = backgroundRes;
        this.accentColorRes = accentColorRes;
        this.homePrimaryTextColorRes = homePrimaryTextColorRes;
        this.homeSecondaryTextColorRes = homeSecondaryTextColorRes;
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

    @DrawableRes
    public int getBackgroundRes() {
        return backgroundRes;
    }

    @ColorRes
    public int getAccentColorRes() {
        return accentColorRes;
    }

    @ColorRes
    public int getHomePrimaryTextColorRes() {
        return homePrimaryTextColorRes;
    }

    @ColorRes
    public int getHomeSecondaryTextColorRes() {
        return homeSecondaryTextColorRes;
    }
}
