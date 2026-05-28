package com.litroenade.yunjiweather.ui.compose;

public enum WeatherNavigationTarget {
    MANAGE_CITIES(Surface.FULL_PAGE),
    SEARCH_CITY(Surface.SHEET),
    DESKTOP_WEATHER(Surface.FULL_PAGE),
    PERSONALIZATION(Surface.FULL_PAGE),
    SETTINGS(Surface.FULL_PAGE),
    ALERTS(Surface.FULL_PAGE),
    LIFE_INDEX(Surface.FULL_PAGE);

    private final Surface surface;

    WeatherNavigationTarget(Surface surface) {
        this.surface = surface;
    }

    public Surface getSurface() {
        return surface;
    }

    public boolean isFullPage() {
        return surface == Surface.FULL_PAGE;
    }

    public boolean isSheet() {
        return surface == Surface.SHEET;
    }

    public enum Surface {
        FULL_PAGE,
        SHEET
    }
}
