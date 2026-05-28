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
import com.litroenade.yunjiweather.ui.compose.WeatherAtmosphere
import com.litroenade.yunjiweather.ui.compose.WeatherAnimation
import com.litroenade.yunjiweather.ui.compose.WeatherSceneSpec
import com.litroenade.yunjiweather.ui.compose.formatWeatherTime
import com.litroenade.yunjiweather.ui.compose.theme.LocalThemeSkin
import com.litroenade.yunjiweather.ui.compose.theme.LocalYunJiVisualTheme
import com.litroenade.yunjiweather.ui.compose.theme.effects.ThemeWeatherEffect
import com.litroenade.yunjiweather.ui.compose.theme.effects.ThemeWeatherEffectCatalog
import com.litroenade.yunjiweather.ui.compose.theme.skins.ThemeSkin
import com.litroenade.yunjiweather.ui.home.HomeViewModel
import com.litroenade.yunjiweather.utils.AirQualityUtils
import com.litroenade.yunjiweather.utils.HomeBlock
import com.litroenade.yunjiweather.utils.LunarCalendarUtils
import com.litroenade.yunjiweather.utils.SunProgressState
import com.litroenade.yunjiweather.utils.SunriseSunsetProgress
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils
import com.litroenade.yunjiweather.utils.WeatherIconUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * Main weather surface. It composes cached-first city paging, pull-to-refresh, weather
 * atmosphere, warning/news cards, and user-reordered home modules into one scroll page.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    animationEnabled: Boolean = true,
    developerToolsEnabled: Boolean = false,
    temperatureUnit: String = WeatherDisplayUtils.TEMPERATURE_CELSIUS,
    windUnit: String = WeatherDisplayUtils.WIND_SCALE,
    homeBlockOrder: List<HomeBlock> = HomeBlock.defaultOrder(),
    homeBlockEnabled: Map<HomeBlock, Boolean> = emptyMap(),
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
    val uiState by viewModel.getUiState().observeAsState()
    val message by viewModel.getMessage().observeAsState("")
    val cityPages by viewModel.getCityPages().observeAsState(emptyList())
    val selectedCityPage by viewModel.getSelectedCityPage().observeAsState(0)
    val activeWarnings by viewModel.getActiveWarnings().observeAsState(emptyList())
    val isRefreshing by viewModel.getRefreshing().observeAsState(false)
    val pullToRefreshState = rememberPullToRefreshState()
    var debugWeatherScenario by remember { mutableStateOf<DebugWeatherScenario?>(null) }
    var showDebugWeatherDialog by remember { mutableStateOf(false) }
    var debugWeatherTapCount by remember { mutableStateOf(0) }
    val swipeThresholdPx = with(LocalDensity.current) { 72.dp.toPx() }
    var horizontalSwipeDistance by remember { mutableStateOf(0f) }
    var isCityDragging by remember { mutableStateOf(false) }
    var citySwitchDirection by remember { mutableStateOf(1) }
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
    val displayedWeatherData = remember(weatherData, debugWeatherScenario) {
        weatherData?.let { data -> debugWeatherScenario?.applyTo(data) ?: data }
    }
    LaunchedEffect(weatherData?.locationId, developerToolsEnabled) {
        if (!developerToolsEnabled) {
            debugWeatherScenario = null
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
    val themeWeatherEffect = remember(themeSkin.key) {
        ThemeWeatherEffectCatalog.getEffect(themeSkin.key)
    }
    val weatherSceneSpec = remember(displayedWeatherData?.iconCode) {
        WeatherSceneSpec.fromIconCode(displayedWeatherData?.iconCode ?: "100")
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
        val backdropImageResId = themeWeatherEffect.homeBackdropImageResId
        if (backdropImageResId != null) {
            Image(
                painter = painterResource(backdropImageResId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = -cityDragOffset * 0.16f
                    }
                    .alpha(themeWeatherEffect.homeBackdropAlpha(weatherSceneSpec)),
                contentScale = ContentScale.Crop
            )
            HomeBackdropScrim(
                sceneSpec = weatherSceneSpec,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (animationEnabled) {
            WeatherAtmosphere(
                sceneSpec = weatherSceneSpec,
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
                            val displayData = debugWeatherScenario?.applyTo(data) ?: data
                            item {
                                AnimatedContent(
                                    targetState = "${displayData.locationId}:${displayData.iconCode}",
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
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        CurrentWeatherSection(
                                            data = displayData,
                                            state = state,
                                            animationEnabled = animationEnabled,
                                            developerToolsEnabled = developerToolsEnabled,
                                            temperatureUnit = temperatureUnit,
                                            sceneSpec = weatherSceneSpec,
                                            onDeveloperWeatherTap = {
                                                debugWeatherTapCount += 1
                                                if (debugWeatherTapCount >= 5) {
                                                    debugWeatherTapCount = 0
                                                    showDebugWeatherDialog = true
                                                }
                                            }
                                        )
                                        HomeContentBlocks(
                                            data = displayData,
                                            warnings = activeWarnings,
                                            temperatureUnit = temperatureUnit,
                                            windUnit = windUnit,
                                            blocks = homeBlockOrder,
                                            enabledBlocks = homeBlockEnabled,
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
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp)
            )
        }
    }
    if (showDebugWeatherDialog && developerToolsEnabled && weatherData != null) {
        DebugWeatherDialog(
            selectedScenario = debugWeatherScenario,
            onScenarioSelected = { scenario ->
                debugWeatherScenario = scenario
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
    val textColor = weatherScenePrimaryTextColor(sceneSpec, MaterialTheme.colorScheme.onSurface, themeEffect)
    val containerColor = weatherSceneFloatingSurfaceColor(sceneSpec, MaterialTheme.colorScheme.surface, themeEffect)
    val borderColor = weatherSceneStrokeColor(sceneSpec, textColor, themeEffect)
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
    onDeveloperWeatherTap: () -> Unit
) {
    val visualTheme = LocalYunJiVisualTheme.current
    val themeSkin = LocalThemeSkin.current
    val themeEffect = ThemeWeatherEffectCatalog.getEffect(themeSkin.key)
    val primaryTextColor = weatherScenePrimaryTextColor(sceneSpec, visualTheme.primaryWeatherText, themeEffect)
    val secondaryTextColor = weatherSceneSecondaryTextColor(sceneSpec, visualTheme.secondaryWeatherText, themeEffect)
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
    onManageCities: () -> Unit,
    onSearchCity: () -> Unit,
    onSettings: () -> Unit,
    onPersonalization: () -> Unit,
    onDesktopWeather: () -> Unit
) {
    val visualTheme = LocalYunJiVisualTheme.current
    val themeSkin = LocalThemeSkin.current
    val themeEffect = ThemeWeatherEffectCatalog.getEffect(themeSkin.key)
    val primaryTextColor = weatherScenePrimaryTextColor(sceneSpec, visualTheme.primaryWeatherText, themeEffect)
    val secondaryTextColor = weatherSceneSecondaryTextColor(sceneSpec, visualTheme.secondaryWeatherText, themeEffect)
    val iconContainerColor = weatherSceneFloatingSurfaceColor(sceneSpec, MaterialTheme.colorScheme.surface, themeEffect)
    val iconBorderColor = weatherSceneStrokeColor(sceneSpec, primaryTextColor, themeEffect)
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
    modifier: Modifier = Modifier
) {
    val visualTheme = LocalYunJiVisualTheme.current
    val themeEffect = ThemeWeatherEffectCatalog.getEffect(LocalThemeSkin.current.key)
    val dotColor = weatherScenePrimaryTextColor(sceneSpec, visualTheme.primaryWeatherText, themeEffect)
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
private fun WeatherMetricPanel(data: HomeWeatherData, temperatureUnit: String, windUnit: String) {
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
private fun HomeContentBlocks(
    data: HomeWeatherData,
    warnings: List<WarningEntity>,
    temperatureUnit: String,
    windUnit: String,
    blocks: List<HomeBlock>,
    enabledBlocks: Map<HomeBlock, Boolean>,
    onOpenAlerts: () -> Unit,
    onOpenLifeIndex: () -> Unit,
    onPersonalization: () -> Unit
) {
    val visibleBlocks = blocks.filter { block -> enabledBlocks[block] ?: true }
    if (visibleBlocks.isEmpty()) {
        MessageCard(
            title = "首页模块已全部隐藏",
            message = "可在个性换肤右上角设置中恢复默认布局。",
            buttonText = "打开个性换肤",
            onButtonClick = onPersonalization
        )
        return
    }
    visibleBlocks.forEach { block ->
        when (block) {
            HomeBlock.WEATHER_METRICS -> WeatherMetricPanel(data, temperatureUnit, windUnit)
            HomeBlock.WIND_DETAIL -> WindDetailPanel(data, windUnit)
            HomeBlock.AIR_SUN -> SunAndAirPanel(data)
            HomeBlock.ADVICE -> AdvicePanel(data)
            HomeBlock.WEATHER_INSIGHT -> WeatherInsightPanel(data, warnings, onOpenAlerts, onOpenLifeIndex)
            HomeBlock.HOURLY_FORECAST -> HourlyForecast(data.hourlyForecasts, temperatureUnit)
            HomeBlock.DAILY_FORECAST -> DailyForecast(data.dailyForecasts, temperatureUnit, data.updateTime)
        }
    }
}

@Composable
private fun WindDetailPanel(data: HomeWeatherData, windUnit: String) {
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
private fun SunAndAirPanel(data: HomeWeatherData) {
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
private fun WeatherInsightPanel(
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
private fun AdvicePanel(data: HomeWeatherData) {
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
private fun HourlyForecast(forecasts: List<WeatherHourlyData>, temperatureUnit: String) {
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
private fun DailyForecast(
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

private data class DebugWeatherScenario(
    val title: String,
    val subtitle: String,
    val temperature: String,
    val feelsLike: String,
    val tempMax: String,
    val tempMin: String,
    val condition: String,
    val iconCode: String,
    val windDir: String,
    val windScale: String,
    val windSpeed: String,
    val visibility: String,
    val airQualityIndex: String,
    val airQualityCategory: String,
    val primaryPollutant: String,
    val uvIndex: String,
    val clothingAdvice: String,
    val travelAdvice: String
) {
    fun applyTo(data: HomeWeatherData): HomeWeatherData {
        return HomeWeatherData(
            data.cityName,
            data.locationId,
            temperature,
            condition,
            feelsLike,
            tempMax,
            tempMin,
            data.humidity,
            windDir,
            windScale,
            windSpeed,
            data.pressure,
            visibility,
            iconCode,
            data.updateTime,
            clothingAdvice,
            travelAdvice,
            airQualityIndex,
            airQualityCategory,
            primaryPollutant,
            uvIndex,
            data.sunrise,
            data.sunset,
            data.hourlyForecasts.map { item ->
                item.copy(condition = condition, iconCode = iconCode)
            },
            data.dailyForecasts.map { item ->
                item.copy(condition = condition, iconCode = iconCode)
            }
        )
    }
}

private val debugWeatherScenarios = listOf(
    DebugWeatherScenario(
        title = "晴天",
        subtitle = "强光、低云量、空气较好",
        temperature = "29",
        feelsLike = "31",
        tempMax = "32",
        tempMin = "23",
        condition = "晴",
        iconCode = "100",
        windDir = "东南风",
        windScale = "2",
        windSpeed = "10",
        visibility = "18",
        airQualityIndex = "42",
        airQualityCategory = "优",
        primaryPollutant = "无",
        uvIndex = "8",
        clothingAdvice = "白天气温偏高，建议穿轻薄透气衣物。",
        travelAdvice = "天气晴朗，适合出行，注意防晒补水。"
    ),
    DebugWeatherScenario(
        title = "多云",
        subtitle = "弱光、云层、常规首页状态",
        temperature = "24",
        feelsLike = "25",
        tempMax = "28",
        tempMin = "21",
        condition = "多云",
        iconCode = "101",
        windDir = "东北风",
        windScale = "2",
        windSpeed = "12",
        visibility = "14",
        airQualityIndex = "58",
        airQualityCategory = "良",
        primaryPollutant = "PM2.5",
        uvIndex = "4",
        clothingAdvice = "体感舒适，早晚可加一件薄外套。",
        travelAdvice = "云量较多，整体适宜通勤和户外活动。"
    ),
    DebugWeatherScenario(
        title = "小雨",
        subtitle = "雨层动效、湿度场景",
        temperature = "19",
        feelsLike = "18",
        tempMax = "21",
        tempMin = "17",
        condition = "小雨",
        iconCode = "305",
        windDir = "北风",
        windScale = "3",
        windSpeed = "18",
        visibility = "8",
        airQualityIndex = "35",
        airQualityCategory = "优",
        primaryPollutant = "无",
        uvIndex = "1",
        clothingAdvice = "雨天体感偏凉，建议携带雨具并加外套。",
        travelAdvice = "道路湿滑，出行请预留时间。"
    ),
    DebugWeatherScenario(
        title = "大雪",
        subtitle = "雪粒动效、低温场景",
        temperature = "-4",
        feelsLike = "-8",
        tempMax = "-1",
        tempMin = "-7",
        condition = "大雪",
        iconCode = "402",
        windDir = "西北风",
        windScale = "4",
        windSpeed = "24",
        visibility = "4",
        airQualityIndex = "66",
        airQualityCategory = "良",
        primaryPollutant = "PM10",
        uvIndex = "1",
        clothingAdvice = "低温降雪，建议穿厚羽绒服并注意防滑。",
        travelAdvice = "积雪可能影响交通，减少不必要外出。"
    ),
    DebugWeatherScenario(
        title = "夜间晴",
        subtitle = "深色天空、夜间图标",
        temperature = "18",
        feelsLike = "17",
        tempMax = "23",
        tempMin = "16",
        condition = "晴",
        iconCode = "150",
        windDir = "南风",
        windScale = "1",
        windSpeed = "6",
        visibility = "20",
        airQualityIndex = "48",
        airQualityCategory = "优",
        primaryPollutant = "无",
        uvIndex = "0",
        clothingAdvice = "夜间气温下降，外出可加薄外套。",
        travelAdvice = "夜间能见度较好，注意温差。"
    )
)

@Composable
private fun DebugWeatherDialog(
    selectedScenario: DebugWeatherScenario?,
    onScenarioSelected: (DebugWeatherScenario?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("调试天气") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "只改变当前页面显示，不写入缓存，也不会影响真实刷新结果。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DebugWeatherOption(
                    title = "恢复真实天气",
                    subtitle = "回到缓存或接口返回的数据",
                    selected = selectedScenario == null,
                    onClick = { onScenarioSelected(null) }
                )
                debugWeatherScenarios.forEach { scenario ->
                    DebugWeatherOption(
                        title = scenario.title,
                        subtitle = scenario.subtitle,
                        selected = selectedScenario == scenario,
                        onClick = { onScenarioSelected(scenario) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
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
    modifier: Modifier = Modifier
) {
    val topAlpha = if (sceneSpec.category == WeatherIconUtils.WeatherCategory.NIGHT) 0.44f else 0.26f
    val bottomAlpha = if (sceneSpec.category == WeatherIconUtils.WeatherCategory.NIGHT) 0.54f else 0.34f
    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFF06151A).copy(alpha = topAlpha),
                    Color.Transparent,
                    Color(0xFF06151A).copy(alpha = bottomAlpha)
                )
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                listOf(
                    Color.White.copy(alpha = if (sceneSpec.hasCelestialGlow()) 0.20f else 0.10f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.72f, size.height * 0.12f),
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

private fun weatherScenePrimaryTextColor(sceneSpec: WeatherSceneSpec, fallback: Color, effect: ThemeWeatherEffect): Color {
    return if (effect.usesImmersiveForeground(sceneSpec)) {
        Color.White
    } else {
        fallback
    }
}

private fun weatherSceneSecondaryTextColor(sceneSpec: WeatherSceneSpec, fallback: Color, effect: ThemeWeatherEffect): Color {
    return if (effect.usesImmersiveForeground(sceneSpec)) {
        Color.White.copy(alpha = 0.78f)
    } else {
        fallback
    }
}

private fun weatherSceneFloatingSurfaceColor(sceneSpec: WeatherSceneSpec, fallback: Color, effect: ThemeWeatherEffect): Color {
    return if (effect.usesImmersiveForeground(sceneSpec)) {
        Color.White.copy(alpha = 0.18f)
    } else {
        fallback.copy(alpha = 0.34f)
    }
}

private fun weatherSceneStrokeColor(sceneSpec: WeatherSceneSpec, foreground: Color, effect: ThemeWeatherEffect): Color {
    return if (effect.usesImmersiveForeground(sceneSpec)) {
        Color.White.copy(alpha = 0.20f)
    } else {
        foreground.copy(alpha = 0.18f)
    }
}
