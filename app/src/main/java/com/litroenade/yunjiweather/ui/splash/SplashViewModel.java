package com.litroenade.yunjiweather.ui.splash;

import androidx.lifecycle.ViewModel;

import com.litroenade.yunjiweather.data.repository.SettingsRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public final class SplashViewModel extends ViewModel {

    private final boolean darkModeEnabled;
    private final String visualTheme;

    @Inject
    public SplashViewModel(SettingsRepository settingsRepository) {
        darkModeEnabled = settingsRepository.isDarkModeEnabled();
        visualTheme = settingsRepository.getVisualTheme();
    }

    public boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }

    public String getVisualTheme() {
        return visualTheme;
    }
}
