package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.utils.HomeBlock;

import java.util.List;
import java.util.Map;

public interface SettingsRepository {

    boolean isWarningEnabled();

    void setWarningEnabled(boolean enabled);

    boolean isAnimationEnabled();

    void setAnimationEnabled(boolean enabled);

    boolean isDarkModeEnabled();

    void setDarkModeEnabled(boolean enabled);

    boolean isDeveloperToolsEnabled();

    void setDeveloperToolsEnabled(boolean enabled);

    String getTemperatureUnit();

    void setTemperatureUnit(String unit);

    String getWindUnit();

    void setWindUnit(String unit);

    boolean isDailyReminderEnabled();

    void setDailyReminderEnabled(boolean enabled);

    String getVisualTheme();

    void setVisualTheme(String themeKey);

    String getCustomThemeImageUri();

    void setCustomThemeImageUri(String imageUri);

    String getCustomThemeCropAnchor();

    void setCustomThemeCropAnchor(String cropAnchor);

    Map<String, String> getCustomThemeImageUris();

    Map<String, String> getCustomThemeCropAnchors();

    void setCustomThemeImage(String weatherKey, String imageUri, String cropAnchor);

    void clearCustomThemeImage(String weatherKey);

    void clearCustomThemeImages();

    List<HomeBlock> getHomeBlockOrder(String themeKey);

    boolean isHomeBlockEnabled(String themeKey, HomeBlock block);

    void setHomeBlockEnabled(String themeKey, HomeBlock block, boolean enabled);

    void moveHomeBlock(String themeKey, HomeBlock block, int direction);

    List<String> getHomeModuleOrder(String themeKey, List<String> availableModuleKeys);

    boolean isHomeModuleEnabled(String themeKey, String moduleKey);

    void setHomeModuleEnabled(String themeKey, String moduleKey, boolean enabled);

    void moveHomeModule(String themeKey, String moduleKey, int direction, List<String> availableModuleKeys);

    void resetHomeBlockLayout(String themeKey);
}
