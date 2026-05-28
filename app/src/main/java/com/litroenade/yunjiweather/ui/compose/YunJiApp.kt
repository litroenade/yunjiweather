package com.litroenade.yunjiweather.ui.compose

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.litroenade.yunjiweather.R
import com.litroenade.yunjiweather.ui.compose.screens.AlertScreen
import com.litroenade.yunjiweather.ui.compose.screens.CityScreen
import com.litroenade.yunjiweather.ui.compose.screens.DesktopWeatherScreen
import com.litroenade.yunjiweather.ui.compose.screens.HomeScreen
import com.litroenade.yunjiweather.ui.compose.screens.LifeIndexScreen
import com.litroenade.yunjiweather.ui.compose.screens.MineScreen
import com.litroenade.yunjiweather.ui.compose.screens.PersonalizationBlockEditor
import com.litroenade.yunjiweather.ui.compose.screens.PersonalizationScreen
import com.litroenade.yunjiweather.ui.compose.theme.LocalYunJiVisualTheme
import com.litroenade.yunjiweather.ui.home.HomeViewModel
import com.litroenade.yunjiweather.utils.HomeBlock
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
    onHomeBlockEnabledChange: (HomeBlock, Boolean) -> Unit = { _, _ -> },
    onMoveHomeBlockUp: (HomeBlock) -> Unit = {},
    onMoveHomeBlockDown: (HomeBlock) -> Unit = {},
    onResetHomeBlocks: () -> Unit = {},
    onDisplayedWeatherIconCodeChanged: (String?) -> Unit = {}
) {
    var activeTarget by rememberSaveable { mutableStateOf<WeatherNavigationTarget?>(null) }
    var showPersonalizationBlockEditor by rememberSaveable { mutableStateOf(false) }
    var noticeText by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val visualTheme = LocalYunJiVisualTheme.current
    val activePage = activeTarget?.takeIf { it.isFullPage }
    val activeSheet = activeTarget?.takeIf { it.isSheet }
    val closePageAndRefresh = {
        activeTarget = null
        homeViewModel.refresh()
    }

    LaunchedEffect(activePage) {
        if (activePage != null) {
            onDisplayedWeatherIconCodeChanged("")
        }
        if (activePage != WeatherNavigationTarget.PERSONALIZATION) {
            showPersonalizationBlockEditor = false
        }
    }

    BackHandler(enabled = activeTarget != null || showPersonalizationBlockEditor) {
        if (showPersonalizationBlockEditor) {
            showPersonalizationBlockEditor = false
        } else {
            closePageAndRefresh()
        }
    }

    if (activePage == null) {
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
                onManageCities = { activeTarget = WeatherNavigationTarget.MANAGE_CITIES },
                onSearchCity = { activeTarget = WeatherNavigationTarget.SEARCH_CITY },
                onSettings = { activeTarget = WeatherNavigationTarget.SETTINGS },
                onPersonalization = { activeTarget = WeatherNavigationTarget.PERSONALIZATION },
                onDesktopWeather = { activeTarget = WeatherNavigationTarget.DESKTOP_WEATHER },
                onOpenAlerts = { activeTarget = WeatherNavigationTarget.ALERTS },
                onOpenLifeIndex = { activeTarget = WeatherNavigationTarget.LIFE_INDEX }
            )
        }
    } else {
        WeatherPageScaffold(
            title = activePageTitle(activePage),
            subtitle = activePageSubtitle(activePage),
            onBack = closePageAndRefresh,
            modifier = modifier,
            action = if (activePage == WeatherNavigationTarget.PERSONALIZATION) {
                {
                    IconButton(onClick = { showPersonalizationBlockEditor = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings_24),
                            contentDescription = "调整首页模块",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            } else {
                null
            }
        ) { pageModifier ->
            when (activePage) {
                WeatherNavigationTarget.MANAGE_CITIES -> CityScreen(
                    modifier = pageModifier,
                    temperatureUnit = temperatureUnit,
                    respectStatusBar = false,
                    showHeader = false,
                    onDefaultCityChanged = homeViewModel::refresh
                )

                WeatherNavigationTarget.DESKTOP_WEATHER -> DesktopWeatherScreen(
                    modifier = pageModifier,
                    onRequestWidget = { noticeText = requestWeatherWidgetPin(context) }
                )

                WeatherNavigationTarget.PERSONALIZATION -> PersonalizationScreen(
                    modifier = pageModifier
                )

                WeatherNavigationTarget.SETTINGS -> MineScreen(
                    modifier = pageModifier,
                    respectStatusBar = false,
                    showHeader = false
                )

                WeatherNavigationTarget.ALERTS -> AlertScreen(
                    modifier = pageModifier,
                    respectStatusBar = false
                )

                WeatherNavigationTarget.LIFE_INDEX -> LifeIndexScreen(
                    modifier = pageModifier,
                    respectStatusBar = false
                )

                WeatherNavigationTarget.SEARCH_CITY -> Unit
            }
        }
    }

    when (activeSheet) {
        WeatherNavigationTarget.SEARCH_CITY -> {
            ModalBottomSheet(
                onDismissRequest = {
                    activeTarget = null
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
                            activeTarget = null
                            homeViewModel.refresh()
                        }
                    )
                }
            }
        }

        null -> Unit
        else -> Unit
    }

    if (showPersonalizationBlockEditor) {
        ModalBottomSheet(
            onDismissRequest = { showPersonalizationBlockEditor = false },
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ) {
            PersonalizationBlockEditor(
                homeBlockOrder = homeBlockOrder,
                homeBlockEnabled = homeBlockEnabled,
                onHomeBlockEnabledChange = onHomeBlockEnabledChange,
                onMoveHomeBlockUp = onMoveHomeBlockUp,
                onMoveHomeBlockDown = onMoveHomeBlockDown,
                onResetHomeBlocks = onResetHomeBlocks
            )
        }
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

private fun activePageTitle(target: WeatherNavigationTarget): String {
    return when (target) {
        WeatherNavigationTarget.MANAGE_CITIES -> "管理城市"
        WeatherNavigationTarget.DESKTOP_WEATHER -> "桌面天气"
        WeatherNavigationTarget.PERSONALIZATION -> "个性换肤"
        WeatherNavigationTarget.SETTINGS -> "设置"
        WeatherNavigationTarget.ALERTS -> "天气预警"
        WeatherNavigationTarget.LIFE_INDEX -> "生活指数"
        WeatherNavigationTarget.SEARCH_CITY -> "搜索城市"
    }
}

private fun activePageSubtitle(target: WeatherNavigationTarget): String? {
    return when (target) {
        WeatherNavigationTarget.MANAGE_CITIES -> "搜索、添加、切换默认城市"
        WeatherNavigationTarget.DESKTOP_WEATHER -> "管理系统桌面小组件"
        WeatherNavigationTarget.PERSONALIZATION -> "选择主题，右上角调整首页模块"
        WeatherNavigationTarget.SETTINGS -> "通知、单位和本地数据"
        WeatherNavigationTarget.ALERTS -> null
        WeatherNavigationTarget.LIFE_INDEX -> null
        WeatherNavigationTarget.SEARCH_CITY -> null
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
