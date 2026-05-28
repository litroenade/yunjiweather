package com.litroenade.yunjiweather

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
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
import com.litroenade.yunjiweather.notification.NotificationHelper
import com.litroenade.yunjiweather.settings.SettingsManager
import com.litroenade.yunjiweather.ui.compose.YunJiApp
import com.litroenade.yunjiweather.ui.compose.WeatherSceneSpec
import com.litroenade.yunjiweather.ui.compose.theme.YunJiTheme
import com.litroenade.yunjiweather.ui.home.HomeViewModel
import com.litroenade.yunjiweather.ui.mine.MineViewModel
import com.litroenade.yunjiweather.utils.HomeBlock
import com.litroenade.yunjiweather.utils.PermissionUtils
import com.litroenade.yunjiweather.worker.DailyWeatherWorker
import com.litroenade.yunjiweather.worker.WeatherAlertWorker
import com.litroenade.yunjiweather.worker.WorkerScopeUtils
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private var pendingLocationViewModel: HomeViewModel? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val viewModel = pendingLocationViewModel
        pendingLocationViewModel = null
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted && viewModel != null) {
            updateDefaultCityFromDeviceLocation(viewModel)
        } else {
            viewModel?.publishMessage("定位权限未开启")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createWarningChannel(this)
        NotificationHelper.createDailyReminderChannel(this)
        rebuildLegacyScopedWorkersIfNeeded()
        setContent {
            val mineViewModel: MineViewModel = viewModel()
            val homeViewModel: HomeViewModel = viewModel()
            val settingsManager = remember { SettingsManager(this) }
            val darkModeEnabled by mineViewModel.getDarkModeEnabled().observeAsState(
                settingsManager.isDarkModeEnabled()
            )
            val visualThemeKey by mineViewModel.getVisualTheme().observeAsState(
                settingsManager.getVisualTheme()
            )
            val animationEnabled by mineViewModel.getAnimationEnabled().observeAsState(
                settingsManager.isAnimationEnabled()
            )
            val temperatureUnit by mineViewModel.getTemperatureUnit().observeAsState(
                settingsManager.getTemperatureUnit()
            )
            val windUnit by mineViewModel.getWindUnit().observeAsState(
                settingsManager.getWindUnit()
            )
            val developerToolsEnabled by mineViewModel.getDeveloperToolsEnabled().observeAsState(
                settingsManager.isDeveloperToolsEnabled()
            )
            val homeBlockOrder by mineViewModel.getHomeBlockOrder().observeAsState(HomeBlock.defaultOrder())
            val homeBlockEnabled by mineViewModel.getHomeBlockEnabled().observeAsState(emptyMap())
            var displayedWeatherIconCode by remember { mutableStateOf<String?>(null) }
            val homeUiState by homeViewModel.getUiState().observeAsState()
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
                visualThemeKey = visualThemeKey
            ) {
                YunJiApp(
                    animationEnabled = animationEnabled,
                    developerToolsEnabled = developerToolsEnabled,
                    temperatureUnit = temperatureUnit,
                    windUnit = windUnit,
                    homeBlockOrder = homeBlockOrder,
                    homeBlockEnabled = homeBlockEnabled,
                    homeViewModel = homeViewModel,
                    onDisplayedWeatherIconCodeChanged = { iconCode ->
                        displayedWeatherIconCode = iconCode
                    },
                    onUseCurrentLocation = { requestCurrentLocation(homeViewModel) }
                )
            }
        }
    }

    private fun requestCurrentLocation(homeViewModel: HomeViewModel) {
        if (!PermissionUtils.hasLocationPermission(this)) {
            pendingLocationViewModel = homeViewModel
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }
        updateDefaultCityFromDeviceLocation(homeViewModel)
    }

    @SuppressLint("MissingPermission")
    private fun updateDefaultCityFromDeviceLocation(homeViewModel: HomeViewModel) {
        val locationManager = getSystemService(LocationManager::class.java)
        if (locationManager == null) {
            homeViewModel.publishMessage("系统定位服务不可用")
            return
        }
        val lastLocation = findLastKnownLocation(locationManager)
        if (lastLocation != null && isRecentLocation(lastLocation)) {
            homeViewModel.updateDefaultCityByLocation(lastLocation.latitude, lastLocation.longitude)
            return
        }

        val provider = findEnabledLocationProvider(locationManager)
        if (provider == null) {
            homeViewModel.publishMessage("系统定位服务未开启")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            locationManager.getCurrentLocation(provider, null, ContextCompat.getMainExecutor(this)) { location ->
                if (location == null) {
                    publishLocationFallback(homeViewModel, lastLocation)
                } else {
                    homeViewModel.updateDefaultCityByLocation(location.latitude, location.longitude)
                }
            }
        } else {
            requestSingleLocationUpdate(locationManager, provider, homeViewModel, lastLocation)
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    private fun requestSingleLocationUpdate(
        locationManager: LocationManager,
        provider: String,
        homeViewModel: HomeViewModel,
        fallbackLocation: Location?
    ) {
        val handler = android.os.Handler(Looper.getMainLooper())
        var delivered = false
        var listener: LocationListener? = null
        val timeout = Runnable {
            if (!delivered) {
                delivered = true
                listener?.let { runCatching { locationManager.removeUpdates(it) } }
                publishLocationFallback(homeViewModel, fallbackLocation)
            }
        }
        listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (delivered) {
                    return
                }
                delivered = true
                handler.removeCallbacks(timeout)
                homeViewModel.updateDefaultCityByLocation(location.latitude, location.longitude)
            }
        }
        handler.postDelayed(timeout, SINGLE_LOCATION_TIMEOUT_MILLIS)
        locationManager.requestSingleUpdate(
            provider,
            listener!!,
            Looper.getMainLooper()
        )
    }

    @SuppressLint("MissingPermission")
    private fun findLastKnownLocation(locationManager: LocationManager): Location? {
        return LOCATION_PROVIDERS
            .mapNotNull { provider ->
                runCatching {
                    if (locationManager.allProviders.contains(provider)) {
                        locationManager.getLastKnownLocation(provider)
                    } else {
                        null
                    }
                }.getOrNull()
            }
            .maxByOrNull { location -> location.time }
    }

    private fun isRecentLocation(location: Location): Boolean {
        val ageMillis = System.currentTimeMillis() - location.time
        return location.time > 0L && ageMillis >= 0L && ageMillis <= LAST_KNOWN_LOCATION_MAX_AGE_MILLIS
    }

    private fun publishLocationFallback(homeViewModel: HomeViewModel, fallbackLocation: Location?) {
        if (fallbackLocation == null) {
            homeViewModel.publishMessage("暂时无法获取当前位置")
            return
        }
        homeViewModel.publishMessage("使用上次定位结果")
        homeViewModel.updateDefaultCityByLocation(fallbackLocation.latitude, fallbackLocation.longitude)
    }

    private fun findEnabledLocationProvider(locationManager: LocationManager): String? {
        return LOCATION_PROVIDERS.firstOrNull { provider ->
            runCatching {
                locationManager.allProviders.contains(provider) && locationManager.isProviderEnabled(provider)
            }.getOrDefault(false)
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
        private const val LAST_KNOWN_LOCATION_MAX_AGE_MILLIS = 10L * 60L * 1000L
        private const val SINGLE_LOCATION_TIMEOUT_MILLIS = 8L * 1000L
        private val LOCATION_PROVIDERS = listOf(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
    }
}
