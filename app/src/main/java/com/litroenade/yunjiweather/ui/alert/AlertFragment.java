package com.litroenade.yunjiweather.ui.alert;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.litroenade.yunjiweather.R;
import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.databinding.FragmentAlertBinding;
import com.litroenade.yunjiweather.utils.DateTimeUtils;

import java.util.List;
import java.util.Locale;

public class AlertFragment extends Fragment {

    private FragmentAlertBinding binding;
    private AlertViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AlertViewModel.class);
        binding = FragmentAlertBinding.inflate(inflater, container, false);
        binding.refreshAlertButton.setOnClickListener(view -> viewModel.refreshState());
        viewModel.getAlertStateText().observe(getViewLifecycleOwner(), binding.alertEmptyText::setText);
        viewModel.getWarnings().observe(getViewLifecycleOwner(), this::renderWarnings);
        viewModel.getLoading().observe(getViewLifecycleOwner(), this::renderLoading);
        return binding.getRoot();
    }

    private void renderLoading(Boolean isLoading) {
        boolean loading = Boolean.TRUE.equals(isLoading);
        binding.alertProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.refreshAlertButton.setEnabled(!loading);
    }

    private void renderWarnings(List<WarningEntity> warningList) {
        binding.warningListContainer.removeAllViews();
        boolean hasWarnings = warningList != null && !warningList.isEmpty();
        binding.alertEmptyCard.setVisibility(hasWarnings ? View.GONE : View.VISIBLE);
        binding.warningListContainer.setVisibility(hasWarnings ? View.VISIBLE : View.GONE);
        if (!hasWarnings) {
            return;
        }
        for (WarningEntity warning : warningList) {
            binding.warningListContainer.addView(createWarningCard(warning));
        }
    }

    private View createWarningCard(WarningEntity warning) {
        MaterialCardView cardView = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        cardView.setLayoutParams(cardParams);
        cardView.setCardElevation(0);
        cardView.setRadius(dp(18));
        cardView.setStrokeWidth(dp(1));
        cardView.setStrokeColor(resolveWarningColor(warning.level));
        cardView.setCardBackgroundColor(warning.isRead ? Color.WHITE : Color.parseColor("#FFF7ED"));
        cardView.setOnClickListener(view -> showWarningDetail(warning));

        LinearLayout contentLayout = new LinearLayout(requireContext());
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dp(18), dp(16), dp(18), dp(16));

        TextView titleView = createTextView(warning.title, 17f, R.color.weather_text_primary, Typeface.BOLD);
        TextView readStateView = createTextView(warning.isRead ? "已读" : "未读", 12f, 0, Typeface.BOLD);
        readStateView.setTextColor(warning.isRead ? Color.parseColor("#667085") : resolveWarningColor(warning.level));
        readStateView.setPadding(0, 0, 0, dp(4));
        TextView metaView = createTextView(
                "级别：" + warning.level + "    类型：" + warning.type,
                13f,
                0,
                Typeface.NORMAL
        );
        metaView.setTextColor(resolveWarningColor(warning.level));
        metaView.setPadding(0, dp(8), 0, 0);

        TextView timeView = createTextView(
                "发布时间：" + DateTimeUtils.formatMinuteTime(warning.publishTime),
                13f,
                R.color.weather_text_secondary,
                Typeface.NORMAL
        );
        timeView.setPadding(0, dp(4), 0, 0);

        TextView contentView = createTextView(warning.content, 14f, R.color.weather_text_secondary, Typeface.NORMAL);
        contentView.setPadding(0, dp(10), 0, 0);
        contentView.setLineSpacing(dp(2), 1f);

        contentLayout.addView(readStateView);
        contentLayout.addView(titleView);
        contentLayout.addView(metaView);
        contentLayout.addView(timeView);
        contentLayout.addView(contentView);
        cardView.addView(contentLayout);
        return cardView;
    }

    private void showWarningDetail(WarningEntity warning) {
        viewModel.markWarningRead(warning.warningId);
        new AlertDialog.Builder(requireContext())
                .setTitle(warning.title)
                .setMessage("级别：" + warning.level
                        + "\n类型：" + warning.type
                        + "\n发布时间：" + DateTimeUtils.formatMinuteTime(warning.publishTime)
                        + "\n\n" + warning.content)
                .setPositiveButton("知道了", null)
                .show();
    }

    private TextView createTextView(String text, float textSize, int colorRes, int style) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextSize(textSize);
        textView.setTypeface(Typeface.DEFAULT, style);
        textView.setGravity(Gravity.START);
        if (colorRes != 0) {
            textView.setTextColor(requireContext().getColor(colorRes));
        }
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return textView;
    }

    private int resolveWarningColor(String level) {
        String normalizedLevel = level.toLowerCase(Locale.ROOT);
        if (normalizedLevel.contains("red") || normalizedLevel.contains("红")) {
            return Color.parseColor("#B42318");
        }
        if (normalizedLevel.contains("orange") || normalizedLevel.contains("橙")) {
            return Color.parseColor("#C4320A");
        }
        if (normalizedLevel.contains("yellow") || normalizedLevel.contains("黄")) {
            return Color.parseColor("#B54708");
        }
        if (normalizedLevel.contains("blue") || normalizedLevel.contains("蓝")) {
            return Color.parseColor("#155EEF");
        }
        return Color.parseColor("#667085");
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
