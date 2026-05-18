package com.litroenade.yunjiweather.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateTimeUtils {

    private static final TimeZone CHINA_TIME_ZONE = TimeZone.getTimeZone("Asia/Shanghai");
    private static final ZoneId CHINA_ZONE_ID = ZoneId.of("Asia/Shanghai");

    private DateTimeUtils() {
    }

    public static boolean isCacheExpired(long nowTime, long expireTime) {
        return nowTime >= expireTime;
    }

    public static String formatCacheUpdateTime(long updateTime) {
        return "缓存更新时间：" + formatMinuteTime(updateTime);
    }

    public static String formatMinuteTime(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        formatter.setTimeZone(CHINA_TIME_ZONE);
        return formatter.format(new Date(time));
    }

    public static long parseQWeatherTime(String value) {
        return OffsetDateTime.parse(value).toInstant().toEpochMilli();
    }

    public static String formatQWeatherHour(String value) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.CHINA);
        formatter.setTimeZone(CHINA_TIME_ZONE);
        return formatter.format(new Date(parseQWeatherTime(value)));
    }

    public static String formatQWeatherDate(String value) {
        return value.substring(5).replace("-", "/");
    }

    public static long parseOpenMeteoLocalTime(String value) {
        return LocalDateTime.parse(value).atZone(CHINA_ZONE_ID).toInstant().toEpochMilli();
    }

    public static String formatOpenMeteoHour(String value) {
        return value.substring(11, 16);
    }

    public static String formatOpenMeteoDate(String value) {
        return value.substring(5).replace("-", "/");
    }
}
