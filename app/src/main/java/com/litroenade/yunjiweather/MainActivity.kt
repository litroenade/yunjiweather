package com.litroenade.yunjiweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.litroenade.yunjiweather.data.model.CustomThemeCropAnchor
import com.litroenade.yunjiweather.data.model.CustomThemeProfile
import com.litroenade.yunjiweather.notification.NotificationHelper
import com.litroenade.yunjiweather.ui.compose.YunJiApp
import com.litroenade.yunjiweather.ui.compose.WeatherSceneSpec
import com.litroenade.yunjiweather.ui.compose.home.modules.HomeModuleCatalog
import com.litroenade.yunjiweather.ui.compose.theme.YunJiTheme
import com.litroenade.yunjiweather.ui.home.HomeViewModel
import com.litroenade.yunjiweather.ui.location.AndroidLocationClient
import com.litroenade.yunjiweather.ui.location.LocationPermissionResult
import com.litroenade.yunjiweather.ui.location.LocationUiState
import com.litroenade.yunjiweather.ui.mine.MineViewModel
import com.litroenade.yunjiweather.utils.VisualThemeUtils
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils
import com.litroenade.yunjiweather.widget.WeatherWidgetRefreshScheduler
import com.litroenade.yunjiweather.worker.DailyWeatherWorker
import com.litroenade.yunjiweather.worker.WeatherAlertWorker
import com.litroenade.yunjiweather.worker.WorkerScopeUtils
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createWarningChannel(this)
        NotificationHelper.createDailyReminderChannel(this)
        rebuildLegacyScopedWorkersIfNeeded()
        setContent {
            val mineViewModel: MineViewModel = viewModel()
            val homeViewModel: HomeViewModel = viewModel()
            val locationClient = remember { AndroidLocationClient(this@MainActivity) }
            var locationUiState by remember { mutableStateOf(LocationUiState.idle()) }
            fun fetchDeviceLocation() {
                locationUiState = LocationUiState.fetchingLocation()
                locationClient.requestCurrentLocation(
                    onSuccess = { latitude, longitude ->
                        homeViewModel.updateDefaultCityByLocation(latitude, longitude)
                        locationUiState = LocationUiState.success("已获取系统定位，正在切换默认城市。")
                    },
                    onError = { message ->
                        locationUiState = LocationUiState.error(message)
                    }
                )
            }
            val locationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { grants ->
                if (LocationPermissionResult.hasUsablePermission(grants)) {
                    fetchDeviceLocation()
                } else {
                    locationUiState = LocationUiState.denied()
                }
            }
            val requestDeviceLocation = {
                if (LocationPermissionResult.hasUsablePermission(this@MainActivity)) {
                    fetchDeviceLocation()
                } else {
                    locationUiState = LocationUiState.requestingPermission()
                    locationPermissionLauncher.launch(LocationPermissionResult.RUNTIME_PERMISSIONS)
                }
            }
            val darkModeEnabled by mineViewModel.darkModeEnabled.observeAsState(
                false
            )
            val visualThemeKey by mineViewModel.visualTheme.observeAsState(
                VisualThemeUtils.THEME_SKY
            )
            val customThemeImageUri by mineViewModel.customThemeImageUri.observeAsState(
                ""
            )
            val customThemeCropAnchor by mineViewModel.customThemeCropAnchor.observeAsState(
                CustomThemeCropAnchor.CENTER
            )
            val customThemeImageUris by mineViewModel.customThemeImageUris.observeAsState(emptyMap())
            val customThemeCropAnchors by mineViewModel.customThemeCropAnchors.observeAsState(emptyMap())
            val customThemeProfile by mineViewModel.customThemeProfile.observeAsState(CustomThemeProfile.empty())
            val animationEnabled by mineViewModel.animationEnabled.observeAsState(
                true
            )
            val temperatureUnit by mineViewModel.temperatureUnit.observeAsState(
                WeatherDisplayUtils.TEMPERATURE_CELSIUS
            )
            val windUnit by mineViewModel.windUnit.observeAsState(
                WeatherDisplayUtils.WIND_SCALE
            )
            val developerToolsEnabled by mineViewModel.developerToolsEnabled.observeAsState(
                false
            )
            val homeModuleOrder by mineViewModel.homeModuleOrder.observeAsState(
                HomeModuleCatalog.getAvailableModules(visualThemeKey)
            )
            val homeModuleEnabled by mineViewModel.homeModuleEnabled.observeAsState(emptyMap())
            var displayedWeatherIconCode by remember { mutableStateOf<String?>(null) }
            val homeUiState by homeViewModel.uiState.observeAsState()
            val weatherIconCode = displayedWeatherIconCode ?: homeUiState?.data?.iconCode
            val useLightSystemBarIcons = remember(weatherIconCode, darkModeEnabled) {
                if (weatherIconCode.isNullOrBlank()) {
                    darkModeEnabled
                } else {
                    WeatherSceneSpec.fromIconCode(weatherIconCode).usesLightForeground()
                }
            }
            SideEffect {
                enableEdgeToEdge(
                    statusBarStyle = transparentSystemBarStyle(useLightSystemBarIcons),
                    navigationBarStyle = transparentSystemBarStyle(useLightSystemBarIcons)
                )
            }
            YunJiTheme(
                darkTheme = darkModeEnabled,
                visualThemeKey = visualThemeKey,
                customThemeImageUri = customThemeImageUri,
                customThemeCropAnchor = customThemeCropAnchor,
                customThemeImageUris = customThemeImageUris,
                customThemeCropAnchors = customThemeCropAnchors,
                customThemeProfile = customThemeProfile
            ) {
                YunJiApp(
                    animationEnabled = animationEnabled,
                    developerToolsEnabled = developerToolsEnabled,
                    temperatureUnit = temperatureUnit,
                    windUnit = windUnit,
                    homeModules = homeModuleOrder,
                    homeModuleEnabled = homeModuleEnabled,
                    homeViewModel = homeViewModel,
                    locationUiState = locationUiState,
                    onRequestLocation = requestDeviceLocation,
                    onHomeModuleEnabledChange = mineViewModel::setHomeModuleEnabled,
                    onMoveHomeModuleUp = mineViewModel::moveHomeModuleUp,
                    onMoveHomeModuleDown = mineViewModel::moveHomeModuleDown,
                    onResetHomeBlocks = mineViewModel::resetHomeBlockLayout,
                    onDisplayedWeatherIconCodeChanged = { iconCode ->
                        displayedWeatherIconCode = iconCode
                    }
                )
            }
        }
    }

    private fun rebuildLegacyScopedWorkersIfNeeded() {
        val preferences = getSharedPreferences(WORK_BOOTSTRAP_PREFS, MODE_PRIVATE)
        val workManager = WorkManager.getInstance(this)
        if (preferences.getBoolean(KEY_SINGLE_SCOPE_WORK_REBUILT, false)) {
            scheduleWorkers(workManager)
            return
        }
        workManager.cancelAllWork().result.addListener(
            {
                preferences.edit().putBoolean(KEY_SINGLE_SCOPE_WORK_REBUILT, true).apply()
                scheduleWorkers(workManager)
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun scheduleWorkers(workManager: WorkManager = WorkManager.getInstance(this)) {
        scheduleWeatherAlertWorker(workManager)
        scheduleDailyWeatherWorker(workManager)
        WeatherWidgetRefreshScheduler.sync(this)
    }

    private fun scheduleWeatherAlertWorker(workManager: WorkManager) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequest.Builder(
            WeatherAlertWorker::class.java,
            6,
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniquePeriodicWork(
            WorkerScopeUtils.weatherAlertWorkName(),
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun scheduleDailyWeatherWorker(workManager: WorkManager) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequest.Builder(
            DailyWeatherWorker::class.java,
            24,
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniquePeriodicWork(
            WorkerScopeUtils.dailyWeatherWorkName(),
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun transparentSystemBarStyle(useLightIcons: Boolean): SystemBarStyle {
        return if (useLightIcons) {
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        }
    }

    private companion object {
        private const val WORK_BOOTSTRAP_PREFS = "yunji_weather_work_bootstrap"
        private const val KEY_SINGLE_SCOPE_WORK_REBUILT = "single_scope_work_rebuilt"
    }
}
