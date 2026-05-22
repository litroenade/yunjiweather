package com.litroenade.yunjiweather.ui.index;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.litroenade.yunjiweather.data.model.LifeIndexItem;
import com.litroenade.yunjiweather.databinding.FragmentLifeIndexBinding;
import com.litroenade.yunjiweather.settings.SettingsManager;
import com.litroenade.yunjiweather.ui.calendar.CalendarDetailBottomSheet;
import com.litroenade.yunjiweather.utils.LunarCalendarUtils;
import com.litroenade.yunjiweather.utils.VisualThemeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class LifeIndexFragment extends Fragment {

    private FragmentLifeIndexBinding binding;
    private LifeIndexAdapter adapter;
    private LunarCalendarAdapter calendarAdapter;
    private SettingsManager settingsManager;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LifeIndexViewModel viewModel = new ViewModelProvider(this).get(LifeIndexViewModel.class);
        settingsManager = new SettingsManager(requireContext());
        binding = FragmentLifeIndexBinding.inflate(inflater, container, false);
        VisualThemeUtils.applyAppBackground(binding.getRoot(), settingsManager.getVisualTheme());
        adapter = new LifeIndexAdapter(this::showDetail);
        calendarAdapter = new LunarCalendarAdapter(item -> showCalendarDetail(item.getLunarInfo()));
        binding.indexRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.indexRecyclerView.setAdapter(adapter);
        binding.indexRecyclerView.setNestedScrollingEnabled(false);
        binding.weekCalendarRecyclerView.setLayoutManager(new LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));
        binding.weekCalendarRecyclerView.setAdapter(calendarAdapter);
        binding.weekCalendarRecyclerView.setNestedScrollingEnabled(false);
        renderTodayCalendar();
        calendarAdapter.submitData(buildWeekCalendarItems());
        viewModel.getIndexItems().observe(getViewLifecycleOwner(), adapter::submitData);
        viewModel.getStateText().observe(getViewLifecycleOwner(), binding.indexStateText::setText);
        return binding.getRoot();
    }

    private void renderTodayCalendar() {
        LunarCalendarUtils.LunarDayInfo info = LunarCalendarUtils.today();
        binding.todayCalendarDayText.setText(String.format(Locale.CHINA, "%02d", info.getGregorianDay()));
        binding.todayCalendarDateText.setText(String.format(
                Locale.CHINA,
                "%04d年%02d月 · %s",
                info.getGregorianYear(),
                info.getGregorianMonth(),
                info.getWeekdayText()
        ));
        binding.todayCalendarLunarText.setText(info.getLunarText());
        binding.todayCalendarFestivalText.setText(info.getFestivalOrDefaultText());
    }

    private List<LunarCalendarAdapter.DayItem> buildWeekCalendarItems() {
        List<LunarCalendarAdapter.DayItem> items = new ArrayList<>();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"), Locale.CHINA);
        for (int offset = 0; offset < 7; offset++) {
            Calendar targetCalendar = (Calendar) calendar.clone();
            targetCalendar.add(Calendar.DAY_OF_MONTH, offset);
            LunarCalendarUtils.LunarDayInfo info = LunarCalendarUtils.fromTimeMillis(targetCalendar.getTimeInMillis());
            items.add(LunarCalendarAdapter.DayItem.create(info, offset == 0));
        }
        return items;
    }

    private void showCalendarDetail(LunarCalendarUtils.LunarDayInfo info) {
        CalendarDetailBottomSheet.show(
                requireContext(),
                CalendarDetailBottomSheet.Detail.lifeCalendar(info)
        );
    }

    private void showDetail(LifeIndexItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle(item.getName() + "指数")
                .setMessage(item.getDetail())
                .setPositiveButton("知道了", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && settingsManager != null) {
            VisualThemeUtils.applyAppBackground(binding.getRoot(), settingsManager.getVisualTheme());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter = null;
        calendarAdapter = null;
        settingsManager = null;
        binding = null;
    }
}
