package com.litroenade.yunjiweather.ui.city;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.model.CityWeatherSummary;
import com.litroenade.yunjiweather.databinding.FragmentCityBinding;
import com.litroenade.yunjiweather.settings.SettingsManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CityFragment extends Fragment {

    private FragmentCityBinding binding;
    private CityViewModel viewModel;
    private SettingsManager settingsManager;
    private CityAdapter cityAdapter;
    private Map<String, CityWeatherSummary> latestSummaries = Collections.emptyMap();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(CityViewModel.class);
        settingsManager = new SettingsManager(requireContext());
        binding = FragmentCityBinding.inflate(inflater, container, false);
        setupCityList();

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

    private void setupCityList() {
        cityAdapter = new CityAdapter(new CityAdapter.Listener() {
            @Override
            public void onSetDefault(CityEntity city) {
                viewModel.setDefaultCity(city);
            }

            @Override
            public void onDelete(CityEntity city) {
                viewModel.removeCity(city);
            }
        });
        binding.cityRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.cityRecyclerView.setAdapter(cityAdapter);
        binding.cityRecyclerView.setNestedScrollingEnabled(false);
    }

    private void renderCurrentCities() {
        List<CityEntity> currentCities = viewModel.getCities().getValue();
        if (currentCities != null) {
            renderCities(currentCities);
        }
    }

    private void renderCities(List<CityEntity> cities) {
        String defaultCity = viewModel.getDefaultCity().getValue();
        cityAdapter.submitData(cities, latestSummaries, defaultCity, settingsManager.getTemperatureUnit());
        binding.emptyText.setVisibility(cities.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cityAdapter = null;
        binding = null;
    }
}
