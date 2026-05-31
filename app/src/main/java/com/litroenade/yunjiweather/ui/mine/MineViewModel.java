package com.litroenade.yunjiweather.ui.mine;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.data.model.CustomThemeCropAnchor;
import com.litroenade.yunjiweather.data.model.CustomThemeProfile;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.SettingsRepository;
import com.litroenade.yunjiweather.ui.compose.home.modules.HomeModuleCatalog;
import com.litroenade.yunjiweather.ui.compose.home.modules.HomeModuleDefinition;
import com.litroenade.yunjiweather.utils.DefaultCityUtils;
import com.litroenade.yunjiweather.utils.LocalStorageSummaryUtils;
import com.litroenade.yunjiweather.utils.MineCacheStatusUtils;
import com.litroenade.yunjiweather.utils.VisualTheme;
import com.litroenade.yunjiweather.utils.VisualThemeCatalog;
import com.litroenade.yunjiweather.utils.VisualThemeUtils;
import com.litroenade.yunjiweather.widget.WeatherAppWidgetProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MineViewModel extends AndroidViewModel {

    private final SettingsRepository settingsRepository;
    private final AppDatabase database;
    private final CityRepository cityRepository;
    private final ExecutorService diskExecutor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<String> localSpaceText = new MutableLiveData<>();
    private final MutableLiveData<String> defaultCity = new MutableLiveData<>();
    private final MutableLiveData<Boolean> warningEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> animationEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> darkModeEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> developerToolsEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> temperatureUnit = new MutableLiveData<>();
    private final MutableLiveData<String> windUnit = new MutableLiveData<>();
    private final MutableLiveData<Boolean> dailyReminderEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> visualTheme = new MutableLiveData<>();
    private final MutableLiveData<String> customThemeImageUri = new MutableLiveData<>();
    private final MutableLiveData<String> customThemeCropAnchor = new MutableLiveData<>();
    private final MutableLiveData<Map<String, String>> customThemeImageUris = new MutableLiveData<>();
    private final MutableLiveData<Map<String, String>> customThemeCropAnchors = new MutableLiveData<>();
    private final MutableLiveData<CustomThemeProfile> customThemeProfile = new MutableLiveData<>();
    private final MutableLiveData<List<HomeModuleDefinition>> homeModuleOrder = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Boolean>> homeModuleEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> dataUpdateTime = new MutableLiveData<>();
    private final MutableLiveData<String> localStorageSummary = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    @Inject
    public MineViewModel(
            @NonNull Application application,
            SettingsRepository settingsRepository,
            AppDatabase database,
            CityRepository cityRepository
    ) {
        super(application);
        this.settingsRepository = settingsRepository;
        this.database = database;
        this.cityRepository = cityRepository;
        refresh();
    }

    public LiveData<String> getLocalSpaceText() {
        return localSpaceText;
    }

    public LiveData<String> getDefaultCity() {
        return defaultCity;
    }

    public LiveData<Boolean> getWarningEnabled() {
        return warningEnabled;
    }

    public LiveData<Boolean> getAnimationEnabled() {
        return animationEnabled;
    }

    public LiveData<Boolean> getDarkModeEnabled() {
        return darkModeEnabled;
    }

    public LiveData<Boolean> getDeveloperToolsEnabled() {
        return developerToolsEnabled;
    }

    public LiveData<String> getTemperatureUnit() {
        return temperatureUnit;
    }

    public LiveData<String> getWindUnit() {
        return windUnit;
    }

    public LiveData<Boolean> getDailyReminderEnabled() {
        return dailyReminderEnabled;
    }

    public LiveData<String> getVisualTheme() {
        return visualTheme;
    }

    public LiveData<String> getCustomThemeImageUri() {
        return customThemeImageUri;
    }

    public LiveData<String> getCustomThemeCropAnchor() {
        return customThemeCropAnchor;
    }

    public LiveData<Map<String, String>> getCustomThemeImageUris() {
        return customThemeImageUris;
    }

    public LiveData<Map<String, String>> getCustomThemeCropAnchors() {
        return customThemeCropAnchors;
    }

    public LiveData<CustomThemeProfile> getCustomThemeProfile() {
        return customThemeProfile;
    }

    public String getCurrentVisualTheme() {
        return settingsRepository.getVisualTheme();
    }

    public List<VisualTheme> getVisualThemes() {
        return VisualThemeCatalog.getThemes();
    }

    public LiveData<List<HomeModuleDefinition>> getHomeModuleOrder() {
        return homeModuleOrder;
    }

    public LiveData<Map<String, Boolean>> getHomeModuleEnabled() {
        return homeModuleEnabled;
    }

    public LiveData<String> getDataUpdateTime() {
        return dataUpdateTime;
    }

    public LiveData<String> getLocalStorageSummary() {
        return localStorageSummary;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void refresh() {
        refreshLocalSpaceText();
        reloadSettings();
        refreshDefaultCity();
        refreshDataUpdateTime();
        refreshLocalStorageSummary();
    }

    public void setWarningEnabled(boolean enabled) {
        settingsRepository.setWarningEnabled(enabled);
        reloadSettings();
        message.setValue(enabled ? "天气预警通知已开启" : "天气预警通知已关闭");
    }

    public void setAnimationEnabled(boolean enabled) {
        settingsRepository.setAnimationEnabled(enabled);
        reloadSettings();
        message.setValue(enabled ? "天气动画已开启" : "天气动画已关闭");
    }

    public void setDarkModeEnabled(boolean enabled) {
        settingsRepository.setDarkModeEnabled(enabled);
        reloadSettings();
        message.setValue(enabled ? "深色模式已开启" : "深色模式已关闭");
    }

    public void setDeveloperToolsEnabled(boolean enabled) {
        settingsRepository.setDeveloperToolsEnabled(enabled);
        reloadSettings();
        message.setValue(enabled ? "开发者工具已允许" : "开发者工具已关闭");
    }

    public void setTemperatureUnit(String unit) {
        settingsRepository.setTemperatureUnit(unit);
        reloadSettings();
        message.setValue("温度单位已更新");
    }

    public void setWindUnit(String unit) {
        settingsRepository.setWindUnit(unit);
        reloadSettings();
        message.setValue("风速单位已更新");
    }

    public void setDailyReminderEnabled(boolean enabled) {
        settingsRepository.setDailyReminderEnabled(enabled);
        reloadSettings();
        message.setValue(enabled ? "每日提醒已开启" : "每日提醒已关闭");
    }

    public void setVisualTheme(String themeKey) {
        settingsRepository.setVisualTheme(themeKey);
        reloadSettings();
        refreshWeatherWidgets();
        VisualTheme theme = VisualThemeCatalog.getThemeOrDefault(themeKey);
        message.setValue("主题/个性化已应用：" + theme.getDisplayName());
    }

    public void setCustomThemeImageUri(String imageUri) {
        settingsRepository.setCustomThemeImageUri(imageUri);
        settingsRepository.setVisualTheme(VisualThemeUtils.THEME_CUSTOM_1);
        reloadSettings();
        refreshWeatherWidgets();
        message.setValue("自定义主题图片已更新");
    }

    public void setCustomThemeCropAnchor(String cropAnchor) {
        settingsRepository.setCustomThemeCropAnchor(cropAnchor);
        reloadSettings();
        refreshWeatherWidgets();
        message.setValue("自定义主题裁剪位置已更新");
    }

    public void setCustomThemeImage(String weatherKey, String imageUri, String cropAnchor) {
        settingsRepository.setCustomThemeImage(weatherKey, imageUri, cropAnchor);
        settingsRepository.setVisualTheme(VisualThemeUtils.THEME_CUSTOM_1);
        reloadSettings();
        refreshWeatherWidgets();
        message.setValue("自定义主题图片已保存");
    }

    public void setCustomThemeImages(Map<String, String> imageUris, Map<String, String> cropAnchors) {
        if (imageUris == null || imageUris.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : imageUris.entrySet()) {
            String cropAnchor = CustomThemeCropAnchor.CENTER;
            if (cropAnchors != null && cropAnchors.get(entry.getKey()) != null) {
                cropAnchor = cropAnchors.get(entry.getKey());
            }
            settingsRepository.setCustomThemeImage(
                    entry.getKey(),
                    entry.getValue(),
                    cropAnchor
            );
        }
        settingsRepository.setVisualTheme(VisualThemeUtils.THEME_CUSTOM_1);
        reloadSettings();
        refreshWeatherWidgets();
        message.setValue("自定义主题已保存并应用");
    }

    public void setCustomThemeProfile(CustomThemeProfile profile) {
        settingsRepository.setCustomThemeProfile(profile);
        settingsRepository.setVisualTheme(VisualThemeUtils.THEME_CUSTOM_1);
        reloadSettings();
        refreshWeatherWidgets();
        message.setValue("自定义主题包已保存并应用");
    }

    public void clearCustomThemeImage() {
        settingsRepository.clearCustomThemeImages();
        if (VisualThemeUtils.THEME_CUSTOM_1.equals(settingsRepository.getVisualTheme())) {
            settingsRepository.setVisualTheme(VisualThemeUtils.THEME_SKY);
        }
        reloadSettings();
        refreshWeatherWidgets();
        message.setValue("自定义主题图片已移除");
    }

    public void clearCustomThemeImage(String weatherKey) {
        settingsRepository.clearCustomThemeImage(weatherKey);
        if (settingsRepository.getCustomThemeProfile().getAssets().isEmpty()
                && VisualThemeUtils.THEME_CUSTOM_1.equals(settingsRepository.getVisualTheme())) {
            settingsRepository.setVisualTheme(VisualThemeUtils.THEME_SKY);
        }
        reloadSettings();
        refreshWeatherWidgets();
        message.setValue("自定义主题场景素材已移除");
    }

    public void setHomeModuleEnabled(HomeModuleDefinition module, boolean enabled) {
        settingsRepository.setHomeModuleEnabled(settingsRepository.getVisualTheme(), module.getKey(), enabled);
        reloadHomeBlockLayout();
        message.setValue(enabled ? "首页模块已显示：" + module.getDisplayName() : "首页模块已隐藏：" + module.getDisplayName());
    }

    public void moveHomeModuleUp(HomeModuleDefinition module) {
        String themeKey = settingsRepository.getVisualTheme();
        settingsRepository.moveHomeModule(themeKey, module.getKey(), -1, HomeModuleCatalog.getAvailableModuleKeys(themeKey));
        reloadHomeBlockLayout();
        message.setValue("首页模块已上移：" + module.getDisplayName());
    }

    public void moveHomeModuleDown(HomeModuleDefinition module) {
        String themeKey = settingsRepository.getVisualTheme();
        settingsRepository.moveHomeModule(themeKey, module.getKey(), 1, HomeModuleCatalog.getAvailableModuleKeys(themeKey));
        reloadHomeBlockLayout();
        message.setValue("首页模块已下移：" + module.getDisplayName());
    }

    public void moveHomeModuleTo(HomeModuleDefinition module, int targetIndex) {
        String themeKey = settingsRepository.getVisualTheme();
        settingsRepository.moveHomeModuleTo(themeKey, module.getKey(), targetIndex, HomeModuleCatalog.getAvailableModuleKeys(themeKey));
        reloadHomeBlockLayout();
        message.setValue("首页模块顺序已调整：" + module.getDisplayName());
    }

    public void resetHomeBlockLayout() {
        settingsRepository.resetHomeBlockLayout(settingsRepository.getVisualTheme());
        reloadHomeBlockLayout();
        message.setValue("首页模块布局已恢复默认");
    }

    public void clearCache() {
        diskExecutor.execute(() -> {
            database.weatherCacheDao().clearAll();
            message.postValue("天气缓存已清理");
            dataUpdateTime.postValue(MineCacheStatusUtils.formatDataUpdateTime(null));
            refreshLocalStorageSummary();
        });
    }

    private void refreshLocalSpaceText() {
        localSpaceText.setValue("本机天气空间");
    }

    private void reloadSettings() {
        warningEnabled.setValue(settingsRepository.isWarningEnabled());
        animationEnabled.setValue(settingsRepository.isAnimationEnabled());
        darkModeEnabled.setValue(settingsRepository.isDarkModeEnabled());
        developerToolsEnabled.setValue(settingsRepository.isDeveloperToolsEnabled());
        temperatureUnit.setValue(settingsRepository.getTemperatureUnit());
        windUnit.setValue(settingsRepository.getWindUnit());
        dailyReminderEnabled.setValue(settingsRepository.isDailyReminderEnabled());
        String themeKey = settingsRepository.getVisualTheme();
        visualTheme.setValue(themeKey);
        customThemeImageUri.setValue(settingsRepository.getCustomThemeImageUri());
        customThemeCropAnchor.setValue(settingsRepository.getCustomThemeCropAnchor());
        customThemeImageUris.setValue(settingsRepository.getCustomThemeImageUris());
        customThemeCropAnchors.setValue(settingsRepository.getCustomThemeCropAnchors());
        customThemeProfile.setValue(settingsRepository.getCustomThemeProfile());
        reloadHomeBlockLayout();
    }

    private void refreshWeatherWidgets() {
        WeatherAppWidgetProvider.updateAll(getApplication());
    }

    private void reloadHomeBlockLayout() {
        String themeKey = settingsRepository.getVisualTheme();
        List<HomeModuleDefinition> availableModules = HomeModuleCatalog.getAvailableModules(themeKey);
        List<String> availableKeys = HomeModuleCatalog.getAvailableModuleKeys(themeKey);
        List<String> orderedKeys = settingsRepository.getHomeModuleOrder(themeKey, availableKeys);
        Map<String, HomeModuleDefinition> moduleByKey = new HashMap<>();
        for (HomeModuleDefinition module : availableModules) {
            moduleByKey.put(module.getKey(), module);
        }
        List<HomeModuleDefinition> orderedModules = new ArrayList<>();
        Map<String, Boolean> enabled = new HashMap<>();
        for (String moduleKey : orderedKeys) {
            HomeModuleDefinition module = moduleByKey.get(moduleKey);
            if (module != null) {
                orderedModules.add(module);
                enabled.put(moduleKey, settingsRepository.isHomeModuleEnabled(themeKey, moduleKey));
            }
        }
        homeModuleOrder.setValue(orderedModules);
        homeModuleEnabled.setValue(enabled);
    }

    private void refreshDefaultCity() {
        diskExecutor.execute(() -> {
            CityEntity city = cityRepository.resolveDefaultCity(System.currentTimeMillis());
            defaultCity.postValue(DefaultCityUtils.formatDefaultCityText(city));
        });
    }

    private void refreshDataUpdateTime() {
        diskExecutor.execute(() -> {
            Long latestUpdateTime = database.weatherCacheDao().findLatestUpdateTime();
            dataUpdateTime.postValue(MineCacheStatusUtils.formatDataUpdateTime(latestUpdateTime));
        });
    }

    private void refreshLocalStorageSummary() {
        diskExecutor.execute(() -> {
            int cityCount = cityRepository.count();
            int cacheCount = database.weatherCacheDao().count();
            int warningCount = database.warningDao().count();
            localStorageSummary.postValue(LocalStorageSummaryUtils.formatSummary(
                    cityCount,
                    cacheCount,
                    warningCount
            ));
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        diskExecutor.shutdown();
    }
}
