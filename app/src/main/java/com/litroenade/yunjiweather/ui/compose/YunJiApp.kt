package com.litroenade.yunjiweather.ui.compose

import android.appwidget.AppWidgetManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.litroenade.yunjiweather.data.model.HomeWeatherData
import com.litroenade.yunjiweather.ui.home.HomeViewModel
import com.litroenade.yunjiweather.ui.compose.screens.AlertScreen
import com.litroenade.yunjiweather.ui.compose.screens.CityScreen
import com.litroenade.yunjiweather.ui.compose.screens.HomeScreen
import com.litroenade.yunjiweather.ui.compose.screens.LifeIndexScreen
import com.litroenade.yunjiweather.ui.compose.screens.MineScreen
import com.litroenade.yunjiweather.ui.compose.theme.LocalYunJiVisualTheme
import com.litroenade.yunjiweather.utils.HomeBlock
import com.litroenade.yunjiweather.utils.WeatherShareUtils
import com.litroenade.yunjiweather.widget.WeatherAppWidgetProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YunJiApp(
    modifier: Modifier = Modifier,
    animationEnabled: Boolean = true,
    developerToolsEnabled: Boolean = false,
    temperatureUnit: String,
    windUnit: String,
    homeBlockOrder: List<HomeBlock> = HomeBlock.defaultOrder(),
    homeBlockEnabled: Map<HomeBlock, Boolean> = emptyMap(),
    homeViewModel: HomeViewModel = viewModel(),
    onDisplayedWeatherIconCodeChanged: (String?) -> Unit = {},
    onUseCurrentLocation: () -> Unit = {}
) {
    var activeSheet by rememberSaveable { mutableStateOf<WeatherSheet?>(null) }
    var noticeText by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val visualTheme = LocalYunJiVisualTheme.current

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(visualTheme.background),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        HomeScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            animationEnabled = animationEnabled,
            developerToolsEnabled = developerToolsEnabled,
            temperatureUnit = temperatureUnit,
            windUnit = windUnit,
            homeBlockOrder = homeBlockOrder,
            homeBlockEnabled = homeBlockEnabled,
            viewModel = homeViewModel,
            onDisplayedWeatherIconCodeChanged = onDisplayedWeatherIconCodeChanged,
            onManageCities = { activeSheet = WeatherSheet.ManageCities },
            onSearchCity = { activeSheet = WeatherSheet.SearchCity },
            onUseCurrentLocation = onUseCurrentLocation,
            onSettings = { activeSheet = WeatherSheet.Settings },
            onDesktopWeather = { noticeText = requestWeatherWidgetPin(context) },
            onOpenAlerts = { activeSheet = WeatherSheet.Alerts },
            onOpenLifeIndex = { activeSheet = WeatherSheet.LifeIndex },
            onFeedbackWeather = { data ->
                val result = launchWeatherFeedback(context, data, temperatureUnit, windUnit)
                if (result.isNotBlank()) {
                    noticeText = result
                }
            },
            onShareWeather = { data ->
                val result = launchWeatherShare(context, data, temperatureUnit, windUnit)
                if (result.isNotBlank()) {
                    noticeText = result
                }
            }
        )
    }

    when (activeSheet) {
        WeatherSheet.ManageCities -> {
            ModalBottomSheet(
                onDismissRequest = {
                    activeSheet = null
                    homeViewModel.refresh()
                },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 420.dp)
                ) {
                    CityScreen(
                        modifier = Modifier.fillMaxWidth(),
                        temperatureUnit = temperatureUnit,
                        respectStatusBar = false,
                        onDefaultCityChanged = {
                            activeSheet = null
                            homeViewModel.refresh()
                        }
                    )
                }
            }
        }

        WeatherSheet.SearchCity -> {
            ModalBottomSheet(
                onDismissRequest = {
                    activeSheet = null
                    homeViewModel.refresh()
                },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 420.dp)
                ) {
                    CityScreen(
                        modifier = Modifier.fillMaxWidth(),
                        temperatureUnit = temperatureUnit,
                        respectStatusBar = false,
                        autoFocusSearch = true,
                        onDefaultCityChanged = {
                            activeSheet = null
                            homeViewModel.refresh()
                        }
                    )
                }
            }
        }

        WeatherSheet.Settings -> {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 420.dp)
                ) {
                    MineScreen(
                        modifier = Modifier.fillMaxWidth(),
                        respectStatusBar = false
                    )
                }
            }
        }

        WeatherSheet.Alerts -> {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 480.dp)
                ) {
                    AlertScreen(
                        modifier = Modifier.fillMaxWidth(),
                        respectStatusBar = false
                    )
                }
            }
        }

        WeatherSheet.LifeIndex -> {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 480.dp)
                ) {
                    LifeIndexScreen(
                        modifier = Modifier.fillMaxWidth(),
                        respectStatusBar = false
                    )
                }
            }
        }

        null -> Unit
    }

    if (noticeText.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { noticeText = "" },
            text = { Text(noticeText) },
            confirmButton = {
                TextButton(onClick = { noticeText = "" }) {
                    Text("知道了")
                }
            }
        )
    }
}

private fun launchWeatherShare(
    context: Context,
    weatherData: HomeWeatherData?,
    temperatureUnit: String,
    windUnit: String
): String {
    if (weatherData == null) {
        return "暂无可分享的天气数据。"
    }
    val shareText = WeatherShareUtils.buildShareText(weatherData, temperatureUnit, windUnit)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    return if (startChooser(context, intent, "分享天气")) {
        ""
    } else {
        "系统没有可用的分享应用。"
    }
}

private fun launchWeatherFeedback(
    context: Context,
    weatherData: HomeWeatherData?,
    temperatureUnit: String,
    windUnit: String
): String {
    if (weatherData == null) {
        return "暂无天气数据可反馈。"
    }
    val body = WeatherShareUtils.buildShareText(weatherData, temperatureUnit, windUnit) +
        "\n\n请补充你看到的实际天气、位置和问题描述："
    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
        setData(Uri.parse("mailto:"))
        putExtra(Intent.EXTRA_SUBJECT, "云迹天气反馈 - ${weatherData.cityName}")
        putExtra(Intent.EXTRA_TEXT, body)
    }
    if (startChooser(context, emailIntent, "反馈当前天气")) {
        return ""
    }
    val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "云迹天气反馈 - ${weatherData.cityName}")
        putExtra(Intent.EXTRA_TEXT, body)
    }
    return if (startChooser(context, fallbackIntent, "反馈当前天气")) {
        ""
    } else {
        "系统没有可用的反馈应用。"
    }
}

private fun requestWeatherWidgetPin(context: Context): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
        if (appWidgetManager?.isRequestPinAppWidgetSupported == true) {
            val provider = ComponentName(context, WeatherAppWidgetProvider::class.java)
            val requested = appWidgetManager.requestPinAppWidget(provider, null, null)
            if (requested) {
                return "已请求添加桌面天气，请在系统弹窗中确认。"
            }
        }
    }
    return "请长按桌面，在系统小组件列表中选择云迹天气。"
}

private fun startChooser(context: Context, intent: Intent, title: String): Boolean {
    return try {
        context.startActivity(
            Intent.createChooser(intent, title)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        true
    } catch (exception: ActivityNotFoundException) {
        false
    }
}

private enum class WeatherSheet {
    ManageCities,
    SearchCity,
    Settings,
    Alerts,
    LifeIndex
}
