package com.litroenade.yunjiweather.ui.calendar;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.litroenade.yunjiweather.databinding.DialogCalendarDetailBinding;
import com.litroenade.yunjiweather.utils.LunarCalendarUtils;

import java.util.Locale;

public final class CalendarDetailBottomSheet {

    private CalendarDetailBottomSheet() {
    }

    public static void show(Context context, Detail detail) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        DialogCalendarDetailBinding binding = DialogCalendarDetailBinding.inflate(LayoutInflater.from(context));
        bind(binding, detail);
        binding.calendarDetailCloseButton.setOnClickListener(view -> dialog.dismiss());
        dialog.setContentView(binding.getRoot());
        dialog.setOnShowListener(dialogInterface -> {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundColor(Color.TRANSPARENT);
            }
        });
        dialog.show();
    }

    private static void bind(DialogCalendarDetailBinding binding, Detail detail) {
        LunarCalendarUtils.LunarDayInfo info = detail.getLunarInfo();
        binding.calendarDetailTitleText.setText(detail.getTitle());
        binding.calendarDetailMonthText.setText(String.format(
                Locale.CHINA,
                "%04d年%02d月",
                info.getGregorianYear(),
                info.getGregorianMonth()
        ));
        binding.calendarDetailDayText.setText(String.format(Locale.CHINA, "%02d", info.getGregorianDay()));
        binding.calendarDetailWeekdayText.setText(info.getWeekdayText());
        binding.calendarDetailLunarText.setText(info.getLunarText().replace("农历", ""));
        binding.calendarDetailFestivalText.setText(resolveFestivalText(info));
        if (detail.hasWeather()) {
            binding.calendarDetailWeatherGroup.setVisibility(View.VISIBLE);
            binding.calendarDetailWeatherConditionText.setText(detail.getWeatherConditionText());
            binding.calendarDetailWeatherTempText.setText(detail.getWeatherTemperatureText());
        } else {
            binding.calendarDetailWeatherGroup.setVisibility(View.GONE);
        }
        binding.calendarDetailAdviceText.setText(detail.getAdviceText());
    }

    private static String resolveFestivalText(LunarCalendarUtils.LunarDayInfo info) {
        String festival = info.getFestivalText();
        if (festival == null || festival.trim().isEmpty()) {
            return "暂无传统节日";
        }
        return festival;
    }

    public static final class Detail {
        private final String title;
        private final LunarCalendarUtils.LunarDayInfo lunarInfo;
        private final String weatherConditionText;
        private final String weatherTemperatureText;
        private final String adviceText;

        private Detail(
                String title,
                LunarCalendarUtils.LunarDayInfo lunarInfo,
                String weatherConditionText,
                String weatherTemperatureText,
                String adviceText
        ) {
            this.title = title;
            this.lunarInfo = lunarInfo;
            this.weatherConditionText = weatherConditionText;
            this.weatherTemperatureText = weatherTemperatureText;
            this.adviceText = adviceText;
        }

        public static Detail weatherCalendar(
                LunarCalendarUtils.LunarDayInfo lunarInfo,
                String weatherConditionText,
                String weatherTemperatureText
        ) {
            return new Detail(
                    "天气日历",
                    lunarInfo,
                    weatherConditionText,
                    weatherTemperatureText,
                    "可结合当天温度、天气和生活指数安排穿衣、出行与户外活动。"
            );
        }

        public static Detail lifeCalendar(LunarCalendarUtils.LunarDayInfo lunarInfo) {
            return new Detail(
                    "日历详情",
                    lunarInfo,
                    "",
                    "",
                    "农历和节日信息用于辅助安排生活事项，具体出行仍建议结合首页天气和预警状态。"
            );
        }

        private String getTitle() {
            return title;
        }

        private LunarCalendarUtils.LunarDayInfo getLunarInfo() {
            return lunarInfo;
        }

        private boolean hasWeather() {
            return weatherConditionText != null && !weatherConditionText.trim().isEmpty();
        }

        private String getWeatherConditionText() {
            return weatherConditionText;
        }

        private String getWeatherTemperatureText() {
            return weatherTemperatureText;
        }

        private String getAdviceText() {
            return adviceText;
        }
    }
}
