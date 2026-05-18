package com.litroenade.yunjiweather.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.model.WeatherDailyData;
import com.litroenade.yunjiweather.data.model.WeatherHourlyData;
import com.litroenade.yunjiweather.databinding.FragmentHomeBinding;
import com.litroenade.yunjiweather.settings.SettingsManager;
import com.litroenade.yunjiweather.utils.DateTimeUtils;
import com.litroenade.yunjiweather.utils.PermissionUtils;
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils;
import com.litroenade.yunjiweather.utils.WeatherIconUtils;

import java.util.Map;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private SettingsManager settingsManager;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private WeatherAnimationView weatherAnimationView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::handleLocationPermissionResult
        );
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        settingsManager = new SettingsManager(requireContext());

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        binding.locationButton.setOnClickListener(view -> handleLocationClick());
        binding.refreshButton.setOnClickListener(view -> homeViewModel.refresh());
        binding.retryButton.setOnClickListener(view -> homeViewModel.refresh());
        binding.swipeRefresh.setOnRefreshListener(homeViewModel::refresh);
        homeViewModel.getUiState().observe(getViewLifecycleOwner(), this::renderState);
        homeViewModel.getMessage().observe(getViewLifecycleOwner(), message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        );

        if (homeViewModel.getUiState().getValue() == null) {
            homeViewModel.loadHomeWeather();
        }
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (homeViewModel != null && homeViewModel.getUiState().getValue() != null) {
            renderState(homeViewModel.getUiState().getValue());
        }
    }

    private void handleLocationClick() {
        if (!PermissionUtils.hasLocationPermission(requireContext())) {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
            return;
        }
        requestCurrentLocation();
    }

    private void handleLocationPermissionResult(Map<String, Boolean> result) {
        boolean granted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION))
                || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
        if (granted) {
            requestCurrentLocation();
        } else if (isAdded()) {
            Toast.makeText(requireContext(), "未授予定位权限，请手动选择城市", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestCurrentLocation() {
        if (!isAdded()) {
            return;
        }
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Toast.makeText(requireContext(), "无法获取系统定位服务", Toast.LENGTH_SHORT).show();
            return;
        }
        String provider = resolveLocationProvider(locationManager);
        if (provider == null) {
            Toast.makeText(requireContext(), "请先开启系统定位服务", Toast.LENGTH_SHORT).show();
            return;
        }
        CancellationSignal cancellationSignal = new CancellationSignal();
        locationManager.getCurrentLocation(
                provider,
                cancellationSignal,
                ContextCompat.getMainExecutor(requireContext()),
                this::handleLocationResult
        );
    }

    private String resolveLocationProvider(LocationManager locationManager) {
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        }
        return null;
    }

    private void handleLocationResult(Location location) {
        if (!isAdded()) {
            return;
        }
        if (location == null) {
            Toast.makeText(requireContext(), "定位失败，请手动选择城市", Toast.LENGTH_SHORT).show();
            return;
        }
        homeViewModel.updateDefaultCityByLocation(location.getLatitude(), location.getLongitude());
    }

    private void renderState(UiState<HomeWeatherData> state) {
        if (state == null || binding == null) {
            return;
        }
        binding.swipeRefresh.setRefreshing(false);
        binding.progressBar.setVisibility(state.getStatus() == UiState.Status.LOADING ? View.VISIBLE : View.GONE);
        binding.errorContainer.setVisibility(View.GONE);
        binding.contentContainer.setVisibility(View.GONE);
        binding.cacheStateText.setVisibility(View.GONE);

        if (state.getStatus() == UiState.Status.LOADING) {
            return;
        }
        if (state.getStatus() == UiState.Status.ERROR || state.getStatus() == UiState.Status.EMPTY) {
            binding.errorContainer.setVisibility(View.VISIBLE);
            binding.errorMessageText.setText(state.getMessage());
            return;
        }
        HomeWeatherData data = state.getData();
        if (data == null) {
            binding.errorContainer.setVisibility(View.VISIBLE);
            binding.errorMessageText.setText("天气数据为空，请稍后重试。");
            return;
        }
        renderWeatherData(data);
        if (state.getStatus() == UiState.Status.CACHE) {
            binding.cacheStateText.setText(state.getMessage() + " " + DateTimeUtils.formatCacheUpdateTime(state.getUpdateTime()));
            binding.cacheStateText.setVisibility(View.VISIBLE);
        }
    }

    private void renderWeatherData(HomeWeatherData data) {
        String temperatureUnit = settingsManager.getTemperatureUnit();
        String windUnit = settingsManager.getWindUnit();
        binding.contentContainer.setVisibility(View.VISIBLE);
        binding.homeRoot.setBackgroundResource(WeatherIconUtils.getWeatherBackgroundRes(data.getIconCode()));
        binding.weatherIconImage.setImageResource(WeatherIconUtils.getWeatherIconRes(data.getIconCode()));
        renderWeatherIconAnimation(data.getIconCode());
        binding.cityNameText.setText(data.getCityName());
        binding.tempText.setText(WeatherDisplayUtils.formatTemperature(data.getTemperature(), temperatureUnit));
        binding.conditionText.setText(data.getCondition());
        binding.feelsLikeText.setText("体感 " + WeatherDisplayUtils.formatTemperature(data.getFeelsLike(), temperatureUnit));
        binding.tempRangeText.setText("今日 "
                + WeatherDisplayUtils.formatTemperature(data.getTempMin(), temperatureUnit)
                + " / "
                + WeatherDisplayUtils.formatTemperature(data.getTempMax(), temperatureUnit));
        binding.humidityText.setText("湿度 " + data.getHumidity() + "%");
        binding.windText.setText(WeatherDisplayUtils.formatWind(
                data.getWindDir(),
                data.getWindScale(),
                data.getWindSpeed(),
                windUnit
        ));
        binding.pressureText.setText("气压 " + data.getPressure() + " hPa");
        binding.visibilityText.setText("能见度 " + data.getVisibility() + " km");
        binding.airQualityText.setText("AQI " + data.getAirQualityIndex() + " · " + data.getAirQualityCategory());
        binding.airPrimaryText.setText("首要污染物：" + data.getPrimaryPollutant());
        binding.clothingAdviceText.setText("穿衣：" + data.getClothingAdvice());
        binding.travelAdviceText.setText("出行：" + data.getTravelAdvice());
        binding.updateTimeText.setText(DateTimeUtils.formatCacheUpdateTime(data.getUpdateTime()).replace("缓存", ""));
        renderHourlyForecasts(data, temperatureUnit);
        renderDailyForecasts(data, temperatureUnit);
    }

    private void renderWeatherIconAnimation(String iconCode) {
        binding.weatherIconImage.animate().cancel();
        binding.weatherIconImage.setScaleX(1f);
        binding.weatherIconImage.setScaleY(1f);
        binding.weatherIconImage.setRotation(0f);
        if (!settingsManager.isAnimationEnabled()) {
            binding.weatherIconImage.setVisibility(View.VISIBLE);
            if (weatherAnimationView != null) {
                weatherAnimationView.setVisibility(View.GONE);
            }
            return;
        }
        ensureWeatherAnimationView();
        binding.weatherIconImage.setVisibility(View.GONE);
        weatherAnimationView.setIconCode(iconCode);
        weatherAnimationView.setVisibility(View.VISIBLE);
    }

    private void ensureWeatherAnimationView() {
        if (weatherAnimationView != null) {
            return;
        }
        ViewGroup parent = (ViewGroup) binding.weatherIconImage.getParent();
        int iconIndex = parent.indexOfChild(binding.weatherIconImage);
        weatherAnimationView = new WeatherAnimationView(requireContext());
        ViewGroup.LayoutParams iconParams = binding.weatherIconImage.getLayoutParams();
        weatherAnimationView.setLayoutParams(new LinearLayout.LayoutParams(iconParams.width, iconParams.height));
        weatherAnimationView.setVisibility(View.GONE);
        parent.addView(weatherAnimationView, iconIndex + 1);
    }

    private void renderHourlyForecasts(HomeWeatherData data, String temperatureUnit) {
        binding.hourlyForecastContainer.removeAllViews();
        for (WeatherHourlyData hourlyData : data.getHourlyForecasts()) {
            TextView itemView = new TextView(requireContext());
            itemView.setText(hourlyData.getTimeText()
                    + "\n"
                    + hourlyData.getCondition()
                    + "\n"
                    + WeatherDisplayUtils.formatTemperature(hourlyData.getTemperature(), temperatureUnit));
            itemView.setTextColor(requireContext().getColor(com.litroenade.yunjiweather.R.color.weather_text_primary));
            itemView.setTextSize(13f);
            itemView.setGravity(android.view.Gravity.CENTER);
            itemView.setMinWidth(130);
            itemView.setPadding(12, 8, 12, 8);
            binding.hourlyForecastContainer.addView(itemView);
        }
    }

    private void renderDailyForecasts(HomeWeatherData data, String temperatureUnit) {
        binding.dailyForecastContainer.removeAllViews();
        for (WeatherDailyData dailyData : data.getDailyForecasts()) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(0, 10, 0, 10);

            TextView dateText = createDailyText(dailyData.getDateText(), 1f, android.view.Gravity.START);
            TextView conditionText = createDailyText(dailyData.getCondition(), 1f, android.view.Gravity.CENTER);
            TextView tempText = createDailyText(
                    WeatherDisplayUtils.formatTemperature(dailyData.getTempMin(), temperatureUnit)
                            + " / "
                            + WeatherDisplayUtils.formatTemperature(dailyData.getTempMax(), temperatureUnit),
                    1f,
                    android.view.Gravity.END
            );
            row.addView(dateText);
            row.addView(conditionText);
            row.addView(tempText);
            binding.dailyForecastContainer.addView(row);
        }
    }

    private TextView createDailyText(String text, float weight, int gravity) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextColor(requireContext().getColor(com.litroenade.yunjiweather.R.color.weather_text_secondary));
        textView.setTextSize(14f);
        textView.setGravity(gravity);
        textView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight));
        return textView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        weatherAnimationView = null;
        binding = null;
    }
}
