package com.litroenade.yunjiweather.widget;

public final class WidgetStyleSpec {

    private static final WidgetStyleSpec COMPACT = new WidgetStyleSpec(
            WeatherWidgetLayoutMode.COMPACT,
            140,
            140,
            140,
            176,
            22,
            14,
            34,
            false,
            true,
            false
    );
    private static final WidgetStyleSpec STANDARD = new WidgetStyleSpec(
            WeatherWidgetLayoutMode.STANDARD,
            220,
            140,
            220,
            140,
            22,
            18,
            44,
            true,
            true,
            false
    );
    private static final WidgetStyleSpec EXPANDED = new WidgetStyleSpec(
            WeatherWidgetLayoutMode.EXPANDED,
            300,
            150,
            300,
            150,
            22,
            18,
            48,
            true,
            true,
            true
    );

    private final WeatherWidgetLayoutMode mode;
    private final int minWidthDp;
    private final int minHeightDp;
    private final int previewWidthDp;
    private final int previewHeightDp;
    private final int cornerRadiusDp;
    private final int contentPaddingDp;
    private final int temperatureTextSizeSp;
    private final boolean updateTimeVisible;
    private final boolean detailsVisible;
    private final boolean adviceVisible;

    private WidgetStyleSpec(
            WeatherWidgetLayoutMode mode,
            int minWidthDp,
            int minHeightDp,
            int previewWidthDp,
            int previewHeightDp,
            int cornerRadiusDp,
            int contentPaddingDp,
            int temperatureTextSizeSp,
            boolean updateTimeVisible,
            boolean detailsVisible,
            boolean adviceVisible
    ) {
        this.mode = mode;
        this.minWidthDp = minWidthDp;
        this.minHeightDp = minHeightDp;
        this.previewWidthDp = previewWidthDp;
        this.previewHeightDp = previewHeightDp;
        this.cornerRadiusDp = cornerRadiusDp;
        this.contentPaddingDp = contentPaddingDp;
        this.temperatureTextSizeSp = temperatureTextSizeSp;
        this.updateTimeVisible = updateTimeVisible;
        this.detailsVisible = detailsVisible;
        this.adviceVisible = adviceVisible;
    }

    public static WidgetStyleSpec forMode(WeatherWidgetLayoutMode mode) {
        if (mode == WeatherWidgetLayoutMode.COMPACT) {
            return COMPACT;
        }
        if (mode == WeatherWidgetLayoutMode.EXPANDED) {
            return EXPANDED;
        }
        return STANDARD;
    }

    public static WeatherWidgetLayoutMode modeForSize(int minWidthDp, int minHeightDp) {
        if (minWidthDp <= 0 && minHeightDp <= 0) {
            return WeatherWidgetLayoutMode.STANDARD;
        }
        if (minWidthDp >= EXPANDED.minWidthDp && minHeightDp >= EXPANDED.minHeightDp) {
            return WeatherWidgetLayoutMode.EXPANDED;
        }
        if (minWidthDp >= STANDARD.minWidthDp && minHeightDp >= STANDARD.minHeightDp) {
            return WeatherWidgetLayoutMode.STANDARD;
        }
        return WeatherWidgetLayoutMode.COMPACT;
    }

    public WeatherWidgetLayoutMode getMode() {
        return mode;
    }

    public int getMinWidthDp() {
        return minWidthDp;
    }

    public int getMinHeightDp() {
        return minHeightDp;
    }

    public int getPreviewWidthDp() {
        return previewWidthDp;
    }

    public int getPreviewHeightDp() {
        return previewHeightDp;
    }

    public int getCornerRadiusDp() {
        return cornerRadiusDp;
    }

    public int getContentPaddingDp() {
        return contentPaddingDp;
    }

    public int getTemperatureTextSizeSp() {
        return temperatureTextSizeSp;
    }

    public boolean isUpdateTimeVisible() {
        return updateTimeVisible;
    }

    public boolean isDetailsVisible() {
        return detailsVisible;
    }

    public boolean isAdviceVisible() {
        return adviceVisible;
    }
}
