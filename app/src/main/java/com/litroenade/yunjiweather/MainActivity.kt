package com.litroenade.yunjiweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.litroenade.yunjiweather.worker.DailyWeatherWorker
import com.litroenade.yunjiweather.worker.WeatherAlertWorker
import com.litroenade.yunjiweather.worker.WorkerScopeUtils
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

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
                    onHomeBlockEnabledChange = mineViewModel::setHomeBlockEnabled,
                    onMoveHomeBlockUp = mineViewModel::moveHomeBlockUp,
                    onMoveHomeBlockDown = mineViewModel::moveHomeBlockDown,
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
