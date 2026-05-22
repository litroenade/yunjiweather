package com.litroenade.yunjiweather.utils;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public final class LunarCalendarUtils {

    private static final TimeZone CHINA_TIME_ZONE = TimeZone.getTimeZone("Asia/Shanghai");
    private static final int BASE_YEAR = 1900;
    private static final int BASE_MONTH = 1;
    private static final int BASE_DAY = 31;
    private static final int MIN_YEAR = 1900;
    private static final int MAX_YEAR = 2100;

    private static final int[] LUNAR_INFO = {
            0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
            0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
            0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
            0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
            0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
            0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0,
            0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
            0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6,
            0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
            0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x05ac0, 0x0ab60, 0x096d5, 0x092e0,
            0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
            0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
            0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
            0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
            0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,
            0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0,
            0x0a2e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,
            0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,
            0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160,
            0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252,
            0x0d520
    };

    private static final String[] LUNAR_MONTH_NAMES = {
            "正月", "二月", "三月", "四月", "五月", "六月",
            "七月", "八月", "九月", "十月", "冬月", "腊月"
    };
    private static final String[] WEEKDAY_NAMES = {
            "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"
    };

    private LunarCalendarUtils() {
    }

    public static LunarDayInfo today() {
        Calendar calendar = Calendar.getInstance(CHINA_TIME_ZONE);
        return fromTimeMillis(calendar.getTimeInMillis());
    }

    public static LunarDayInfo fromTimeMillis(long timeMillis) {
        Calendar calendar = Calendar.getInstance(CHINA_TIME_ZONE);
        calendar.setTimeInMillis(timeMillis);
        return fromGregorian(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    public static LunarDayInfo fromGregorian(int year, int month, int day) {
        validateGregorianRange(year, month, day);
        Calendar calendar = Calendar.getInstance(CHINA_TIME_ZONE);
        calendar.clear();
        calendar.set(year, month - 1, day, 12, 0, 0);
        if (calendar.get(Calendar.YEAR) != year
                || calendar.get(Calendar.MONTH) != month - 1
                || calendar.get(Calendar.DAY_OF_MONTH) != day) {
            throw new IllegalArgumentException("公历日期不合法");
        }
        return fromCalendar(calendar);
    }

    public static LunarDayInfo fromDisplayDate(String dateText, long referenceTimeMillis) {
        if (dateText == null || dateText.trim().isEmpty()) {
            throw new IllegalArgumentException("日期文本不能为空");
        }
        String text = dateText.trim().replace('/', '-');
        String[] parts = text.split("-");
        if (parts.length == 3) {
            return fromGregorian(parseNumber(parts[0], "年份"), parseNumber(parts[1], "月份"), parseNumber(parts[2], "日期"));
        }
        if (parts.length != 2) {
            throw new IllegalArgumentException("日期文本格式不正确：" + dateText);
        }
        Calendar reference = Calendar.getInstance(CHINA_TIME_ZONE);
        reference.setTimeInMillis(referenceTimeMillis);
        int referenceYear = reference.get(Calendar.YEAR);
        int referenceMonth = reference.get(Calendar.MONTH) + 1;
        int month = parseNumber(parts[0], "月份");
        int day = parseNumber(parts[1], "日期");
        int year = referenceYear;
        if (referenceMonth >= 10 && month <= 3) {
            year++;
        } else if (referenceMonth <= 3 && month >= 10) {
            year--;
        }
        return fromGregorian(year, month, day);
    }

    private static LunarDayInfo fromCalendar(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int offset = daysBetween(baseCalendar(), calendar);
        int lunarYear = BASE_YEAR;
        while (lunarYear <= MAX_YEAR) {
            int yearDays = lunarYearDays(lunarYear);
            if (offset < yearDays) {
                break;
            }
            offset -= yearDays;
            lunarYear++;
        }
        if (lunarYear > MAX_YEAR) {
            throw new IllegalArgumentException("农历日期超出支持范围");
        }
        int leapMonth = leapMonth(lunarYear);
        boolean leap = false;
        int lunarMonth = 1;
        while (lunarMonth <= 12) {
            int monthDays = leap ? leapDays(lunarYear) : lunarMonthDays(lunarYear, lunarMonth);
            if (offset < monthDays) {
                break;
            }
            offset -= monthDays;
            if (leap) {
                leap = false;
                lunarMonth++;
            } else if (lunarMonth == leapMonth) {
                leap = true;
            } else {
                lunarMonth++;
            }
        }
        int lunarDay = offset + 1;
        String lunarText = "农历" + (leap ? "闰" : "") + LUNAR_MONTH_NAMES[lunarMonth - 1] + formatLunarDay(lunarDay);
        return new LunarDayInfo(
                year,
                month,
                day,
                lunarYear,
                lunarMonth,
                lunarDay,
                leap,
                formatGregorian(year, month, day),
                WEEKDAY_NAMES[calendar.get(Calendar.DAY_OF_WEEK) - 1],
                lunarText,
                resolveFestival(month, day, lunarMonth, lunarDay)
        );
    }

    private static Calendar baseCalendar() {
        Calendar calendar = Calendar.getInstance(CHINA_TIME_ZONE);
        calendar.clear();
        calendar.set(BASE_YEAR, BASE_MONTH - 1, BASE_DAY, 12, 0, 0);
        return calendar;
    }

    private static int daysBetween(Calendar start, Calendar end) {
        long diff = end.getTimeInMillis() - start.getTimeInMillis();
        return (int) (diff / 86_400_000L);
    }

    private static int lunarYearDays(int year) {
        int days = 348;
        int info = LUNAR_INFO[year - BASE_YEAR];
        for (int mask = 0x8000; mask > 0x8; mask >>= 1) {
            if ((info & mask) != 0) {
                days++;
            }
        }
        return days + leapDays(year);
    }

    private static int leapMonth(int year) {
        return LUNAR_INFO[year - BASE_YEAR] & 0xf;
    }

    private static int leapDays(int year) {
        if (leapMonth(year) == 0) {
            return 0;
        }
        return (LUNAR_INFO[year - BASE_YEAR] & 0x10000) == 0 ? 29 : 30;
    }

    private static int lunarMonthDays(int year, int month) {
        return (LUNAR_INFO[year - BASE_YEAR] & (0x10000 >> month)) == 0 ? 29 : 30;
    }

    private static String formatLunarDay(int day) {
        if (day < 1 || day > 30) {
            throw new IllegalArgumentException("农历日期不合法");
        }
        String[] dayNames = {
                "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
                "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
                "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
        };
        return dayNames[day - 1];
    }

    private static String resolveFestival(int gregorianMonth, int gregorianDay, int lunarMonth, int lunarDay) {
        if (gregorianMonth == 1 && gregorianDay == 1) {
            return "元旦";
        }
        if (gregorianMonth == 5 && gregorianDay == 1) {
            return "劳动节";
        }
        if (gregorianMonth == 10 && gregorianDay == 1) {
            return "国庆节";
        }
        if (lunarMonth == 1 && lunarDay == 1) {
            return "春节";
        }
        if (lunarMonth == 1 && lunarDay == 15) {
            return "元宵节";
        }
        if (lunarMonth == 5 && lunarDay == 5) {
            return "端午节";
        }
        if (lunarMonth == 7 && lunarDay == 7) {
            return "七夕";
        }
        if (lunarMonth == 8 && lunarDay == 15) {
            return "中秋节";
        }
        if (lunarMonth == 9 && lunarDay == 9) {
            return "重阳节";
        }
        if (lunarMonth == 12 && lunarDay == 8) {
            return "腊八节";
        }
        return "";
    }

    private static String formatGregorian(int year, int month, int day) {
        return String.format(Locale.CHINA, "%04d年%02d月%02d日", year, month, day);
    }

    private static int parseNumber(String value, String fieldName) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + "必须是数字", exception);
        }
    }

    private static void validateGregorianRange(int year, int month, int day) {
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new IllegalArgumentException("公历年份超出支持范围：" + year);
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("公历月份不合法：" + month);
        }
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("公历日期不合法：" + day);
        }
    }

    public static final class LunarDayInfo {
        private final int gregorianYear;
        private final int gregorianMonth;
        private final int gregorianDay;
        private final int lunarYear;
        private final int lunarMonth;
        private final int lunarDay;
        private final boolean leapMonth;
        private final String gregorianText;
        private final String weekdayText;
        private final String lunarText;
        private final String festivalText;

        private LunarDayInfo(
                int gregorianYear,
                int gregorianMonth,
                int gregorianDay,
                int lunarYear,
                int lunarMonth,
                int lunarDay,
                boolean leapMonth,
                String gregorianText,
                String weekdayText,
                String lunarText,
                String festivalText
        ) {
            this.gregorianYear = gregorianYear;
            this.gregorianMonth = gregorianMonth;
            this.gregorianDay = gregorianDay;
            this.lunarYear = lunarYear;
            this.lunarMonth = lunarMonth;
            this.lunarDay = lunarDay;
            this.leapMonth = leapMonth;
            this.gregorianText = gregorianText;
            this.weekdayText = weekdayText;
            this.lunarText = lunarText;
            this.festivalText = festivalText;
        }

        public int getGregorianYear() {
            return gregorianYear;
        }

        public int getGregorianMonth() {
            return gregorianMonth;
        }

        public int getGregorianDay() {
            return gregorianDay;
        }

        public int getLunarYear() {
            return lunarYear;
        }

        public int getLunarMonth() {
            return lunarMonth;
        }

        public int getLunarDay() {
            return lunarDay;
        }

        public boolean isLeapMonth() {
            return leapMonth;
        }

        public String getGregorianText() {
            return gregorianText;
        }

        public String getWeekdayText() {
            return weekdayText;
        }

        public String getLunarText() {
            return lunarText;
        }

        public String getFestivalText() {
            return festivalText;
        }

        public String getFestivalOrDefaultText() {
            if (festivalText == null || festivalText.trim().isEmpty()) {
                return "今日暂无传统节日";
            }
            return festivalText;
        }
    }
}
