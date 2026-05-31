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
 * 首页主天气面板。
 * 这里把切城、下拉刷新、动态天气背景和可重排模块组合起来，具体卡片渲染下沉到模块层。
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

    val headerCityName = displayedWeatherData?.cityName ?: "云迹天气"
    val headerNoticeText = message
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
        weatherBackground(weatherSceneSpec, themeSkin)
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
                    MessageCard("等待加载", "正在读取默认城市天气。", "刷新", viewModel::refresh)
                }

                else -> when (state.status) {
                    UiState.Status.LOADING -> item { LoadingCard() }
                    UiState.Status.ERROR, UiState.Status.EMPTY -> item {
                        Spacer(Modifier.height(18.dp))
                        MessageCard(
                            title = "天气加载失败",
                            message = state.message ?: "暂无可用天气数据。",
                            buttonText = "重试",
                            onButtonClick = viewModel::refresh
                        )
                    }

                    UiState.Status.SUCCESS, UiState.Status.CACHE -> {
                        val data = state.data
                        if (data == null) {
                            item {
                                Spacer(Modifier.height(18.dp))
                                MessageCard("天气加载失败", "天气数据为空。", "重试", viewModel::refresh)
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
        isRefreshing -> "正在更新 · $lastUpdateText"
        pullFraction >= 1f -> "松开更新 · $lastUpdateText"
        else -> "下拉更新 · $lastUpdateText"
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
                text = "正在更新天气数据",
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
                    text = "${data.condition}  空气${data.airQualityCategory}",
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
                    "缓存 ${formatWeatherTime(state.updateTime)}"
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
                contentDescription = "搜索城市",
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
                        text = { Text("管理城市") },
                        onClick = {
                            expanded = false
                            onManageCities()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("桌面天气") },
                        onClick = {
                            expanded = false
                            onDesktopWeather()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("个性化") },
                        onClick = {
                            expanded = false
                            onPersonalization()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("设置") },
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
        SectionTitle("今日实况")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile("体感", formatTemperatureDisplay(data.feelsLike, temperatureUnit), Modifier.weight(1f))
            MetricTile("湿度", "${data.humidity}%", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile("风力", formatWindDisplay(data, windUnit), Modifier.weight(1f))
            MetricTile("能见度", "${data.visibility} km", Modifier.weight(1f))
        }
        AirQualitySummary(data)
    }
}

@Composable
internal fun WindDetailPanel(data: HomeWeatherData, windUnit: String) {
    InfoCard {
        SectionTitle("风和风力")
        Text(
            text = formatWindDisplay(data, windUnit),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile("风向", data.windDir, Modifier.weight(1f))
            MetricTile("风速", "${data.windSpeed} km/h", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile("气压", "${data.pressure} hPa", Modifier.weight(1f))
            MetricTile("能见度", "${data.visibility} km", Modifier.weight(1f))
        }
    }
}

@Composable
internal fun SunAndAirPanel(data: HomeWeatherData) {
    InfoCard {
        SectionTitle("空气质量概览")
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
                data.uvIndex.ifBlank { "暂无" },
                Modifier.weight(1f)
            )
            MetricTile(
                "首要污染物",
                data.primaryPollutant,
                Modifier.weight(1f)
            )
        }
        if (data.sunrise.isNotBlank() && data.sunset.isNotBlank()) {
            SunriseSunsetArc(data.sunrise, data.sunset)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile("日出", data.sunrise, Modifier.weight(1f))
                MetricTile("日落", data.sunset, Modifier.weight(1f))
            }
        } else {
            Text(
                text = "当前缓存缺少日出日落数据，刷新后会从天气接口补齐。",
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
                text = "日出日落 · ${progressState.phaseText}",
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
                        text = "空气质量",
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
                        text = "首要污染物 ${data.primaryPollutant}",
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
        SectionTitle("今日资讯")
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
                title = "穿衣",
                text = data.clothingAdvice,
                modifier = Modifier.weight(1f)
            )
            AdviceTile(
                title = "出行",
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
        SectionTitle("日历生活")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureEntryTile(
                title = lunarDay.weekdayText,
                subtitle = "${lunarDay.gregorianText} · ${lunarDay.lunarText}",
                onClick = onOpenLifeIndex,
                modifier = Modifier.weight(1f)
            )
            FeatureEntryTile(
                title = lunarDay.festivalOrDefaultText,
                subtitle = "结合 ${data.cityName} 的天气查看生活建议",
                onClick = onOpenLifeIndex,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile("穿衣", data.clothingAdvice, Modifier.weight(1f))
            MetricTile("出行", data.travelAdvice, Modifier.weight(1f))
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
        SectionTitle("逐小时")
        if (forecasts.isEmpty()) {
            Text("暂无逐小时数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        SectionTitle("未来几天")
        if (items.isEmpty()) {
            Text("暂无日预报数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        title = { Text("调试时间与天气") },
        text = {
            LazyColumn(
                modifier = Modifier.height(430.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "选择一个时间段和一种天气，组合后只影响当前页面预览，不写入缓存。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item { DebugSectionTitle("时间") }
                items(DebugWeatherPresets.timePresets, key = { preset -> preset.key }) { preset ->
                    DebugWeatherOption(
                        title = preset.title,
                        subtitle = preset.subtitle,
                        selected = selectedTime == preset,
                        onClick = { selectedTime = preset }
                    )
                }
                item { DebugSectionTitle("天气") }
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
                    Text("应用组合")
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
        "上次 ${formatWeatherTime(updateMillis)}"
    } else {
        "尚未更新"
    }
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

private fun weatherBackground(sceneSpec: WeatherSceneSpec, skin: ThemeSkin): Brush {
    val depth = ((skin.homeImmersion - 1f) * 0.38f).coerceIn(0f, 0.22f)
    if (sceneSpec.category == WeatherIconUtils.WeatherCategory.NIGHT) {
        return Brush.verticalGradient(
            listOf(
                skin.nightGradientTop,
                skin.nightGradientMiddle,
                deepenWeatherColor(skin.nightGradientBottom, depth * 0.62f)
            )
        )
    }
    return Brush.verticalGradient(
        listOf(
            deepenWeatherColor(Color(sceneSpec.topColor), depth),
            deepenWeatherColor(Color(sceneSpec.middleColor), depth * 0.72f),
            Color(sceneSpec.bottomColor)
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
