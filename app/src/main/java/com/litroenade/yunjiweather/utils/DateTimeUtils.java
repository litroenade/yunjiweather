package com.litroenade.yunjiweather.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateTimeUtils {

    private static final TimeZone CHINA_TIME_ZONE = TimeZone.getTimeZone("Asia/Shanghai");
    private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

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
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("天气时间不能为空");
        }
        String text = value.trim();
        int offsetIndex = findOffsetIndex(text);
        if (offsetIndex <= 0) {
            throw new IllegalArgumentException("天气时间必须包含时区偏移：" + value);
        }
        String dateTimeText = text.substring(0, offsetIndex);
        String pattern = dateTimeText.length() == 19 ? "yyyy-MM-dd'T'HH:mm:ss" : "yyyy-MM-dd'T'HH:mm";
        long localTimeAsUtc = parseTime(dateTimeText, pattern, UTC_TIME_ZONE);
        return localTimeAsUtc - parseOffsetMillis(text.substring(offsetIndex));
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
        return parseTime(value, "yyyy-MM-dd'T'HH:mm", CHINA_TIME_ZONE);
    }

    public static String formatOpenMeteoHour(String value) {
        return value.substring(11, 16);
    }

    public static String formatOpenMeteoDate(String value) {
        return value.substring(5).replace("-", "/");
    }

    private static int findOffsetIndex(String value) {
        for (int i = 10; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current == '+' || current == '-') {
                return i;
            }
        }
        return -1;
    }

    private static long parseOffsetMillis(String offsetText) {
        if (offsetText.length() != 6 || offsetText.charAt(3) != ':') {
            throw new IllegalArgumentException("天气时间时区格式不正确：" + offsetText);
        }
        int sign = offsetText.charAt(0) == '-' ? -1 : 1;
        int hours = parseNumber(offsetText.substring(1, 3), "时区小时");
        int minutes = parseNumber(offsetText.substring(4, 6), "时区分钟");
        return sign * ((hours * 60L + minutes) * 60_000L);
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

    private static int parseNumber(String value, String fieldName) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + "必须是数字", exception);
        }
    }
}
