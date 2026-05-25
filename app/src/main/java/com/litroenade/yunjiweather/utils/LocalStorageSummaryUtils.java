package com.litroenade.yunjiweather.utils;

public final class LocalStorageSummaryUtils {

    private LocalStorageSummaryUtils() {
    }

    public static String formatSummary(int cityCount, int cacheCount, int warningCount) {
        return "城市数据：" + cityCount + " 个"
                + "\n天气缓存：" + cacheCount + " 条"
                + "\n预警记录：" + warningCount + " 条";
    }
}
