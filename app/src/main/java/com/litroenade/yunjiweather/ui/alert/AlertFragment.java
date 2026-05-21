package com.litroenade.yunjiweather.ui.alert;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.databinding.FragmentAlertBinding;
import com.litroenade.yunjiweather.utils.DateTimeUtils;

import java.util.List;

public class AlertFragment extends Fragment {

    private FragmentAlertBinding binding;
    private AlertViewModel viewModel;
    private AlertAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AlertViewModel.class);
        binding = FragmentAlertBinding.inflate(inflater, container, false);
        adapter = new AlertAdapter(this::showWarningDetail);
        binding.warningRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.warningRecyclerView.setAdapter(adapter);
        binding.warningRecyclerView.setNestedScrollingEnabled(false);
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
        boolean hasWarnings = warningList != null && !warningList.isEmpty();
        binding.alertEmptyCard.setVisibility(hasWarnings ? View.GONE : View.VISIBLE);
        binding.warningRecyclerView.setVisibility(hasWarnings ? View.VISIBLE : View.GONE);
        adapter.submitData(warningList);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter = null;
        binding = null;
    }
}
