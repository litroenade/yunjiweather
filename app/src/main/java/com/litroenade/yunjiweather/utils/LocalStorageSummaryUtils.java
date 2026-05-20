package com.litroenade.yunjiweather.utils;

public final class LocalStorageSummaryUtils {

    private LocalStorageSummaryUtils() {
    }

    public static String formatSummary(boolean accountExists, int cityCount, int cacheCount, int warningCount) {
        return "本地账户：" + (accountExists ? "已保存" : "未找到")
                + "\n城市数据：" + cityCount + " 个"
                + "\n天气缓存：" + cacheCount + " 条"
                + "\n预警记录：" + warningCount + " 条";
    }
}
