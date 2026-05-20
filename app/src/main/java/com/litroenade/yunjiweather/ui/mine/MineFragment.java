package com.litroenade.yunjiweather.ui.mine;

import android.os.Bundle;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.litroenade.yunjiweather.databinding.FragmentMineBinding;
import com.litroenade.yunjiweather.ui.auth.AuthActivity;
import com.litroenade.yunjiweather.utils.PermissionUtils;
import com.litroenade.yunjiweather.utils.ThemeModeUtils;
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils;
import com.google.android.material.button.MaterialButton;

public class MineFragment extends Fragment {

    private FragmentMineBinding binding;
    private MineViewModel viewModel;
    private boolean bindingSwitches;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(MineViewModel.class);
        binding = FragmentMineBinding.inflate(inflater, container, false);

        binding.warningSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!bindingSwitches) {
                if (isChecked) {
                    PermissionUtils.requestNotificationPermission(requireActivity());
                }
                viewModel.setWarningEnabled(isChecked);
            }
        });
        binding.dailyReminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!bindingSwitches) {
                if (isChecked) {
                    PermissionUtils.requestNotificationPermission(requireActivity());
                }
                viewModel.setDailyReminderEnabled(isChecked);
            }
        });
        binding.animationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!bindingSwitches) {
                viewModel.setAnimationEnabled(isChecked);
            }
        });
        binding.darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!bindingSwitches) {
                viewModel.setDarkModeEnabled(isChecked);
                applyDarkMode(isChecked);
            }
        });
        binding.celsiusButton.setOnClickListener(view ->
                viewModel.setTemperatureUnit(WeatherDisplayUtils.TEMPERATURE_CELSIUS)
        );
        binding.fahrenheitButton.setOnClickListener(view ->
                viewModel.setTemperatureUnit(WeatherDisplayUtils.TEMPERATURE_FAHRENHEIT)
        );
        binding.windScaleButton.setOnClickListener(view ->
                viewModel.setWindUnit(WeatherDisplayUtils.WIND_SCALE)
        );
        binding.windMsButton.setOnClickListener(view ->
                viewModel.setWindUnit(WeatherDisplayUtils.WIND_METER_PER_SECOND)
        );
        binding.clearCacheButton.setOnClickListener(view -> viewModel.clearCache());
        binding.logoutButton.setOnClickListener(view -> viewModel.logout());
        binding.aboutButton.setOnClickListener(view -> showAboutDialog());
        binding.dataSourceButton.setOnClickListener(view -> showDataSourceDialog());
        binding.helpButton.setOnClickListener(view -> showHelpDialog());

        viewModel.getAccountText().observe(getViewLifecycleOwner(), binding.accountText::setText);
        viewModel.getDefaultCity().observe(getViewLifecycleOwner(), binding.defaultCityText::setText);
        viewModel.getWarningEnabled().observe(getViewLifecycleOwner(), enabled -> setSwitchChecked(binding.warningSwitch, enabled));
        viewModel.getDailyReminderEnabled().observe(getViewLifecycleOwner(), enabled -> setSwitchChecked(binding.dailyReminderSwitch, enabled));
        viewModel.getAnimationEnabled().observe(getViewLifecycleOwner(), enabled -> setSwitchChecked(binding.animationSwitch, enabled));
        viewModel.getDarkModeEnabled().observe(getViewLifecycleOwner(), enabled -> setSwitchChecked(binding.darkModeSwitch, enabled));
        viewModel.getTemperatureUnit().observe(getViewLifecycleOwner(), this::renderTemperatureUnit);
        viewModel.getWindUnit().observe(getViewLifecycleOwner(), this::renderWindUnit);
        viewModel.getDataUpdateTime().observe(getViewLifecycleOwner(), binding.dataUpdateText::setText);
        viewModel.getLocalStorageSummary().observe(getViewLifecycleOwner(), binding.localStorageSummaryText::setText);
        viewModel.getMessage().observe(getViewLifecycleOwner(), message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        );
        viewModel.getLogoutEvent().observe(getViewLifecycleOwner(), shouldLogout -> {
            if (Boolean.TRUE.equals(shouldLogout)) {
                viewModel.consumeLogoutEvent();
                Intent intent = new Intent(requireContext(), AuthActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refresh();
    }

    private void setSwitchChecked(com.google.android.material.switchmaterial.SwitchMaterial switchView, boolean checked) {
        bindingSwitches = true;
        switchView.setChecked(checked);
        bindingSwitches = false;
    }

    private void renderTemperatureUnit(String unit) {
        setOptionChecked(binding.celsiusButton, WeatherDisplayUtils.TEMPERATURE_CELSIUS.equals(unit));
        setOptionChecked(binding.fahrenheitButton, WeatherDisplayUtils.TEMPERATURE_FAHRENHEIT.equals(unit));
    }

    private void renderWindUnit(String unit) {
        setOptionChecked(binding.windScaleButton, WeatherDisplayUtils.WIND_SCALE.equals(unit));
        setOptionChecked(binding.windMsButton, WeatherDisplayUtils.WIND_METER_PER_SECOND.equals(unit));
    }

    private void setOptionChecked(MaterialButton button, boolean checked) {
        button.setSelected(checked);
        button.setAlpha(checked ? 1.0f : 0.72f);
    }

    private void applyDarkMode(boolean enabled) {
        int targetMode = ThemeModeUtils.resolveNightMode(
                enabled,
                AppCompatDelegate.MODE_NIGHT_YES,
                AppCompatDelegate.MODE_NIGHT_NO
        );
        if (ThemeModeUtils.shouldApplyNightMode(AppCompatDelegate.getDefaultNightMode(), targetMode)) {
            AppCompatDelegate.setDefaultNightMode(targetMode);
        }
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("关于云迹天气")
                .setMessage("云迹天气是一个课程项目级天气生活服务 App，当前已接入首页天气、城市管理、生活指数、预警状态和基础设置。")
                .setPositiveButton("知道了", null)
                .show();
    }

    private void showDataSourceDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("数据来源")
                .setMessage("默认使用 Open-Meteo 获取天气、空气质量和城市搜索数据，无需 API Key。配置 QWeather 后，可增强生活指数和官方天气预警能力。请在 local.properties 中配置 qweather.apiHost 和 qweather.apiKey。")
                .setPositiveButton("知道了", null)
                .show();
    }

    private void showHelpDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("使用帮助")
                .setMessage("本项目账户为本地账户，不上传密码；不同账户的城市、缓存、设置和预警状态互相隔离。首页可下拉刷新天气，点击定位后才会申请定位权限。城市页可添加、删除和切换默认城市。网络失败时会优先显示当前账户的本地缓存。预警通知和每日提醒需要开启通知权限。未配置 QWeather 时，不会伪造官方天气预警。")
                .setPositiveButton("知道了", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
