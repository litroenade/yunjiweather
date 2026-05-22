package com.litroenade.yunjiweather.utils;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class LunarCalendarUtilsTest {

    @Test
    public void fromGregorianReturnsSpringFestival() {
        LunarCalendarUtils.LunarDayInfo info = LunarCalendarUtils.fromGregorian(2024, 2, 10);

        assertEquals("星期六", info.getWeekdayText());
        assertEquals("农历正月初一", info.getLunarText());
        assertEquals("春节", info.getFestivalText());
        assertEquals("2024年02月10日", info.getGregorianText());
    }

    @Test
    public void fromGregorianReturnsMidAutumnFestival() {
        LunarCalendarUtils.LunarDayInfo info = LunarCalendarUtils.fromGregorian(2024, 9, 17);

        assertEquals("星期二", info.getWeekdayText());
        assertEquals("农历八月十五", info.getLunarText());
        assertEquals("中秋节", info.getFestivalText());
    }

    @Test
    public void fromDisplayDateInfersNextYearWhenMonthWraps() {
        long referenceTime = createChinaTime(2026, 12, 30);

        LunarCalendarUtils.LunarDayInfo info = LunarCalendarUtils.fromDisplayDate("01/01", referenceTime);

        assertEquals("2027年01月01日", info.getGregorianText());
        assertEquals("星期五", info.getWeekdayText());
    }

    @Test
    public void fromDisplayDateAcceptsFullDateText() {
        LunarCalendarUtils.LunarDayInfo info = LunarCalendarUtils.fromDisplayDate(
                "2024-02-10",
                createChinaTime(2024, 1, 1)
        );

        assertEquals("农历正月初一", info.getLunarText());
        assertEquals("春节", info.getFestivalText());
    }

    @Test
    public void fromTimeMillisUsesDateWithoutMorningDrift() {
        LunarCalendarUtils.LunarDayInfo expected = LunarCalendarUtils.fromGregorian(2026, 5, 21);

        LunarCalendarUtils.LunarDayInfo actual = LunarCalendarUtils.fromTimeMillis(
                createChinaTime(2026, 5, 21, 9)
        );

        assertEquals(expected.getGregorianText(), actual.getGregorianText());
        assertEquals(expected.getLunarText(), actual.getLunarText());
        assertEquals(expected.getWeekdayText(), actual.getWeekdayText());
    }

    private long createChinaTime(int year, int month, int day) {
        return createChinaTime(year, month, day, 12);
    }

    private long createChinaTime(int year, int month, int day, int hour) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
        calendar.clear();
        calendar.set(year, month - 1, day, hour, 0, 0);
        return calendar.getTimeInMillis();
    }
}
