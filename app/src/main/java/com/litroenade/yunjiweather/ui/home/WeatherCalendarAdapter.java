package com.litroenade.yunjiweather.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.litroenade.yunjiweather.data.model.WeatherDailyData;
import com.litroenade.yunjiweather.R;
import com.litroenade.yunjiweather.databinding.ItemWeatherCalendarDayBinding;
import com.litroenade.yunjiweather.utils.LunarCalendarUtils;
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils;
import com.litroenade.yunjiweather.utils.WeatherIconUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class WeatherCalendarAdapter extends RecyclerView.Adapter<WeatherCalendarAdapter.CalendarViewHolder> {

    private final List<WeatherDailyData> items = new ArrayList<>();
    private final OnCalendarDayClickListener listener;
    private String temperatureUnit = WeatherDisplayUtils.TEMPERATURE_CELSIUS;

    public WeatherCalendarAdapter(OnCalendarDayClickListener listener) {
        this.listener = listener;
    }

    public void submitData(List<WeatherDailyData> nextItems, String nextTemperatureUnit) {
        List<WeatherDailyData> oldItems = new ArrayList<>(items);
        List<WeatherDailyData> newItems = nextItems == null ? new ArrayList<>() : new ArrayList<>(nextItems);
        String oldTemperatureUnit = temperatureUnit;
        String newTemperatureUnit = nextTemperatureUnit == null
                ? WeatherDisplayUtils.TEMPERATURE_CELSIUS
                : nextTemperatureUnit;
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldItems.size();
            }

            @Override
            public int getNewListSize() {
                return newItems.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(
                        oldItems.get(oldItemPosition).getDateText(),
                        newItems.get(newItemPosition).getDateText()
                );
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                WeatherDailyData oldItem = oldItems.get(oldItemPosition);
                WeatherDailyData newItem = newItems.get(newItemPosition);
                return Objects.equals(oldItem.getCondition(), newItem.getCondition())
                        && Objects.equals(oldItem.getTempMin(), newItem.getTempMin())
                        && Objects.equals(oldItem.getTempMax(), newItem.getTempMax())
                        && Objects.equals(oldItem.getIconCode(), newItem.getIconCode())
                        && Objects.equals(oldTemperatureUnit, newTemperatureUnit);
            }
        });
        items.clear();
        items.addAll(newItems);
        temperatureUnit = newTemperatureUnit;
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWeatherCalendarDayBinding binding = ItemWeatherCalendarDayBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CalendarViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        holder.bind(items.get(position), temperatureUnit, position == 0);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class CalendarViewHolder extends RecyclerView.ViewHolder {
        private final ItemWeatherCalendarDayBinding binding;
        private final OnCalendarDayClickListener listener;

        private CalendarViewHolder(ItemWeatherCalendarDayBinding binding, OnCalendarDayClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        private void bind(WeatherDailyData item, String temperatureUnit, boolean today) {
            LunarCalendarUtils.LunarDayInfo lunarInfo = null;
            try {
                lunarInfo = LunarCalendarUtils.fromDisplayDate(item.getDateText(), System.currentTimeMillis());
                binding.calendarWeekdayText.setText(lunarInfo.getWeekdayText());
                binding.calendarLunarText.setText(lunarInfo.getLunarText().replace("农历", ""));
            } catch (IllegalArgumentException exception) {
                binding.calendarWeekdayText.setText("--");
                binding.calendarLunarText.setText("农历日期不可用");
            }
            binding.calendarLabelText.setText(today
                    ? binding.getRoot().getContext().getString(R.string.home_calendar_today)
                    : item.getDateText());
            binding.calendarDayText.setText(resolveDayText(item.getDateText()));
            binding.calendarIconImage.setImageResource(WeatherIconUtils.getWeatherIconRes(item.getIconCode()));
            binding.calendarConditionText.setText(item.getCondition());
            binding.calendarTempText.setText(String.format(
                    Locale.CHINA,
                    "%s/%s",
                    WeatherDisplayUtils.formatTemperature(item.getTempMin(), temperatureUnit),
                    WeatherDisplayUtils.formatTemperature(item.getTempMax(), temperatureUnit)
            ));
            binding.getRoot().setStrokeColor(ContextCompat.getColor(
                    binding.getRoot().getContext(),
                    today ? R.color.weather_primary : R.color.weather_stroke
            ));
            binding.getRoot().setCardElevation(today ? 3f : 1f);
            LunarCalendarUtils.LunarDayInfo finalLunarInfo = lunarInfo;
            binding.getRoot().setContentDescription(binding.calendarLabelText.getText()
                    + "，" + binding.calendarWeekdayText.getText()
                    + "，" + binding.calendarLunarText.getText()
                    + "，" + item.getCondition()
                    + "，" + binding.calendarTempText.getText());
            binding.getRoot().setOnClickListener(view -> {
                if (listener != null && finalLunarInfo != null) {
                    listener.onCalendarDayClick(item, finalLunarInfo, temperatureUnit);
                }
            });
        }

        private String resolveDayText(String dateText) {
            if (dateText == null || dateText.trim().isEmpty()) {
                return binding.getRoot().getContext().getString(R.string.home_calendar_unknown_day);
            }
            String text = dateText.trim();
            int splitIndex = Math.max(text.lastIndexOf('/'), text.lastIndexOf('-'));
            if (splitIndex >= 0 && splitIndex < text.length() - 1) {
                return text.substring(splitIndex + 1);
            }
            return text;
        }
    }

    public interface OnCalendarDayClickListener {
        void onCalendarDayClick(
                WeatherDailyData item,
                LunarCalendarUtils.LunarDayInfo lunarInfo,
                String temperatureUnit
        );
    }
}
