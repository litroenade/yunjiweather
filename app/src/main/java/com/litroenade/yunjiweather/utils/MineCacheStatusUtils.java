package com.litroenade.yunjiweather.utils;

public final class MineCacheStatusUtils {

    private MineCacheStatusUtils() {
    }

    public static String formatDataUpdateTime(Long latestUpdateTime) {
        if (latestUpdateTime == null) {
            return "数据更新时间：暂无缓存";
        }
        return "数据更新时间：" + DateTimeUtils.formatMinuteTime(latestUpdateTime);
    }
}
