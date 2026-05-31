package com.litroenade.yunjiweather.ui.compose

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
import com.litroenade.yunjiweather.ui.compose.theme.YunJiUiTokens
import com.litroenade.yunjiweather.ui.compose.home.modules.HomeModuleCatalog
import com.litroenade.yunjiweather.ui.compose.home.modules.HomeModuleDefinition
import com.litroenade.yunjiweather.ui.home.HomeViewModel
import com.litroenade.yunjiweather.ui.location.LocationUiState
import com.litroenade.yunjiweather.widget.WeatherAppWidgetProvider
import com.litroenade.yunjiweather.widget.WeatherWidgetLayoutMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YunJiApp(
    modifier: Modifier = Modifier,
    animationEnabled: Boolean = true,
    developerToolsEnabled: Boolean = false,
    temperatureUnit: String,
    windUnit: String,
    homeModules: List<HomeModuleDefinition> = HomeModuleCatalog.getBuiltInModules(),
    homeModuleEnabled: Map<String, Boolean> = emptyMap(),
    homeViewModel: HomeViewModel = viewModel(),
    locationUiState: LocationUiState = LocationUiState.idle(),
    onRequestLocation: () -> Unit = {},
    onHomeModuleEnabledChange: (HomeModuleDefinition, Boolean) -> Unit = { _, _ -> },
    onMoveHomeModuleUp: (HomeModuleDefinition) -> Unit = {},
    onMoveHomeModuleDown: (HomeModuleDefinition) -> Unit = {},
    onResetHomeBlocks: () -> Unit = {},
    onDisplayedWeatherIconCodeChanged: (String?) -> Unit = {}
) {
    var activeTarget by rememberSaveable { mutableStateOf<WeatherNavigationTarget?>(null) }
    var showPersonalizationBlockEditor by rememberSaveable { mutableStateOf(false) }
    var showCityEditor by rememberSaveable { mutableStateOf(false) }
    var personalizationBackRequestVersion by rememberSaveable { mutableStateOf(0) }
    var noticeText by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val visualTheme = LocalYunJiVisualTheme.current
    val homeUiState by homeViewModel.uiState.observeAsState()
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
            personalizationBackRequestVersion = 0
        }
        if (activePage != WeatherNavigationTarget.MANAGE_CITIES) {
            showCityEditor = false
        }
    }

    BackHandler(enabled = activeTarget != null || showPersonalizationBlockEditor) {
        if (showPersonalizationBlockEditor) {
            showPersonalizationBlockEditor = false
        } else if (showCityEditor) {
            showCityEditor = false
        } else {
            closePageAndRefresh()
        }
    }

    AnimatedContent(
        targetState = activePage,
        label = "root-page-transition",
        transitionSpec = {
            val slideDuration = if (animationEnabled) 280 else 0
            val fadeDuration = if (animationEnabled) 220 else 0
            if (initialState == null && targetState != null) {
                (slideInHorizontally(animationSpec = tween(slideDuration)) { width -> width / 5 } + fadeIn(tween(fadeDuration))) togetherWith
                        (slideOutHorizontally(animationSpec = tween(slideDuration)) { width -> -width / 10 } + fadeOut(tween(fadeDuration)))
            } else if (initialState != null && targetState == null) {
                (slideInHorizontally(animationSpec = tween(slideDuration)) { width -> -width / 10 } + fadeIn(tween(fadeDuration))) togetherWith
                        (slideOutHorizontally(animationSpec = tween(slideDuration)) { width -> width / 5 } + fadeOut(tween(fadeDuration)))
            } else {
                fadeIn(tween(fadeDuration)) togetherWith fadeOut(tween(fadeDuration))
            }
        }
    ) { targetPage ->
        if (targetPage == null) {
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
                    homeModules = homeModules,
                    homeModuleEnabled = homeModuleEnabled,
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
            return@AnimatedContent
        }
        val activePage = targetPage
        val immersivePage = activePage == WeatherNavigationTarget.MANAGE_CITIES ||
                activePage == WeatherNavigationTarget.DESKTOP_WEATHER ||
                activePage == WeatherNavigationTarget.PERSONALIZATION
        val pageTitleColor = when {
            visualTheme.defaultWeatherGradient.top.luminance() < 0.35f ||
                    visualTheme.background.luminance() < 0.35f -> Color.White
            activePage == WeatherNavigationTarget.MANAGE_CITIES &&
                    MaterialTheme.colorScheme.background.luminance() < 0.35f -> Color.White
            else -> MaterialTheme.colorScheme.onBackground
        }
        val pageSubtitleColor = if (pageTitleColor == Color.White) {
            Color.White.copy(alpha = 0.72f)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
        val pageBack = when {
            activePage == WeatherNavigationTarget.MANAGE_CITIES && showCityEditor -> {
                { showCityEditor = false }
            }
            activePage == WeatherNavigationTarget.PERSONALIZATION -> {
                { personalizationBackRequestVersion += 1 }
            }
            else -> closePageAndRefresh
        }
        WeatherPageScaffold(
            title = if (activePage == WeatherNavigationTarget.MANAGE_CITIES && showCityEditor) {
                "\u7f16\u8f91\u57ce\u5e02"
            } else {
                activePageTitle(activePage)
            },
            subtitle = when (activePage) {
                WeatherNavigationTarget.MANAGE_CITIES,
                WeatherNavigationTarget.DESKTOP_WEATHER,
                WeatherNavigationTarget.PERSONALIZATION -> null
                else -> activePageSubtitle(activePage)
            },
            onBack = pageBack,
            modifier = modifier,
            immersive = immersivePage,
            backIconResId = if (activePage == WeatherNavigationTarget.MANAGE_CITIES && showCityEditor) {
                R.drawable.ic_close_24
            } else {
                R.drawable.ic_arrow_back_24
            },
            titleColor = pageTitleColor,
            subtitleColor = pageSubtitleColor,
            action = when (activePage) {
                WeatherNavigationTarget.PERSONALIZATION -> {
                    {
                        IconButton(
                            modifier = Modifier.size(YunJiUiTokens.PageHeaderIconButtonSize),
                            onClick = { showPersonalizationBlockEditor = true }
                        ) {
                            Icon(
                                modifier = Modifier.size(YunJiUiTokens.PageHeaderIconSize),
                                painter = painterResource(R.drawable.ic_settings_24),
                                contentDescription = "\u8c03\u6574\u9996\u9875\u6a21\u5757",
                                tint = pageTitleColor
                            )
                        }
                    }
                }
                WeatherNavigationTarget.MANAGE_CITIES -> {
                    {
                        IconButton(
                            modifier = Modifier.size(YunJiUiTokens.PageHeaderIconButtonSize),
                            onClick = { showCityEditor = !showCityEditor }
                        ) {
                            Icon(
                                modifier = Modifier.size(YunJiUiTokens.PageHeaderIconSize),
                                painter = painterResource(if (showCityEditor) R.drawable.ic_check_24 else R.drawable.ic_settings_24),
                                contentDescription = if (showCityEditor) {
                                    "\u5b8c\u6210\u7f16\u8f91\u57ce\u5e02"
                                } else {
                                    "\u7f16\u8f91\u57ce\u5e02"
                                },
                                tint = pageTitleColor
                            )
                        }
                    }
                }
                else -> null
            }
        ) { pageModifier ->
            when (activePage) {
                WeatherNavigationTarget.MANAGE_CITIES -> CityScreen(
                    modifier = pageModifier,
                    temperatureUnit = temperatureUnit,
                    respectStatusBar = false,
                    showHeader = false,
                    editing = showCityEditor,
                    locationUiState = locationUiState,
                    onDefaultCityChanged = homeViewModel::refresh
                )

                WeatherNavigationTarget.DESKTOP_WEATHER -> DesktopWeatherScreen(
                    modifier = pageModifier,
                    homeWeatherData = homeUiState?.data,
                    homeWeatherUpdateTime = homeUiState?.updateTime ?: homeUiState?.data?.updateTime ?: 0L,
                    temperatureUnit = temperatureUnit,
                    animationEnabled = animationEnabled,
                    locationUiState = locationUiState,
                    onRequestLocation = onRequestLocation,
                    onRequestWidget = { mode -> noticeText = requestWeatherWidgetPin(context, mode) }
                )

                WeatherNavigationTarget.PERSONALIZATION -> PersonalizationScreen(
                    modifier = pageModifier,
                    onOpenHomeBlockEditor = { showPersonalizationBlockEditor = true },
                    backRequestVersion = personalizationBackRequestVersion,
                    homeWeatherData = homeUiState?.data,
                    homeWeatherUpdateTime = homeUiState?.updateTime ?: homeUiState?.data?.updateTime ?: 0L,
                    temperatureUnit = temperatureUnit,
                    onExitRequested = closePageAndRefresh
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
                        locationUiState = locationUiState,
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
                homeModules = homeModules,
                homeModuleEnabled = homeModuleEnabled,
                onHomeModuleEnabledChange = onHomeModuleEnabledChange,
                onMoveHomeModuleUp = onMoveHomeModuleUp,
                onMoveHomeModuleDown = onMoveHomeModuleDown,
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
                    Text("\u77e5\u9053\u4e86")
                }
            }
        )
    }
}

private fun activePageTitle(target: WeatherNavigationTarget): String {
    return when (target) {
        WeatherNavigationTarget.MANAGE_CITIES -> "\u7ba1\u7406\u57ce\u5e02"
        WeatherNavigationTarget.DESKTOP_WEATHER -> "\u684c\u9762\u5929\u6c14"
        WeatherNavigationTarget.PERSONALIZATION -> "\u4e2a\u6027\u6362\u80a4"
        WeatherNavigationTarget.SETTINGS -> "\u8bbe\u7f6e"
        WeatherNavigationTarget.ALERTS -> "\u5929\u6c14\u9884\u8b66"
        WeatherNavigationTarget.LIFE_INDEX -> "\u751f\u6d3b\u5efa\u8bae"
        WeatherNavigationTarget.SEARCH_CITY -> "\u641c\u7d22\u57ce\u5e02"
    }
}

private fun activePageSubtitle(target: WeatherNavigationTarget): String? {
    return when (target) {
        WeatherNavigationTarget.MANAGE_CITIES -> "\u641c\u7d22\u3001\u6dfb\u52a0\u3001\u5207\u6362\u9ed8\u8ba4\u57ce\u5e02"
        WeatherNavigationTarget.DESKTOP_WEATHER -> "\u7ba1\u7406\u7cfb\u7edf\u684c\u9762\u5c0f\u7ec4\u4ef6"
        WeatherNavigationTarget.PERSONALIZATION -> "\u9009\u62e9\u4e3b\u9898\uff0c\u53f3\u4e0a\u89d2\u8c03\u6574\u9996\u9875\u6a21\u5757"
        WeatherNavigationTarget.SETTINGS -> "\u901a\u77e5\u3001\u5355\u4f4d\u548c\u672c\u5730\u6570\u636e"
        WeatherNavigationTarget.ALERTS -> null
        WeatherNavigationTarget.LIFE_INDEX -> null
        WeatherNavigationTarget.SEARCH_CITY -> null
    }
}

private fun requestWeatherWidgetPin(context: Context, mode: WeatherWidgetLayoutMode): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
        if (appWidgetManager?.isRequestPinAppWidgetSupported == true) {
            val provider = ComponentName(context, WeatherAppWidgetProvider.providerClassFor(mode))
            val requested = appWidgetManager.requestPinAppWidget(provider, null, null)
            if (requested) {
                return "\u5df2\u8bf7\u6c42\u6dfb\u52a0\u684c\u9762\u5929\u6c14\uff0c\u8bf7\u5728\u7cfb\u7edf\u5f39\u7a97\u4e2d\u786e\u8ba4\u3002"
            }
        }
    }
    return "\u8bf7\u957f\u6309\u684c\u9762\uff0c\u5728\u7cfb\u7edf\u5c0f\u7ec4\u4ef6\u5217\u8868\u4e2d\u9009\u62e9\u4e91\u8ff9\u5929\u6c14\u3002"
}
