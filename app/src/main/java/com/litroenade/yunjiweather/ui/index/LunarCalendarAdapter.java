package com.litroenade.yunjiweather.ui.index;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.litroenade.yunjiweather.R;
import com.litroenade.yunjiweather.databinding.ItemLunarCalendarDayBinding;
import com.litroenade.yunjiweather.utils.LunarCalendarUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LunarCalendarAdapter extends RecyclerView.Adapter<LunarCalendarAdapter.CalendarViewHolder> {

    public interface Listener {
        void onClick(DayItem item);
    }

    private final Listener listener;
    private final List<DayItem> items = new ArrayList<>();

    public LunarCalendarAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitData(List<DayItem> nextItems) {
        List<DayItem> oldItems = new ArrayList<>(items);
        List<DayItem> newItems = nextItems == null ? new ArrayList<>() : new ArrayList<>(nextItems);
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
                DayItem oldItem = oldItems.get(oldItemPosition);
                DayItem newItem = newItems.get(newItemPosition);
                return oldItem.getLunarInfo().getGregorianYear() == newItem.getLunarInfo().getGregorianYear()
                        && oldItem.getLunarInfo().getGregorianMonth() == newItem.getLunarInfo().getGregorianMonth()
                        && oldItem.getLunarInfo().getGregorianDay() == newItem.getLunarInfo().getGregorianDay();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                DayItem oldItem = oldItems.get(oldItemPosition);
                DayItem newItem = newItems.get(newItemPosition);
                return oldItem.isToday() == newItem.isToday()
                        && oldItem.getLunarText().equals(newItem.getLunarText())
                        && oldItem.getFestivalText().equals(newItem.getFestivalText());
            }
        });
        items.clear();
        items.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLunarCalendarDayBinding binding = ItemLunarCalendarDayBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CalendarViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class CalendarViewHolder extends RecyclerView.ViewHolder {
        private final ItemLunarCalendarDayBinding binding;

        private CalendarViewHolder(ItemLunarCalendarDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(DayItem item, Listener listener) {
            int primaryColor = ContextCompat.getColor(
                    binding.getRoot().getContext(),
                    item.isToday() ? R.color.weather_text_inverse : R.color.weather_text_primary
            );
            int secondaryColor = ContextCompat.getColor(
                    binding.getRoot().getContext(),
                    item.isToday() ? R.color.weather_text_inverse : R.color.weather_text_secondary
            );
            binding.getRoot().setBackgroundResource(
                    item.isToday() ? R.drawable.bg_lunar_day_today : R.drawable.bg_lunar_day_normal
            );
            binding.lunarWeekdayText.setText(item.getWeekdayText());
            binding.lunarDayText.setText(item.getDayText());
            binding.lunarDateText.setText(item.getLunarText());
            binding.lunarFestivalText.setText(item.getFestivalText());
            binding.lunarFestivalText.setVisibility(item.getFestivalText().isEmpty() ? View.GONE : View.VISIBLE);
            binding.lunarWeekdayText.setTextColor(secondaryColor);
            binding.lunarDayText.setTextColor(primaryColor);
            binding.lunarDateText.setTextColor(secondaryColor);
            binding.lunarFestivalText.setTextColor(item.isToday()
                    ? primaryColor
                    : ContextCompat.getColor(binding.getRoot().getContext(), R.color.weather_primary));
            String festivalText = item.getFestivalText().isEmpty() ? "无节日" : item.getFestivalText();
            binding.getRoot().setContentDescription(item.getWeekdayText()
                    + "，" + item.getDayText()
                    + "日，" + item.getLunarText()
                    + "，" + festivalText);
            binding.getRoot().setOnClickListener(view -> listener.onClick(item));
        }
    }

    public static final class DayItem {
        private final LunarCalendarUtils.LunarDayInfo lunarInfo;
        private final boolean today;

        private DayItem(LunarCalendarUtils.LunarDayInfo lunarInfo, boolean today) {
            this.lunarInfo = lunarInfo;
            this.today = today;
        }

        public static DayItem create(LunarCalendarUtils.LunarDayInfo lunarInfo, boolean today) {
            return new DayItem(lunarInfo, today);
        }

        public LunarCalendarUtils.LunarDayInfo getLunarInfo() {
            return lunarInfo;
        }

        public boolean isToday() {
            return today;
        }

        public String getWeekdayText() {
            return today ? "今天" : lunarInfo.getWeekdayText();
        }

        public String getDayText() {
            return String.format(Locale.CHINA, "%02d", lunarInfo.getGregorianDay());
        }

        public String getLunarText() {
            return lunarInfo.getLunarText().replace("农历", "");
        }

        public String getFestivalText() {
            String festival = lunarInfo.getFestivalText();
            if (festival == null || festival.trim().isEmpty()) {
                return "";
            }
            return festival;
        }
    }
}
