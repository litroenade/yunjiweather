package com.litroenade.yunjiweather.ui.compose.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.litroenade.yunjiweather.R
import com.litroenade.yunjiweather.common.UiState
import com.litroenade.yunjiweather.data.entity.WarningEntity
import com.litroenade.yunjiweather.data.model.HomeWeatherData
import com.litroenade.yunjiweather.data.model.HomeWeatherInsightBuilder
import com.litroenade.yunjiweather.data.model.WeatherDailyData
import com.litroenade.yunjiweather.data.model.WeatherHourlyData
import com.litroenade.yunjiweather.ui.compose.InfoCard
import com.litroenade.yunjiweather.ui.compose.MessageCard
import com.litroenade.yunjiweather.ui.compose.MetricTile
import com.litroenade.yunjiweather.ui.compose.SectionTitle
import com.litroenade.yunjiweather.ui.compose.UriImage
import com.litroenade.yunjiweather.ui.compose.WeatherAtmosphere
import com.litroenade.yunjiweather.ui.compose.WeatherAnimation
import com.litroenade.yunjiweather.ui.compose.WeatherLightContext
import com.litroenade.yunjiweather.ui.compose.WeatherSceneSpec
import com.litroenade.yunjiweather.ui.compose.debug.DebugWeatherOverride
import com.litroenade.yunjiweather.ui.compose.debug.DebugWeatherPresets
import com.litroenade.yunjiweather.ui.compose.formatWeatherTime
import com.litroenade.yunjiweather.ui.compose.home.modules.HomeModuleCatalog
import com.litroenade.yunjiweather.ui.compose.home.modules.HomeModuleDefinition
import com.litroenade.yunjiweather.ui.compose.theme.LocalCustomThemeOptions
import com.litroenade.yunjiweather.ui.compose.theme.LocalThemeSkin
import com.litroenade.yunjiweather.ui.compose.theme.LocalYunJiVisualTheme
import com.litroenade.yunjiweather.ui.compose.theme.rememberCustomThemeImageAnalysis
import com.litroenade.yunjiweather.ui.compose.theme.effects.ThemeWeatherEffect
import com.litroenade.yunjiweather.ui.compose.theme.effects.ThemeWeatherEffectCatalog
import com.litroenade.yunjiweather.ui.compose.theme.skins.ThemeSkin
import com.litroenade.yunjiweather.ui.home.HomeViewModel
import com.litroenade.yunjiweather.utils.AirQualityUtils
import com.litroenade.yunjiweather.utils.LunarCalendarUtils
import com.litroenade.yunjiweather.utils.SunProgressState
import com.litroenade.yunjiweather.utils.SunriseSunsetProgress
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils
import com.litroenade.yunjiweather.utils.WeatherIconUtils
import com.litroenade.yunjiweather.utils.VisualThemeUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * 首页主天气面板，组合切城、下拉刷新、动态天气背景和可重排模块。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    animationEnabled: Boolean = true,
    developerToolsEnabled: Boolean = false,
    temperatureUnit: String = WeatherDisplayUtils.TEMPERATURE_CELSIUS,
    windUnit: String = WeatherDisplayUtils.WIND_SCALE,
    homeModules: List<HomeModuleDefinition> = HomeModuleCatalog.getBuiltInModules(),
    homeModuleEnabled: Map<String, Boolean> = emptyMap(),
    onManageCities: () -> Unit = {},
    onSearchCity: () -> Unit = {},
    onSettings: () -> Unit = {},
    onPersonalization: () -> Unit = {},
    onDesktopWeather: () -> Unit = {},
    onOpenAlerts: () -> Unit = {},
    onOpenLifeIndex: () -> Unit = {},
    onDisplayedWeatherIconCodeChanged: (String?) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.observeAsState()
    val message by viewModel.message.observeAsState("")
    val cityPages by viewModel.cityPages.observeAsState(emptyList())
    val selectedCityPage by viewModel.selectedCityPage.observeAsState(0)
    val activeWarnings by viewModel.activeWarnings.observeAsState(emptyList())
    val isRefreshing by viewModel.refreshing.observeAsState(false)
    val pullToRefreshState = rememberPullToRefreshState()
    var debugWeatherOverride by remember { mutableStateOf<DebugWeatherOverride?>(null) }
    var showDebugWeatherDialog by remember { mutableStateOf(false) }
    var debugWeatherTapCount by remember { mutableIntStateOf(0) }
    val swipeThresholdPx = with(LocalDensity.current) { 72.dp.toPx() }
    var horizontalSwipeDistance by remember { mutableFloatStateOf(0f) }
    var isCityDragging by remember { mutableStateOf(false) }
    var citySwitchDirection by remember { mutableIntStateOf(1) }
    val cityDragOffset by animateFloatAsState(
        targetValue = if (isCityDragging) {
            horizontalSwipeDistance
                .coerceIn(-swipeThresholdPx * 1.4f, swipeThresholdPx * 1.4f) * 0.22f
        } else {
            0f
        },
        animationSpec = tween(
            durationMillis = if (isCityDragging) 90 else 240,
            easing = FastOutSlowInEasing
        ),
        label = "city-page-drag-offset"
    )
    val cityDragAlpha = 1f - (abs(cityDragOffset) / swipeThresholdPx).coerceIn(0f, 0.08f)
    LaunchedEffect(Unit) {
        viewModel.loadHomeWeather()
    }

    val weatherData = uiState?.data
    val displayedWeatherData = remember(weatherData, debugWeatherOverride) {
        weatherData?.let { data -> debugWeatherOverride?.applyTo(data) ?: data }
    }
    LaunchedEffect(weatherData?.locationId, developerToolsEnabled) {
        if (!developerToolsEnabled) {
            debugWeatherOverride = null
        }
        debugWeatherTapCount = 0
        showDebugWeatherDialog = false
    }
    LaunchedEffect(displayedWeatherData?.iconCode) {
        onDisplayedWeatherIconCodeChanged(displayedWeatherData?.iconCode)
    }
    val lastUpdateText = remember(uiState, weatherData) {
        lastWeatherUpdateLabel(uiState, weatherData)
    }
    val citySwipeModifier = if (cityPages.size > 1) {
        Modifier.pointerInput(cityPages, selectedCityPage) {
            detectHorizontalDragGestures(
                onDragStart = {
                    horizontalSwipeDistance = 0f
                    isCityDragging = true
                },
                onHorizontalDrag = { _, dragAmount ->
                    horizontalSwipeDistance += dragAmount
                },
                onDragCancel = {
                    isCityDragging = false
                    horizontalSwipeDistance = 0f
                },
                onDragEnd = {
                    if (abs(horizontalSwipeDistance) >= swipeThresholdPx) {
                        val currentPage = selectedCityPage.coerceIn(0, cityPages.lastIndex)
                        val nextPage = if (horizontalSwipeDistance < 0f) {
                            citySwitchDirection = 1
                            (currentPage + 1) % cityPages.size
                        } else {
                            citySwitchDirection = -1
                            (currentPage - 1 + cityPages.size) % cityPages.size
                        }
                        viewModel.loadHomeWeatherForCity(cityPages[nextPage].locationId)
                    }
                    isCityDragging = false
                    horizontalSwipeDistance = 0f
                }
            )
        }
    } else {
        Modifier
    }

    val headerCityName = displayedWeatherData?.cityName ?: "\u4e91\u8ff9\u5929\u6c14"
    val headerNoticeText = homeHeaderNoticeText(message, isRefreshing, uiState)
    val visualTheme = LocalYunJiVisualTheme.current
    val themeSkin = LocalThemeSkin.current
    val customThemeOptions = LocalCustomThemeOptions.current
    val themeWeatherEffect = remember(themeSkin.key) {
        ThemeWeatherEffectCatalog.getEffect(themeSkin.key)
    }
    val weatherSceneSpec = remember(displayedWeatherData?.iconCode) {
        WeatherSceneSpec.fromIconCode(displayedWeatherData?.iconCode ?: "100")
    }
    var visualMinuteOfDay by remember { mutableIntStateOf(currentMinuteOfDay()) }
    LaunchedEffect(Unit) {
        while (true) {
            visualMinuteOfDay = currentMinuteOfDay()
            delay(60_000L)
        }
    }
    val effectiveMinuteOfDay = debugWeatherOverride?.time?.minuteOfDay ?: visualMinuteOfDay
    val lightContext = remember(
        debugWeatherOverride,
        displayedWeatherData?.sunrise,
        displayedWeatherData?.sunset,
        effectiveMinuteOfDay
    ) {
        val sunrise = displayedWeatherData?.sunrise ?: ""
        val sunset = displayedWeatherData?.sunset ?: ""
        debugWeatherOverride?.lightContext(sunrise, sunset)
            ?: WeatherLightContext.fromMinuteOfDay(sunrise, sunset, effectiveMinuteOfDay)
    }
    val customThemeImage = remember(customThemeOptions, weatherSceneSpec, lightContext, effectiveMinuteOfDay) {
        customThemeOptions.imageForScene(weatherSceneSpec, lightContext, effectiveMinuteOfDay)
    }
    val useCustomBackdrop = themeSkin.key == VisualThemeUtils.THEME_CUSTOM_1 &&
        customThemeImage.uri.isNotBlank()
    val customImageAnalysis by rememberCustomThemeImageAnalysis(
        if (useCustomBackdrop) customThemeImage.uri else ""
    )
    val customForeground = if (useCustomBackdrop && customImageAnalysis.ready) {
        customImageAnalysis.primaryText
    } else {
        null
    }
    val customSecondaryForeground = if (useCustomBackdrop && customImageAnalysis.ready) {
        customImageAnalysis.secondaryText
    } else {
        null
    }
    val backgroundBrush = if (displayedWeatherData?.iconCode.isNullOrBlank()) {
        Brush.verticalGradient(
            listOf(
                visualTheme.defaultWeatherGradient.top,
                visualTheme.defaultWeatherGradient.middle,
                visualTheme.defaultWeatherGradient.bottom
            )
        )
    } else {
        weatherBackground(weatherSceneSpec, lightContext, themeSkin)
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .then(citySwipeModifier)
            .background(backgroundBrush)
    ) {
        val backdropImageResId = themeWeatherEffect.homeBackdropImageResId(weatherSceneSpec, lightContext)
        if (useCustomBackdrop) {
            UriImage(
                uriString = customThemeImage.uri,
                cropAnchor = customThemeImage.cropAnchor,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = -cityDragOffset * 0.16f
                    }
                    .alpha(0.86f),
                mediaType = customThemeImage.mediaType
            )
            HomeBackdropScrim(
                sceneSpec = weatherSceneSpec,
                lightContext = lightContext,
                darkenScale = customImageAnalysis.darkScrimScale,
                modifier = Modifier.fillMaxSize()
            )
        } else if (backdropImageResId != null) {
            Image(
                painter = painterResource(backdropImageResId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = -cityDragOffset * 0.16f
                    }
                    .alpha(themeWeatherEffect.homeBackdropAlpha(weatherSceneSpec, lightContext)),
                contentScale = ContentScale.Crop
            )
            HomeBackdropScrim(
                sceneSpec = weatherSceneSpec,
                lightContext = lightContext,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (animationEnabled) {
            WeatherAtmosphere(
                sceneSpec = weatherSceneSpec,
                lightContext = lightContext,
                immersion = themeSkin.homeImmersion,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = -cityDragOffset * 0.45f
                    }
                    .alpha(themeWeatherEffect.atmosphereLayerAlpha(themeSkin))
            )
        }
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize(),
            state = pullToRefreshState,
            indicator = {
                PullRefreshStatus(
                    isRefreshing = isRefreshing,
                    pullFraction = pullToRefreshState.distanceFraction,
                    lastUpdateText = lastUpdateText,
                    sceneSpec = weatherSceneSpec,
                    lightContext = lightContext,
                    foregroundOverride = customForeground,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 10.dp)
                )
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = cityDragOffset
                        alpha = cityDragAlpha
                    }
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 18.dp, bottom = 64.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            item {
                WeatherTopActions(
                    cityName = headerCityName,
                    noticeText = headerNoticeText,
                    sceneSpec = weatherSceneSpec,
                    lightContext = lightContext,
                    foregroundOverride = customForeground,
                    secondaryForegroundOverride = customSecondaryForeground,
                    onManageCities = onManageCities,
                    onSearchCity = onSearchCity,
                    onSettings = onSettings,
                    onPersonalization = onPersonalization,
                    onDesktopWeather = onDesktopWeather
                )
            }
            when (val state = uiState) {
                null -> item {
                    Spacer(Modifier.height(18.dp))
                    MessageCard(
                        "\u7b49\u5f85\u52a0\u8f7d",
                        "\u6b63\u5728\u8bfb\u53d6\u9ed8\u8ba4\u57ce\u5e02\u5929\u6c14\u3002",
                        "\u5237\u65b0",
                        viewModel::refresh
                    )
                }

                else -> when (state.status) {
                    UiState.Status.LOADING -> item { LoadingCard() }
                    UiState.Status.ERROR, UiState.Status.EMPTY -> item {
                        Spacer(Modifier.height(18.dp))
                        MessageCard(
                            title = "\u5929\u6c14\u52a0\u8f7d\u5931\u8d25",
                            message = state.message ?: "\u6682\u65e0\u53ef\u7528\u5929\u6c14\u6570\u636e\u3002",
                            buttonText = "\u91cd\u8bd5",
                            onButtonClick = viewModel::refresh
                        )
                    }

                    UiState.Status.SUCCESS, UiState.Status.CACHE -> {
                        val data = state.data
                        if (data == null) {
                            item {
                                Spacer(Modifier.height(18.dp))
                                MessageCard(
                                    "\u5929\u6c14\u52a0\u8f7d\u5931\u8d25",
                                    "\u5929\u6c14\u6570\u636e\u4e3a\u7a7a\u3002",
                                    "\u91cd\u8bd5",
                                    viewModel::refresh
                                )
                            }
                        } else {
                            val displayData = debugWeatherOverride?.applyTo(data) ?: data
                            item {
                                AnimatedContent(
                                    targetState = displayData,
                                    transitionSpec = {
                                        val direction = citySwitchDirection
                                        val enter = slideInHorizontally(
                                            animationSpec = tween(280, easing = FastOutSlowInEasing)
                                        ) { width -> width * direction / 4 } + fadeIn(
                                            animationSpec = tween(180, easing = FastOutSlowInEasing)
                                        )
                                        val exit = slideOutHorizontally(
                                            animationSpec = tween(240, easing = FastOutSlowInEasing)
                                        ) { width -> -width * direction / 4 } + fadeOut(
                                            animationSpec = tween(160, easing = FastOutSlowInEasing)
                                        )
                                        (enter togetherWith exit).using(SizeTransform(clip = false))
                                    },
                                    label = "city-weather-content"
                                ) { animatedData ->
                                    val animatedSceneSpec = WeatherSceneSpec.fromIconCode(animatedData.iconCode)
                                    val animatedLightContext = debugWeatherOverride?.lightContext(
                                        animatedData.sunrise ?: "",
                                        animatedData.sunset ?: ""
                                    ) ?: WeatherLightContext.fromMinuteOfDay(
                                        animatedData.sunrise ?: "",
                                        animatedData.sunset ?: "",
                                        effectiveMinuteOfDay
                                    )
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        CurrentWeatherSection(
                                            data = animatedData,
                                            state = state,
                                            animationEnabled = animationEnabled,
                                            developerToolsEnabled = developerToolsEnabled,
                                            temperatureUnit = temperatureUnit,
                                            sceneSpec = animatedSceneSpec,
                                            lightContext = animatedLightContext,
                                            foregroundOverride = customForeground,
                                            secondaryForegroundOverride = customSecondaryForeground,
                                            onDeveloperWeatherTap = {
                                                debugWeatherTapCount += 1
                                                if (debugWeatherTapCount >= 5) {
                                                    debugWeatherTapCount = 0
                                                    showDebugWeatherDialog = true
                                                }
                                            }
                                        )
                                        HomeContentBlocks(
                                            data = animatedData,
                                            warnings = activeWarnings,
                                            temperatureUnit = temperatureUnit,
                                            windUnit = windUnit,
                                            modules = homeModules,
                                            enabledModules = homeModuleEnabled,
                                            onOpenAlerts = onOpenAlerts,
                                            onOpenLifeIndex = onOpenLifeIndex,
                                            onPersonalization = onPersonalization
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
        if (cityPages.size > 1) {
            CityPageDots(
                pageCount = cityPages.size,
                currentPage = selectedCityPage,
                sceneSpec = weatherSceneSpec,
                lightContext = lightContext,
                foregroundOverride = customForeground,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp)
            )
        }
    }
    if (showDebugWeatherDialog && developerToolsEnabled && weatherData != null) {
        DebugWeatherDialog(
            selectedOverride = debugWeatherOverride,
            onOverrideSelected = { override ->
                debugWeatherOverride = override
                showDebugWeatherDialog = false
            },
            onDismiss = { showDebugWeatherDialog = false }
        )
    }
}

@Composable
private fun PullRefreshStatus(
    isRefreshing: Boolean,
    pullFraction: Float,
    lastUpdateText: String,
    sceneSpec: WeatherSceneSpec,
    lightContext: WeatherLightContext,
    foregroundOverride: Color?,
    modifier: Modifier = Modifier
) {
    if (!isRefreshing && pullFraction <= 0.02f) {
        return
    }
    val text = when {
        isRefreshing -> "\u6b63\u5728\u66f4\u65b0 \u00b7 $lastUpdateText"
        pullFraction >= 1f -> "\u677e\u5f00\u66f4\u65b0 \u00b7 $lastUpdateText"
        else -> "\u4e0b\u62c9\u66f4\u65b0 \u00b7 $lastUpdateText"
    }
    val themeEffect = ThemeWeatherEffectCatalog.getEffect(LocalThemeSkin.current.key)
    val textColor = foregroundOverride
        ?: weatherScenePrimaryTextColor(sceneSpec, lightContext, MaterialTheme.colorScheme.onSurface, themeEffect)
    val containerColor = weatherSceneFloatingSurfaceColor(sceneSpec, lightContext, MaterialTheme.colorScheme.surface, themeEffect)
    val borderColor = weatherSceneStrokeColor(sceneSpec, lightContext, textColor, themeEffect)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(99.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = textColor,
                    strokeWidth = 2.dp
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LoadingCard() {
    InfoCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator()
            Text(
                text = "\u6b63\u5728\u66f4\u65b0\u5929\u6c14\u6570\u636e",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun CurrentWeatherSection(
    data: HomeWeatherData,
    state: UiState<HomeWeatherData>,
    animationEnabled: Boolean,
    developerToolsEnabled: Boolean,
    temperatureUnit: String,
    sceneSpec: WeatherSceneSpec,
    lightContext: WeatherLightContext,
    foregroundOverride: Color?,
    secondaryForegroundOverride: Color?,
    onDeveloperWeatherTap: () -> Unit
) {
    val visualTheme = LocalYunJiVisualTheme.current
    val themeSkin = LocalThemeSkin.current
    val themeEffect = ThemeWeatherEffectCatalog.getEffect(themeSkin.key)
    val primaryTextColor = foregroundOverride
        ?: weatherScenePrimaryTextColor(sceneSpec, lightContext, visualTheme.primaryWeatherText, themeEffect)
    val secondaryTextColor = secondaryForegroundOverride
        ?: weatherSceneSecondaryTextColor(sceneSpec, lightContext, visualTheme.secondaryWeatherText, themeEffect)
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val compact = with(density) { windowInfo.containerSize.height.toDp() < 760.dp }
    val currentWeatherHeight = if (compact) 292.dp else 330.dp
    val animationWidth = (if (compact) 176.dp else 210.dp) * themeSkin.heroAnimationScale
    val animationHeight = (if (compact) 142.dp else 170.dp) * themeSkin.heroAnimationScale
    val animationTop = if (compact) 28.dp else 36.dp
    val temperatureSize = if (compact) 76.sp else 86.sp
    val temperatureLineHeight = if (compact) 76.sp else 86.sp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(currentWeatherHeight)
    ) {
        when {
            animationEnabled && themeEffect.drawsHeroIcon -> {
                WeatherAnimation(
                    sceneSpec = sceneSpec,
                    lightContext = lightContext,
                    motionScale = themeSkin.heroAnimationScale,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = animationTop)
                        .width(animationWidth)
                        .height(animationHeight)
                        .alpha(0.92f)
                )
            }
            !animationEnabled -> {
                Image(
                    painter = painterResource(WeatherIconUtils.getWeatherIconRes(data.iconCode)),
                    contentDescription = data.condition,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 58.dp, end = 18.dp)
                        .size(124.dp)
                        .alpha(0.72f),
                    colorFilter = ColorFilter.tint(primaryTextColor.copy(alpha = 0.72f))
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(if (compact) 28.dp else 40.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = formatTemperatureDisplay(data.temperature, temperatureUnit),
                    fontSize = temperatureSize,
                    lineHeight = temperatureLineHeight,
                    fontWeight = FontWeight.Light,
                    color = primaryTextColor,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .then(
                            if (developerToolsEnabled) {
                                Modifier.clickable(onClick = onDeveloperWeatherTap)
                            } else {
                                Modifier
                            }
                        ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatTemperatureDisplay(data.tempMax, temperatureUnit)} / ${formatTemperatureDisplay(data.tempMin, temperatureUnit)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = primaryTextColor,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${data.condition}  \u7a7a\u6c14${data.airQualityCategory}",
                    style = MaterialTheme.typography.titleSmall,
                    color = secondaryTextColor,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = if (state.status == UiState.Status.CACHE) {
                    "\u7f13\u5b58 ${formatWeatherTime(state.updateTime)}"
                } else {
                    "更新 ${formatWeatherTime(data.updateTime)}"
                },
                style = MaterialTheme.typography.labelMedium,
                color = secondaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WeatherTopActions(
    cityName: String,
    noticeText: String,
    sceneSpec: WeatherSceneSpec,
    lightContext: WeatherLightContext,
    foregroundOverride: Color?,
    secondaryForegroundOverride: Color?,
    onManageCities: () -> Unit,
    onSearchCity: () -> Unit,
    onSettings: () -> Unit,
    onPersonalization: () -> Unit,
    onDesktopWeather: () -> Unit
) {
    val visualTheme = LocalYunJiVisualTheme.current
    val themeSkin = LocalThemeSkin.current
    val themeEffect = ThemeWeatherEffectCatalog.getEffect(themeSkin.key)
    val primaryTextColor = foregroundOverride
        ?: weatherScenePrimaryTextColor(sceneSpec, lightContext, visualTheme.primaryWeatherText, themeEffect)
    val secondaryTextColor = secondaryForegroundOverride
        ?: weatherSceneSecondaryTextColor(sceneSpec, lightContext, visualTheme.secondaryWeatherText, themeEffect)
    val iconContainerColor = weatherSceneFloatingSurfaceColor(sceneSpec, lightContext, MaterialTheme.colorScheme.surface, themeEffect)
    val iconBorderColor = weatherSceneStrokeColor(sceneSpec, lightContext, primaryTextColor, themeEffect)
    var expanded by remember { mutableStateOf(false) }
    val actionSideWidth = 88.dp
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(actionSideWidth))
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = cityName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = primaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (noticeText.isNotBlank()) {
                Text(
                    text = noticeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row(
            modifier = Modifier.width(actionSideWidth),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End)
        ) {
            WeatherTopIcon(
                iconRes = R.drawable.ic_search_24,
                contentDescription = "\u641c\u7d22\u57ce\u5e02",
                tint = primaryTextColor,
                containerColor = iconContainerColor,
                borderColor = iconBorderColor,
                onClick = onSearchCity
            )
            Box {
                WeatherTopIcon(
                    iconRes = R.drawable.ic_more_vertical_24,
                    contentDescription = "更多",
                    tint = primaryTextColor,
                    containerColor = iconContainerColor,
                    borderColor = iconBorderColor,
                    onClick = { expanded = true }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                ) {
                    DropdownMenuItem(
                        text = { Text("\u7ba1\u7406\u57ce\u5e02") },
                        onClick = {
                            expanded = false
                            onManageCities()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("\u684c\u9762\u5929\u6c14") },
                        onClick = {
                            expanded = false
                            onDesktopWeather()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("\u4e2a\u6027\u5316") },
                        onClick = {
                            expanded = false
                            onPersonalization()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("\u8bbe\u7f6e") },
                        onClick = {
                            expanded = false
                            onSettings()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureEntryTile(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visualTheme = LocalYunJiVisualTheme.current
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.24f),
        border = BorderStroke(1.dp, visualTheme.cardStroke)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WeatherTopIcon(
    iconRes: Int,
    contentDescription: String,
    tint: Color,
    containerColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun CityPageDots(
    pageCount: Int,
    currentPage: Int,
    sceneSpec: WeatherSceneSpec,
    lightContext: WeatherLightContext,
    foregroundOverride: Color?,
    modifier: Modifier = Modifier
) {
    val visualTheme = LocalYunJiVisualTheme.current
    val themeEffect = ThemeWeatherEffectCatalog.getEffect(LocalThemeSkin.current.key)
    val dotColor = foregroundOverride
        ?: weatherScenePrimaryTextColor(sceneSpec, lightContext, visualTheme.primaryWeatherText, themeEffect)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage.coerceIn(0, pageCount - 1)
            val dotWidth by animateDpAsState(
                targetValue = if (selected) 22.dp else 5.dp,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = "city-page-dot-width"
            )
            val dotAlpha by animateFloatAsState(
                targetValue = if (selected) 0.92f else 0.24f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = "city-page-dot-alpha"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(dotWidth)
                    .height(if (selected) 6.dp else 5.dp)
                    .background(
                        color = dotColor.copy(alpha = dotAlpha),
                        shape = RoundedCornerShape(99.dp)
                    )
            )
        }
    }
}

@Composable
internal fun WeatherMetricPanel(data: HomeWeatherData, temperatureUnit: String, windUnit: String) {
    InfoCard {
        SectionTitle("\u4eca\u65e5\u5b9e\u51b5")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile("\u4f53\u611f", formatTemperatureDisplay(data.feelsLike, temperatureUnit), Modifier.weight(1f))
            MetricTile("\u6e7f\u5ea6", "${data.humidity}%", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile("\u98ce\u529b", formatWindDisplay(data, windUnit), Modifier.weight(1f))
            MetricTile("\u80fd\u89c1\u5ea6", "${data.visibility} km", Modifier.weight(1f))
        }
        AirQualitySummary(data)
    }
}

@Composable
internal fun WindDetailPanel(data: HomeWeatherData, windUnit: String) {
    InfoCard {
        SectionTitle("\u98ce\u548c\u98ce\u529b")
        Text(
            text = formatWindDisplay(data, windUnit),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile("\u98ce\u5411", data.windDir, Modifier.weight(1f))
            MetricTile("\u98ce\u901f", "${data.windSpeed} km/h", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile("\u6c14\u538b", "${data.pressure} hPa", Modifier.weight(1f))
            MetricTile("\u80fd\u89c1\u5ea6", "${data.visibility} km", Modifier.weight(1f))
        }
    }
}

@Composable
internal fun SunAndAirPanel(data: HomeWeatherData) {
    InfoCard {
        SectionTitle("\u7a7a\u6c14\u8d28\u91cf\u6982\u89c8")
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFFBEE7C4).copy(alpha = 0.72f),
                                Color(0xFF87C7D8).copy(alpha = 0.60f),
                                Color(0xFFF3D17C).copy(alpha = 0.42f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.32f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = data.cityName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "AQI ${data.airQualityIndex} ${data.airQualityCategory}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile(
                "UV",
                data.uvIndex.ifBlank { "\u6682\u65e0" },
                Modifier.weight(1f)
            )
            MetricTile(
                "\u9996\u8981\u6c61\u67d3\u7269",
                data.primaryPollutant,
                Modifier.weight(1f)
            )
        }
        if (data.sunrise.isNotBlank() && data.sunset.isNotBlank()) {
            SunriseSunsetArc(data.sunrise, data.sunset)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile("\u65e5\u51fa", data.sunrise, Modifier.weight(1f))
                MetricTile("\u65e5\u843d", data.sunset, Modifier.weight(1f))
            }
        } else {
            Text(
                text = "\u5f53\u524d\u7f13\u5b58\u7f3a\u5c11\u65e5\u51fa\u65e5\u843d\u6570\u636e\uff0c\u5237\u65b0\u540e\u4f1a\u4ece\u5929\u6c14\u63a5\u53e3\u8865\u9f50\u3002",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SunriseSunsetArc(sunrise: String, sunset: String) {
    var currentMinute by remember { mutableStateOf(currentMinuteText()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentMinute = currentMinuteText()
            delay(60_000L)
        }
    }
    val progressState = remember(sunrise, sunset, currentMinute) {
        SunriseSunsetProgress.calculate(sunrise, sunset, currentMinute)
    }
    val visualTheme = LocalYunJiVisualTheme.current
    val arcColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.76f)
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.34f)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.26f),
        border = BorderStroke(1.dp, visualTheme.cardStroke)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "\u65e5\u51fa\u65e5\u843d \u00b7 ${progressState.phaseText}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
            ) {
                val baseline = size.height * 0.76f
                val path = Path().apply {
                    moveTo(0f, baseline)
                    quadraticTo(size.width / 2f, size.height * 0.08f, size.width, baseline)
                }
                drawPath(
                    path = path,
                    color = arcColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                drawLine(
                    color = trackColor,
                    start = Offset(0f, baseline),
                    end = Offset(size.width, baseline),
                    strokeWidth = 1.dp.toPx()
                )
                drawCircle(
                    color = Color(0xFFFFC65A),
                    radius = 9.dp.toPx(),
                    center = sunArcOffset(size.width, baseline, progressState)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = sunrise,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = sunset,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun sunArcOffset(width: Float, baseline: Float, state: SunProgressState): Offset {
    val progress = state.progress.coerceIn(0f, 1f)
    val x = width * progress
    val y = quadraticArcY(progress, baseline)
    return Offset(x, y)
}

private fun quadraticArcY(progress: Float, baseline: Float): Float {
    val controlY = baseline * 0.10f
    val oneMinus = 1f - progress
    return oneMinus * oneMinus * baseline + 2f * oneMinus * progress * controlY + progress * progress * baseline
}

private fun currentMinuteText(): String {
    return SimpleDateFormat("HH:mm", Locale.CHINA).format(Date())
}

private fun currentMinuteOfDay(): Int {
    val calendar = Calendar.getInstance(Locale.CHINA)
    return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
}

@Composable
private fun AirQualitySummary(data: HomeWeatherData) {
    val airQualityIndex = AirQualityUtils.parseUsAqiDisplay(data.airQualityIndex)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "\u7a7a\u6c14\u8d28\u91cf",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "AQI ${data.airQualityIndex}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = data.airQualityCategory,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "\u9996\u8981\u6c61\u67d3\u7269 ${data.primaryPollutant}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                text = AirQualityUtils.activityAdviceForUsAqi(airQualityIndex),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (airQualityIndex > 100) {
                Text(
                    text = AirQualityUtils.sensitiveGroupAdviceForUsAqi(airQualityIndex),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun WeatherInsightPanel(
    data: HomeWeatherData,
    warnings: List<WarningEntity>,
    onOpenAlerts: () -> Unit,
    onOpenLifeIndex: () -> Unit
) {
    val insight = remember(data, warnings) {
        HomeWeatherInsightBuilder.build(data, warnings)
    }
    InfoCard {
        SectionTitle("\u4eca\u65e5\u8d44\u8baf")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureEntryTile(
                title = insight.primaryTitle,
                subtitle = insight.primarySubtitle,
                onClick = if (insight.isPrimaryOpensAlerts) onOpenAlerts else onOpenLifeIndex,
                modifier = Modifier.weight(1f)
            )
            FeatureEntryTile(
                title = insight.secondaryTitle,
                subtitle = insight.secondarySubtitle,
                onClick = onOpenLifeIndex,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun AdvicePanel(data: HomeWeatherData) {
    InfoCard {
        SectionTitle("生活建议")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AdviceTile(
                title = "\u7a7f\u8863",
                text = data.clothingAdvice,
                modifier = Modifier.weight(1f)
            )
            AdviceTile(
                title = "\u51fa\u884c",
                text = data.travelAdvice,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun CalendarLifePanel(
    data: HomeWeatherData,
    onOpenLifeIndex: () -> Unit
) {
    val lunarDay = remember { LunarCalendarUtils.today() }
    InfoCard {
        SectionTitle("\u65e5\u5386\u751f\u6d3b")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureEntryTile(
                title = lunarDay.weekdayText,
                subtitle = "${lunarDay.gregorianText} \u00b7 ${lunarDay.lunarText}",
                onClick = onOpenLifeIndex,
                modifier = Modifier.weight(1f)
            )
            FeatureEntryTile(
                title = lunarDay.festivalOrDefaultText,
                subtitle = "\u7ed3\u5408 ${data.cityName} \u7684\u5929\u6c14\u67e5\u770b\u751f\u6d3b\u5efa\u8bae",
                onClick = onOpenLifeIndex,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile("\u7a7f\u8863", data.clothingAdvice, Modifier.weight(1f))
            MetricTile("\u51fa\u884c", data.travelAdvice, Modifier.weight(1f))
        }
    }
}

@Composable
private fun AdviceTile(
    title: String,
    text: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun HourlyForecast(forecasts: List<WeatherHourlyData>, temperatureUnit: String) {
    InfoCard {
        SectionTitle("\u9010\u5c0f\u65f6")
        if (forecasts.isEmpty()) {
            Text("\u6682\u65e0\u9010\u5c0f\u65f6\u6570\u636e", color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@InfoCard
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
        ) {
            items(forecasts.take(12)) { item ->
                HourlyForecastCard(item, temperatureUnit)
            }
        }
    }
}

@Composable
private fun HourlyForecastCard(item: WeatherHourlyData, temperatureUnit: String) {
    Column(
        modifier = Modifier.width(74.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            text = item.timeText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Image(
            painter = painterResource(WeatherIconUtils.getWeatherIconRes(item.iconCode)),
            contentDescription = item.condition,
            modifier = Modifier.size(30.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )
        Text(
            text = formatTemperatureDisplay(item.temperature, temperatureUnit),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = item.condition,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun DailyForecast(
    items: List<WeatherDailyData>,
    temperatureUnit: String,
    referenceTimeMillis: Long
) {
    InfoCard {
        SectionTitle("\u672a\u6765\u51e0\u5929")
        if (items.isEmpty()) {
            Text("\u6682\u65e0\u65e5\u9884\u62a5\u6570\u636e", color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@InfoCard
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
        ) {
            items(items.take(10)) { item ->
                DailyForecastColumn(item, temperatureUnit, referenceTimeMillis)
            }
        }
    }
}

@Composable
private fun DailyForecastColumn(
    item: WeatherDailyData,
    temperatureUnit: String,
    referenceTimeMillis: Long
) {
    val lunarText = remember(item.dateText, referenceTimeMillis) {
        forecastCalendarText(item.dateText, referenceTimeMillis)
    }
    Column(
        modifier = Modifier.width(86.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            text = item.dateText,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = lunarText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Image(
            painter = painterResource(WeatherIconUtils.getWeatherIconRes(item.iconCode)),
            contentDescription = item.condition,
            modifier = Modifier.size(30.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )
        Text(
            text = item.condition,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${formatTemperatureDisplay(item.tempMin, temperatureUnit)}/${formatTemperatureDisplay(item.tempMax, temperatureUnit)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun forecastCalendarText(dateText: String, referenceTimeMillis: Long): String {
    val lunarInfo = LunarCalendarUtils.fromDisplayDate(dateText, referenceTimeMillis)
    return lunarInfo.festivalText?.takeIf { text -> text.isNotBlank() } ?: lunarInfo.lunarText
}

@Composable
private fun DebugWeatherDialog(
    selectedOverride: DebugWeatherOverride?,
    onOverrideSelected: (DebugWeatherOverride?) -> Unit,
    onDismiss: () -> Unit
) {
    val initialOverride = selectedOverride ?: DebugWeatherPresets.defaultOverride()
    var selectedWeather by remember(selectedOverride) { mutableStateOf(initialOverride.weather) }
    var selectedTime by remember(selectedOverride) { mutableStateOf(initialOverride.time) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("\u8c03\u8bd5\u65f6\u95f4\u4e0e\u5929\u6c14") },
        text = {
            LazyColumn(
                modifier = Modifier.height(430.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "\u9009\u62e9\u4e00\u4e2a\u65f6\u95f4\u6bb5\u548c\u4e00\u79cd\u5929\u6c14\uff0c\u7ec4\u5408\u540e\u53ea\u5f71\u54cd\u5f53\u524d\u9875\u9762\u9884\u89c8\uff0c\u4e0d\u5199\u5165\u7f13\u5b58\u3002",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item { DebugSectionTitle("\u65f6\u95f4") }
                items(DebugWeatherPresets.timePresets, key = { preset -> preset.key }) { preset ->
                    DebugWeatherOption(
                        title = preset.title,
                        subtitle = preset.subtitle,
                        selected = selectedTime == preset,
                        onClick = { selectedTime = preset }
                    )
                }
                item { DebugSectionTitle("\u5929\u6c14") }
                items(DebugWeatherPresets.weatherPresets, key = { preset -> preset.key }) { preset ->
                    DebugWeatherOption(
                        title = preset.title,
                        subtitle = preset.subtitle,
                        selected = selectedWeather == preset,
                        onClick = { selectedWeather = preset }
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onOverrideSelected(null) }) {
                Text("恢复真实")
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onDismiss) {
                    Text("关闭")
                }
                TextButton(
                    onClick = {
                        onOverrideSelected(DebugWeatherOverride(selectedWeather, selectedTime))
                    }
                ) {
                    Text("\u5e94\u7528\u7ec4\u5408")
                }
            }
        }
    )
}

@Composable
private fun DebugSectionTitle(text: String) {
    Text(
        modifier = Modifier.padding(top = 4.dp),
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun DebugWeatherOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
    }
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
    }
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatTemperatureDisplay(value: String, unit: String): String {
    return WeatherDisplayUtils.formatTemperature(value, unit)
}

private fun formatWindDisplay(data: HomeWeatherData, unit: String): String {
    return WeatherDisplayUtils.formatWind(data.windDir, data.windScale, data.windSpeed, unit)
}

private fun lastWeatherUpdateLabel(state: UiState<HomeWeatherData>?, data: HomeWeatherData?): String {
    val updateMillis = if (state?.status == UiState.Status.CACHE && state.updateTime > 0L) {
        state.updateTime
    } else {
        data?.updateTime ?: 0L
    }
    return if (updateMillis > 0L) {
        "\u4e0a\u6b21 ${formatWeatherTime(updateMillis)}"
    } else {
        "尚未更新"
    }
}

internal fun homeHeaderNoticeText(
    rawMessage: String,
    isRefreshing: Boolean,
    state: UiState<HomeWeatherData>?
): String {
    val message = rawMessage.trim()
    if (message.isEmpty()) {
        return ""
    }
    return if (isCacheRefreshingNotice(message)) {
        if (isRefreshing && state?.status == UiState.Status.CACHE) message else ""
    } else {
        message
    }
}

private fun isCacheRefreshingNotice(message: String): Boolean {
    return message.contains("\u6b63\u5728\u540c\u6b65") ||
        (message.contains("\u672c\u5730\u7f13\u5b58") && message.contains("\u6b63\u5728\u5237\u65b0\u5929\u6c14")) ||
        message.contains("\u5df2\u663e\u793a\u8fc7\u671f\u672c\u5730\u7f13\u5b58") ||
        message.contains("\u5df2\u663e\u793a\u672c\u5730\u7f13\u5b58")
}

@Composable
private fun HomeBackdropScrim(
    sceneSpec: WeatherSceneSpec,
    lightContext: WeatherLightContext,
    modifier: Modifier = Modifier,
    darkenScale: Float = 1f
) {
    val night = lightContext.isNight || sceneSpec.category == WeatherIconUtils.WeatherCategory.NIGHT
    val warmPhase = lightContext.phase == WeatherLightContext.Phase.DAWN || lightContext.phase == WeatherLightContext.Phase.DUSK
    val topAlpha = when {
        night -> 0.44f
        warmPhase -> 0.22f
        else -> 0.26f
    }
    val bottomAlpha = when {
        night -> 0.54f
        warmPhase -> 0.30f
        else -> 0.34f
    }
    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFF06151A).copy(alpha = topAlpha * darkenScale),
                    Color.Transparent,
                    Color(0xFF06151A).copy(alpha = bottomAlpha * darkenScale)
                )
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                listOf(
                    Color.White.copy(alpha = if (sceneSpec.hasCelestialGlow()) 0.20f else 0.10f),
                    Color.Transparent
                ),
                center = Offset(size.width * (0.16f + lightContext.dayProgress * 0.68f).coerceIn(0.12f, 0.86f), size.height * 0.12f),
                radius = size.width * 0.86f
            )
        )
    }
}

private fun weatherBackground(
    sceneSpec: WeatherSceneSpec,
    lightContext: WeatherLightContext,
    skin: ThemeSkin
): Brush {
    val depth = ((skin.homeImmersion - 1f) * 0.38f).coerceIn(0f, 0.22f)
    val night = lightContext.isNight || sceneSpec.category == WeatherIconUtils.WeatherCategory.NIGHT
    val warmPhase = lightContext.phase == WeatherLightContext.Phase.DAWN ||
        lightContext.phase == WeatherLightContext.Phase.DUSK
    if (night && sceneSpec.precipitation == WeatherSceneSpec.Precipitation.NONE) {
        return Brush.verticalGradient(
            listOf(
                skin.nightGradientTop,
                skin.nightGradientMiddle,
                deepenWeatherColor(skin.nightGradientBottom, depth * 0.62f)
            )
        )
    }
    val phaseTint = when {
        night -> Color(0xFF122331)
        warmPhase && lightContext.phase == WeatherLightContext.Phase.DAWN -> Color(0xFFFFC27A)
        warmPhase -> Color(0xFFFF8E76)
        else -> Color.Transparent
    }
    val tintAmount = when {
        night -> 0.38f
        warmPhase -> 0.20f + lightContext.warmth * 0.12f
        else -> 0f
    }
    val precipitationDepth = when (sceneSpec.precipitation) {
        WeatherSceneSpec.Precipitation.RAIN -> if (night) 0.30f else 0.16f
        WeatherSceneSpec.Precipitation.SNOW -> if (night) 0.22f else 0.08f
        WeatherSceneSpec.Precipitation.NONE -> 0f
    }
    return Brush.verticalGradient(
        listOf(
            phaseTintWeatherColor(
                deepenWeatherColor(Color(sceneSpec.topColor), depth + precipitationDepth),
                phaseTint,
                tintAmount
            ),
            phaseTintWeatherColor(
                deepenWeatherColor(Color(sceneSpec.middleColor), depth * 0.72f + precipitationDepth * 0.72f),
                phaseTint,
                tintAmount * 0.82f
            ),
            phaseTintWeatherColor(
                deepenWeatherColor(Color(sceneSpec.bottomColor), precipitationDepth * 0.45f),
                phaseTint,
                tintAmount * 0.42f
            )
        )
    )
}

private fun deepenWeatherColor(color: Color, amount: Float): Color {
    if (amount <= 0f) {
        return color
    }
    return Color(
        red = (color.red * (1f - amount)).coerceIn(0f, 1f),
        green = (color.green * (1f - amount * 0.92f)).coerceIn(0f, 1f),
        blue = (color.blue * (1f - amount * 0.58f)).coerceIn(0f, 1f),
        alpha = color.alpha
    )
}

private fun phaseTintWeatherColor(color: Color, tint: Color, amount: Float): Color {
    if (amount <= 0f || tint.alpha <= 0f) {
        return color
    }
    val clamped = amount.coerceIn(0f, 1f)
    return Color(
        red = color.red * (1f - clamped) + tint.red * clamped,
        green = color.green * (1f - clamped) + tint.green * clamped,
        blue = color.blue * (1f - clamped) + tint.blue * clamped,
        alpha = color.alpha
    )
}

private fun weatherScenePrimaryTextColor(
    sceneSpec: WeatherSceneSpec,
    lightContext: WeatherLightContext,
    fallback: Color,
    effect: ThemeWeatherEffect
): Color {
    return if (effect.usesImmersiveForeground(sceneSpec, lightContext)) {
        Color.White
    } else {
        fallback
    }
}

private fun weatherSceneSecondaryTextColor(
    sceneSpec: WeatherSceneSpec,
    lightContext: WeatherLightContext,
    fallback: Color,
    effect: ThemeWeatherEffect
): Color {
    return if (effect.usesImmersiveForeground(sceneSpec, lightContext)) {
        Color.White.copy(alpha = 0.78f)
    } else {
        fallback
    }
}

private fun weatherSceneFloatingSurfaceColor(
    sceneSpec: WeatherSceneSpec,
    lightContext: WeatherLightContext,
    fallback: Color,
    effect: ThemeWeatherEffect
): Color {
    return if (effect.usesImmersiveForeground(sceneSpec, lightContext)) {
        Color.White.copy(alpha = 0.18f)
    } else {
        fallback.copy(alpha = 0.34f)
    }
}

private fun weatherSceneStrokeColor(
    sceneSpec: WeatherSceneSpec,
    lightContext: WeatherLightContext,
    foreground: Color,
    effect: ThemeWeatherEffect
): Color {
    return if (effect.usesImmersiveForeground(sceneSpec, lightContext)) {
        Color.White.copy(alpha = 0.20f)
    } else {
        foreground.copy(alpha = 0.18f)
    }
}
