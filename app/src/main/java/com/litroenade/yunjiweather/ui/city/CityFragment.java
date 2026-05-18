package com.litroenade.yunjiweather.ui.city;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.model.CityWeatherSummary;
import com.litroenade.yunjiweather.databinding.FragmentCityBinding;
import com.litroenade.yunjiweather.settings.SettingsManager;
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CityFragment extends Fragment {

    private FragmentCityBinding binding;
    private CityViewModel viewModel;
    private SettingsManager settingsManager;
    private Map<String, CityWeatherSummary> latestSummaries = Collections.emptyMap();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(CityViewModel.class);
        settingsManager = new SettingsManager(requireContext());
        binding = FragmentCityBinding.inflate(inflater, container, false);

        binding.addCityButton.setOnClickListener(view -> {
            viewModel.addCity(binding.cityInput.getText().toString());
            binding.cityInput.setText("");
        });
        binding.beijingButton.setOnClickListener(view -> viewModel.addCity("北京"));
        binding.shanghaiButton.setOnClickListener(view -> viewModel.addCity("上海"));
        binding.guangzhouButton.setOnClickListener(view -> viewModel.addCity("广州"));
        binding.shenzhenButton.setOnClickListener(view -> viewModel.addCity("深圳"));

        viewModel.getCities().observe(getViewLifecycleOwner(), this::renderCities);
        viewModel.getCitySummaries().observe(getViewLifecycleOwner(), summaries -> {
            latestSummaries = summaries == null ? Collections.emptyMap() : summaries;
            renderCurrentCities();
        });
        viewModel.getDefaultCity().observe(getViewLifecycleOwner(), defaultCity -> renderCurrentCities());
        viewModel.getMessage().observe(getViewLifecycleOwner(), message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        );
        return binding.getRoot();
    }

    private void renderCurrentCities() {
        List<CityEntity> currentCities = viewModel.getCities().getValue();
        if (currentCities != null) {
            renderCities(currentCities);
        }
    }

    private void renderCities(List<CityEntity> cities) {
        binding.cityListContainer.removeAllViews();
        String defaultCity = viewModel.getDefaultCity().getValue();
        for (CityEntity city : cities) {
            binding.cityListContainer.addView(createCityCard(
                    city,
                    city.cityName.equals(defaultCity),
                    latestSummaries.get(city.locationId)
            ));
        }
        binding.emptyText.setVisibility(cities.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private View createCityCard(CityEntity city, boolean isDefault, CityWeatherSummary summary) {
        MaterialCardView cardView = new MaterialCardView(requireContext());
        cardView.setRadius(18f);
        cardView.setCardElevation(0f);
        cardView.setUseCompatPadding(true);
        cardView.setContentPadding(18, 14, 18, 14);

        View content = LayoutInflater.from(requireContext()).inflate(com.litroenade.yunjiweather.R.layout.item_city_card, cardView, false);
        TextView cityNameText = content.findViewById(com.litroenade.yunjiweather.R.id.city_name_text);
        TextView summaryText = content.findViewById(com.litroenade.yunjiweather.R.id.city_summary_text);
        MaterialButton defaultButton = content.findViewById(com.litroenade.yunjiweather.R.id.set_default_button);
        MaterialButton deleteButton = content.findViewById(com.litroenade.yunjiweather.R.id.delete_city_button);

        cityNameText.setText(city.cityName);
        summaryText.setText(createSummaryText(city, isDefault, summary));
        defaultButton.setEnabled(!isDefault);
        defaultButton.setText(isDefault ? "默认" : "设为默认");
        defaultButton.setOnClickListener(view -> viewModel.setDefaultCity(city));
        deleteButton.setOnClickListener(view -> viewModel.removeCity(city));

        cardView.addView(content);
        return cardView;
    }

    private String createSummaryText(CityEntity city, boolean isDefault, CityWeatherSummary summary) {
        String cityMeta = city.province + " · " + city.country + (isDefault ? " · 默认城市" : "");
        if (summary == null) {
            return cityMeta + "\n天气摘要加载中";
        }
        if (!summary.getErrorMessage().isEmpty()) {
            return cityMeta + "\n" + summary.getErrorMessage();
        }
        String temperatureUnit = settingsManager.getTemperatureUnit();
        String cacheText = summary.isFromCache() ? " · 缓存" : "";
        return cityMeta
                + "\n"
                + summary.getCondition()
                + " "
                + WeatherDisplayUtils.formatTemperature(summary.getTemperature(), temperatureUnit)
                + "  今日 "
                + WeatherDisplayUtils.formatTemperature(summary.getTempMin(), temperatureUnit)
                + " / "
                + WeatherDisplayUtils.formatTemperature(summary.getTempMax(), temperatureUnit)
                + cacheText;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
