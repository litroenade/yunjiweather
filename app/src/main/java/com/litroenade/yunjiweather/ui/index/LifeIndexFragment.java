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

import com.litroenade.yunjiweather.databinding.FragmentLifeIndexBinding;

public class LifeIndexFragment extends Fragment {

    private FragmentLifeIndexBinding binding;
    private LifeIndexAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LifeIndexViewModel viewModel = new ViewModelProvider(this).get(LifeIndexViewModel.class);
        binding = FragmentLifeIndexBinding.inflate(inflater, container, false);
        adapter = new LifeIndexAdapter(this::showDetail);
        binding.indexRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.indexRecyclerView.setAdapter(adapter);
        binding.indexRecyclerView.setNestedScrollingEnabled(false);
        viewModel.getIndexItems().observe(getViewLifecycleOwner(), adapter::submitData);
        viewModel.getStateText().observe(getViewLifecycleOwner(), binding.indexStateText::setText);
        return binding.getRoot();
    }

    private void showDetail(LifeIndexItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle(item.getName() + "指数")
                .setMessage(item.getDetail())
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
