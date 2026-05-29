package com.litroenade.yunjiweather.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateTimeUtils {

    private static final TimeZone CHINA_TIME_ZONE = TimeZone.getTimeZone("Asia/Shanghai");

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

    public static long parseOpenMeteoLocalTime(String value) {
        return parseTime(value, "yyyy-MM-dd'T'HH:mm", CHINA_TIME_ZONE);
    }

    public static String formatOpenMeteoHour(String value) {
        return value.substring(11, 16);
    }

    public static String formatOpenMeteoDate(String value) {
        return value.substring(5).replace("-", "/");
    }

    private static long parseTime(String value, String pattern, TimeZone timeZone) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("时间不能为空");
        }
        SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.CHINA);
        formatter.setLenient(false);
        formatter.setTimeZone(timeZone);
        try {
            return formatter.parse(value.trim()).getTime();
        } catch (ParseException exception) {
            throw new IllegalArgumentException("时间格式不正确：" + value, exception);
        }
    }
}
