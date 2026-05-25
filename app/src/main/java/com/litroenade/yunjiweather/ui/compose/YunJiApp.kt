package com.litroenade.yunjiweather.ui.compose

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.litroenade.yunjiweather.ui.home.HomeViewModel
import com.litroenade.yunjiweather.ui.compose.screens.AlertScreen
import com.litroenade.yunjiweather.ui.compose.screens.CityScreen
import com.litroenade.yunjiweather.ui.compose.screens.HomeScreen
import com.litroenade.yunjiweather.ui.compose.screens.LifeIndexScreen
import com.litroenade.yunjiweather.ui.compose.screens.MineScreen
import com.litroenade.yunjiweather.ui.compose.theme.LocalYunJiVisualTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YunJiApp(
    modifier: Modifier = Modifier,
    animationEnabled: Boolean = true,
    temperatureUnit: String,
    windUnit: String
) {
    var activeSheet by rememberSaveable { mutableStateOf<WeatherSheet?>(null) }
    var noticeText by rememberSaveable { mutableStateOf("") }
    val visualTheme = LocalYunJiVisualTheme.current
    val homeViewModel: HomeViewModel = viewModel()

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
            temperatureUnit = temperatureUnit,
            windUnit = windUnit,
            viewModel = homeViewModel,
            onManageCities = { activeSheet = WeatherSheet.ManageCities },
            onSearchCity = { activeSheet = WeatherSheet.SearchCity },
            onSettings = { activeSheet = WeatherSheet.Settings },
            onDesktopWeather = { noticeText = "桌面天气需要系统小组件能力，当前先保留入口说明。" },
            onPersonalize = { activeSheet = WeatherSheet.Settings },
            onOpenAlerts = { activeSheet = WeatherSheet.Alerts },
            onOpenLifeIndex = { activeSheet = WeatherSheet.LifeIndex },
            onFeedbackWeather = { noticeText = "反馈当前天气需要后续接入反馈通道，当前天气数据不会被静默提交。" },
            onShareWeather = { noticeText = "分享入口已保留，后续接入系统分享面板。" }
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
                        respectStatusBar = false
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
                        autoFocusSearch = true
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

private enum class WeatherSheet {
    ManageCities,
    SearchCity,
    Settings,
    Alerts,
    LifeIndex
}
