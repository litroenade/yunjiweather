package com.litroenade.yunjiweather.ui.index;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.litroenade.yunjiweather.R;
import com.litroenade.yunjiweather.databinding.FragmentLifeIndexBinding;

import java.util.List;

public class LifeIndexFragment extends Fragment {

    private FragmentLifeIndexBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LifeIndexViewModel viewModel = new ViewModelProvider(this).get(LifeIndexViewModel.class);
        binding = FragmentLifeIndexBinding.inflate(inflater, container, false);
        viewModel.getIndexItems().observe(getViewLifecycleOwner(), this::renderIndexes);
        viewModel.getStateText().observe(getViewLifecycleOwner(), binding.indexStateText::setText);
        return binding.getRoot();
    }

    private void renderIndexes(List<LifeIndexItem> items) {
        binding.indexGrid.removeAllViews();
        for (LifeIndexItem item : items) {
            binding.indexGrid.addView(createIndexCard(item));
        }
    }

    private View createIndexCard(LifeIndexItem item) {
        MaterialCardView cardView = new MaterialCardView(requireContext());
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(6, 6, 6, 6);
        cardView.setLayoutParams(params);
        cardView.setRadius(18f);
        cardView.setCardElevation(0f);
        cardView.setUseCompatPadding(true);
        cardView.setContentPadding(16, 16, 16, 16);

        View content = LayoutInflater.from(requireContext()).inflate(R.layout.item_life_index_card, cardView, false);
        TextView nameText = content.findViewById(R.id.index_name_text);
        TextView levelText = content.findViewById(R.id.index_level_text);
        TextView adviceText = content.findViewById(R.id.index_advice_text);
        nameText.setText(item.getName());
        levelText.setText(item.getLevel());
        adviceText.setText(item.getAdvice());
        cardView.addView(content);
        cardView.setOnClickListener(view -> showDetail(item));
        return cardView;
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
        binding = null;
    }
}
