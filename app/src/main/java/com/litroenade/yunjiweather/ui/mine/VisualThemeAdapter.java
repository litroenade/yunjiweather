package com.litroenade.yunjiweather.ui.mine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.litroenade.yunjiweather.R;
import com.litroenade.yunjiweather.databinding.ItemVisualThemeOptionBinding;
import com.litroenade.yunjiweather.utils.VisualTheme;

import java.util.ArrayList;
import java.util.List;

public final class VisualThemeAdapter extends RecyclerView.Adapter<VisualThemeAdapter.ThemeViewHolder> {

    public interface Listener {
        void onThemeClick(VisualTheme theme);
    }

    private final Listener listener;
    private final List<VisualTheme> themes = new ArrayList<>();
    private String selectedThemeKey;

    public VisualThemeAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitData(List<VisualTheme> nextThemes) {
        themes.clear();
        if (nextThemes != null) {
            themes.addAll(nextThemes);
        }
        notifyDataSetChanged();
    }

    public void setSelectedThemeKey(String selectedThemeKey) {
        this.selectedThemeKey = selectedThemeKey;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVisualThemeOptionBinding binding = ItemVisualThemeOptionBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ThemeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeViewHolder holder, int position) {
        holder.bind(themes.get(position), themes.get(position).getKey().equals(selectedThemeKey), listener);
    }

    @Override
    public int getItemCount() {
        return themes.size();
    }

    static final class ThemeViewHolder extends RecyclerView.ViewHolder {
        private final ItemVisualThemeOptionBinding binding;

        private ThemeViewHolder(ItemVisualThemeOptionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(VisualTheme theme, boolean selected, Listener listener) {
            int accentColor = ContextCompat.getColor(binding.getRoot().getContext(), theme.getAccentColorRes());
            int strokeColor = selected
                    ? accentColor
                    : ContextCompat.getColor(binding.getRoot().getContext(), R.color.weather_stroke);
            binding.themeNameText.setText(theme.getDisplayName());
            binding.themeDescriptionText.setText(theme.getShortDescription());
            binding.themePreviewPanel.setBackgroundResource(theme.getBackgroundRes());
            binding.themeCard.setStrokeColor(strokeColor);
            binding.themeCard.setStrokeWidth(selected ? 3 : 1);
            binding.themeCard.setAlpha(selected ? 1.0f : 0.78f);
            binding.themeSelectedText.setTextColor(accentColor);
            binding.themeSelectedText.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
            String selectedText = selected ? "，已选择" : "";
            binding.getRoot().setContentDescription(theme.getDisplayName() + "，" + theme.getShortDescription() + selectedText);
            binding.getRoot().setOnClickListener(view -> listener.onThemeClick(theme));
        }
    }
}
