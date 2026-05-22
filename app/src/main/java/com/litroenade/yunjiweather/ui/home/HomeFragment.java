package com.litroenade.yunjiweather.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.model.WeatherDailyData;
import com.litroenade.yunjiweather.databinding.FragmentHomeBinding;
import com.litroenade.yunjiweather.settings.SettingsManager;
import com.litroenade.yunjiweather.ui.calendar.CalendarDetailBottomSheet;
import com.litroenade.yunjiweather.utils.DateTimeUtils;
import com.litroenade.yunjiweather.utils.LunarCalendarUtils;
import com.litroenade.yunjiweather.utils.PermissionUtils;
import com.litroenade.yunjiweather.utils.VisualTheme;
import com.litroenade.yunjiweather.utils.VisualThemeUtils;
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils;
import com.litroenade.yunjiweather.utils.WeatherIconUtils;

import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private SettingsManager settingsManager;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private WeatherAnimationView weatherAnimationView;
    private HourlyForecastAdapter hourlyForecastAdapter;
    private DailyForecastAdapter dailyForecastAdapter;
    private WeatherCalendarAdapter weatherCalendarAdapter;

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
        applyIdleHomeTheme();
        setupForecastLists();
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

    private void setupForecastLists() {
        hourlyForecastAdapter = new HourlyForecastAdapter();
        binding.hourlyForecastRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        binding.hourlyForecastRecyclerView.setAdapter(hourlyForecastAdapter);
        binding.hourlyForecastRecyclerView.setNestedScrollingEnabled(false);

        weatherCalendarAdapter = new WeatherCalendarAdapter(this::showCalendarDayDetail);
        binding.weatherCalendarRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        binding.weatherCalendarRecyclerView.setAdapter(weatherCalendarAdapter);
        binding.weatherCalendarRecyclerView.setNestedScrollingEnabled(false);

        dailyForecastAdapter = new DailyForecastAdapter();
        binding.dailyForecastRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.dailyForecastRecyclerView.setAdapter(dailyForecastAdapter);
        binding.dailyForecastRecyclerView.setNestedScrollingEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (homeViewModel != null && homeViewModel.getUiState().getValue() != null) {
            renderState(homeViewModel.getUiState().getValue());
        } else if (binding != null && settingsManager != null) {
            applyIdleHomeTheme();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            CancellationSignal cancellationSignal = new CancellationSignal();
            locationManager.getCurrentLocation(
                    provider,
                    cancellationSignal,
                    ContextCompat.getMainExecutor(requireContext()),
                    this::handleLocationResult
            );
            return;
        }
        locationManager.requestSingleUpdate(provider, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                handleLocationResult(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
            }
        }, Looper.getMainLooper());
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
            String cacheText = String.format(
                    Locale.CHINA,
                    "%s %s",
                    state.getMessage(),
                    DateTimeUtils.formatCacheUpdateTime(state.getUpdateTime())
            );
            binding.cacheStateText.setText(cacheText);
            binding.cacheStateText.setVisibility(View.VISIBLE);
        }
    }

    private void renderWeatherData(HomeWeatherData data) {
        String temperatureUnit = settingsManager.getTemperatureUnit();
        String windUnit = settingsManager.getWindUnit();
        binding.contentContainer.setVisibility(View.VISIBLE);
        String themeKey = settingsManager.getVisualTheme();
        binding.homeRoot.setBackgroundResource(VisualThemeUtils.resolveHomeBackground(
                themeKey,
                data.getIconCode()
        ));
        applyHomeForeground(themeKey);
        binding.weatherIconImage.setImageResource(WeatherIconUtils.getWeatherIconRes(data.getIconCode()));
        renderWeatherIconAnimation(data.getIconCode());
        binding.cityNameText.setText(data.getCityName());
        binding.tempText.setText(WeatherDisplayUtils.formatTemperature(data.getTemperature(), temperatureUnit));
        binding.conditionText.setText(data.getCondition());
        binding.feelsLikeText.setText(String.format(
                Locale.CHINA,
                "体感 %s",
                WeatherDisplayUtils.formatTemperature(data.getFeelsLike(), temperatureUnit)
        ));
        binding.tempRangeText.setText(String.format(
                Locale.CHINA,
                "今日 %s / %s",
                WeatherDisplayUtils.formatTemperature(data.getTempMin(), temperatureUnit),
                WeatherDisplayUtils.formatTemperature(data.getTempMax(), temperatureUnit)
        ));
        binding.humidityText.setText(String.format(Locale.CHINA, "湿度 %s%%", data.getHumidity()));
        binding.windText.setText(WeatherDisplayUtils.formatWind(
                data.getWindDir(),
                data.getWindScale(),
                data.getWindSpeed(),
                windUnit
        ));
        binding.pressureText.setText(String.format(Locale.CHINA, "气压 %s hPa", data.getPressure()));
        binding.visibilityText.setText(String.format(Locale.CHINA, "能见度 %s km", data.getVisibility()));
        binding.airQualityText.setText(String.format(
                Locale.CHINA,
                "AQI %s · %s",
                data.getAirQualityIndex(),
                data.getAirQualityCategory()
        ));
        binding.airPrimaryText.setText(String.format(Locale.CHINA, "首要污染物：%s", data.getPrimaryPollutant()));
        binding.clothingAdviceText.setText(String.format(Locale.CHINA, "穿衣：%s", data.getClothingAdvice()));
        binding.travelAdviceText.setText(String.format(Locale.CHINA, "出行：%s", data.getTravelAdvice()));
        binding.updateTimeText.setText(DateTimeUtils.formatCacheUpdateTime(data.getUpdateTime()).replace("缓存", ""));
        renderHourlyForecasts(data, temperatureUnit);
        renderWeatherCalendar(data, temperatureUnit);
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
        weatherAnimationView.setLayoutParams(new ViewGroup.LayoutParams(iconParams.width, iconParams.height));
        weatherAnimationView.setVisibility(View.GONE);
        parent.addView(weatherAnimationView, iconIndex + 1);
    }

    private void renderHourlyForecasts(HomeWeatherData data, String temperatureUnit) {
        if (hourlyForecastAdapter != null) {
            hourlyForecastAdapter.submitData(data.getHourlyForecasts(), temperatureUnit);
        }
    }

    private void renderWeatherCalendar(HomeWeatherData data, String temperatureUnit) {
        if (weatherCalendarAdapter != null) {
            weatherCalendarAdapter.submitData(data.getDailyForecasts(), temperatureUnit);
        }
    }

    private void showCalendarDayDetail(
            WeatherDailyData item,
            LunarCalendarUtils.LunarDayInfo lunarInfo,
            String temperatureUnit
    ) {
        String temperatureText = String.format(
                Locale.CHINA,
                "%s / %s",
                WeatherDisplayUtils.formatTemperature(item.getTempMin(), temperatureUnit),
                WeatherDisplayUtils.formatTemperature(item.getTempMax(), temperatureUnit)
        );
        CalendarDetailBottomSheet.show(
                requireContext(),
                CalendarDetailBottomSheet.Detail.weatherCalendar(lunarInfo, item.getCondition(), temperatureText)
        );
    }

    private void renderDailyForecasts(HomeWeatherData data, String temperatureUnit) {
        if (dailyForecastAdapter != null) {
            dailyForecastAdapter.submitData(data.getDailyForecasts(), temperatureUnit);
        }
    }

    private void applyIdleHomeTheme() {
        String themeKey = settingsManager.getVisualTheme();
        VisualThemeUtils.applyAppBackground(binding.getRoot(), themeKey);
        applyHomeForeground(themeKey);
    }

    private void applyHomeForeground(String themeKey) {
        VisualTheme theme = VisualThemeUtils.resolveTheme(themeKey);
        int primaryColor = ContextCompat.getColor(requireContext(), theme.getHomePrimaryTextColorRes());
        int secondaryColor = ContextCompat.getColor(requireContext(), theme.getHomeSecondaryTextColorRes());
        ColorStateList primaryTint = ColorStateList.valueOf(primaryColor);

        binding.cityNameText.setTextColor(primaryColor);
        binding.locationButton.setTextColor(primaryTint);
        binding.locationButton.setStrokeColor(primaryTint);
        binding.refreshButton.setTextColor(primaryTint);
        binding.refreshButton.setStrokeColor(primaryTint);
        binding.cacheStateText.setTextColor(secondaryColor);
        binding.updateTimeText.setTextColor(secondaryColor);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        weatherAnimationView = null;
        hourlyForecastAdapter = null;
        weatherCalendarAdapter = null;
        dailyForecastAdapter = null;
        binding = null;
    }
}
