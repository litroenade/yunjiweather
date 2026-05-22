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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.litroenade.yunjiweather.R;
import com.litroenade.yunjiweather.databinding.FragmentMineBinding;
import com.litroenade.yunjiweather.ui.auth.AuthActivity;
import com.litroenade.yunjiweather.utils.PermissionUtils;
import com.litroenade.yunjiweather.utils.VisualThemeUtils;
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils;
import com.google.android.material.button.MaterialButton;

public class MineFragment extends Fragment {

    private FragmentMineBinding binding;
    private MineViewModel viewModel;
    private VisualThemeAdapter visualThemeAdapter;
    private boolean bindingSwitches;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(MineViewModel.class);
        binding = FragmentMineBinding.inflate(inflater, container, false);
        VisualThemeUtils.applyAppBackground(binding.getRoot(), viewModel.getCurrentVisualTheme());
        setupVisualThemeList();

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
        binding.developerInfoButton.setOnClickListener(view -> showDeveloperDialog());
        binding.helpButton.setOnClickListener(view -> showHelpDialog());

        viewModel.getAccountText().observe(getViewLifecycleOwner(), binding.accountText::setText);
        viewModel.getDefaultCity().observe(getViewLifecycleOwner(), binding.defaultCityText::setText);
        viewModel.getWarningEnabled().observe(getViewLifecycleOwner(), enabled -> setSwitchChecked(binding.warningSwitch, enabled));
        viewModel.getDailyReminderEnabled().observe(getViewLifecycleOwner(), enabled -> setSwitchChecked(binding.dailyReminderSwitch, enabled));
        viewModel.getAnimationEnabled().observe(getViewLifecycleOwner(), enabled -> setSwitchChecked(binding.animationSwitch, enabled));
        viewModel.getDarkModeEnabled().observe(getViewLifecycleOwner(), enabled -> setSwitchChecked(binding.darkModeSwitch, enabled));
        viewModel.getTemperatureUnit().observe(getViewLifecycleOwner(), this::renderTemperatureUnit);
        viewModel.getWindUnit().observe(getViewLifecycleOwner(), this::renderWindUnit);
        viewModel.getVisualTheme().observe(getViewLifecycleOwner(), this::renderVisualTheme);
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

    private void renderVisualTheme(String themeKey) {
        if (visualThemeAdapter != null) {
            visualThemeAdapter.setSelectedThemeKey(themeKey);
        }
        VisualThemeUtils.applyAppBackground(binding.getRoot(), themeKey);
    }

    private void setupVisualThemeList() {
        visualThemeAdapter = new VisualThemeAdapter(theme -> viewModel.setVisualTheme(theme.getKey()));
        binding.themeRecyclerView.setLayoutManager(new LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));
        binding.themeRecyclerView.setAdapter(visualThemeAdapter);
        binding.themeRecyclerView.setNestedScrollingEnabled(false);
        visualThemeAdapter.submitData(viewModel.getVisualThemes());
    }

    private void setOptionChecked(MaterialButton button, boolean checked) {
        button.setSelected(checked);
        button.setAlpha(checked ? 1.0f : 0.72f);
    }

    private void applyDarkMode(boolean enabled) {
        int targetMode = enabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode);
        }
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.mine_about_title)
                .setMessage(R.string.mine_about_message)
                .setPositiveButton(R.string.mine_dialog_positive, null)
                .show();
    }

    private void showDataSourceDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.mine_data_source_title)
                .setMessage(R.string.mine_data_source_message)
                .setPositiveButton(R.string.mine_dialog_positive, null)
                .show();
    }

    private void showDeveloperDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.mine_developer_title)
                .setMessage(R.string.mine_developer_message)
                .setPositiveButton(R.string.mine_dialog_positive, null)
                .show();
    }

    private void showHelpDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.mine_help_title)
                .setMessage(R.string.mine_help_message)
                .setPositiveButton(R.string.mine_dialog_positive, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        visualThemeAdapter = null;
        binding = null;
    }
}
