package com.litroenade.yunjiweather.ui.compose.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litroenade.yunjiweather.R
import com.litroenade.yunjiweather.data.model.CustomThemeAsset
import com.litroenade.yunjiweather.data.model.HomeWeatherData
import com.litroenade.yunjiweather.ui.compose.UriImage
import com.litroenade.yunjiweather.ui.compose.theme.CustomThemeImage
import com.litroenade.yunjiweather.ui.compose.theme.LocalCustomThemeOptions
import com.litroenade.yunjiweather.ui.compose.theme.LocalYunJiVisualTheme
import com.litroenade.yunjiweather.ui.compose.theme.YunJiUiTokens
import com.litroenade.yunjiweather.ui.location.LocationStatus
import com.litroenade.yunjiweather.ui.location.LocationUiState
import com.litroenade.yunjiweather.utils.DateTimeUtils
import com.litroenade.yunjiweather.utils.DefaultCityUtils
import com.litroenade.yunjiweather.utils.VisualThemeUtils
import com.litroenade.yunjiweather.utils.WeatherIconUtils
import com.litroenade.yunjiweather.widget.WeatherWidgetLayoutMode
import com.litroenade.yunjiweather.widget.WeatherWidgetSnapshot
import com.litroenade.yunjiweather.widget.WeatherWidgetSnapshotFactory
import com.litroenade.yunjiweather.widget.WidgetStyleSpec
import java.util.Calendar
import kotlin.math.abs

private val DesktopWidgetModes = listOf(
    WeatherWidgetLayoutMode.COMPACT,
    WeatherWidgetLayoutMode.STANDARD,
    WeatherWidgetLayoutMode.EXPANDED
)

@Composable
fun DesktopWeatherScreen(
    modifier: Modifier = Modifier,
    homeWeatherData: HomeWeatherData? = null,
    homeWeatherUpdateTime: Long = 0L,
    temperatureUnit: String = "\u0043",
    animationEnabled: Boolean = true,
    locationUiState: LocationUiState = LocationUiState.idle(),
    onRequestLocation: () -> Unit = {},
    onRequestWidget: (WeatherWidgetLayoutMode) -> Unit
) {
    var selectedMode by rememberSaveable { mutableStateOf(WeatherWidgetLayoutMode.EXPANDED) }
    var widgetSwitchDirection by rememberSaveable { mutableStateOf(1) }
    var horizontalSwipeDistance by remember { mutableStateOf(0f) }
    var isWidgetDragging by remember { mutableStateOf(false) }
    var useCurrentLocation by rememberSaveable { mutableStateOf(locationUiState.status == LocationStatus.SUCCESS) }
    val visualTheme = LocalYunJiVisualTheme.current
    val customThemeOptions = LocalCustomThemeOptions.current
    val widgetMinuteOfDay = remember { currentWidgetMinuteOfDay() }
    val customWidgetBackground = customWidgetBackground(
        visualTheme.key,
        customThemeOptions,
        homeWeatherData,
        widgetMinuteOfDay
    )
    val previewSnapshot = remember(homeWeatherData, homeWeatherUpdateTime, temperatureUnit, visualTheme.key, customWidgetBackground) {
        val customWidgetAsset = customWidgetBackground.toCustomThemeAsset()
        homeWeatherData?.let { data ->
            WeatherWidgetSnapshotFactory.fromHomeWeather(
                data,
                DateTimeUtils.formatMinuteTime(
                    if (homeWeatherUpdateTime > 0L) homeWeatherUpdateTime else data.updateTime
                ),
                customWidgetAsset,
                temperatureUnit,
                visualTheme.key
            )
        } ?: WeatherWidgetSnapshotFactory.unavailable(
            DefaultCityUtils.DEFAULT_CITY_NAME,
            visualTheme.key,
            customWidgetAsset
        )
    }
    val backgroundLuminance = (
            visualTheme.defaultWeatherGradient.top.luminance() * 0.25f +
                    visualTheme.defaultWeatherGradient.middle.luminance() * 0.35f +
                    visualTheme.defaultWeatherGradient.bottom.luminance() * 0.40f
            )
    val colors = rememberDesktopWeatherColors(backgroundLuminance < 0.42f)
    val swipeThresholdPx = with(LocalDensity.current) { 56.dp.toPx() }
    val dragProgress = if (isWidgetDragging) {
        (abs(horizontalSwipeDistance) / swipeThresholdPx).coerceIn(0f, 1f)
    } else {
        0f
    }
    val dragOffset = if (isWidgetDragging) {
        horizontalSwipeDistance.coerceIn(-swipeThresholdPx, swipeThresholdPx)
    } else {
        0f
    }
    fun selectWidgetMode(mode: WeatherWidgetLayoutMode) {
        val normalizedMode = mode.normalizedWidgetMode()
        if (normalizedMode != selectedMode) {
            widgetSwitchDirection = if (normalizedMode.ordinalIndex() > selectedMode.ordinalIndex()) 1 else -1
            selectedMode = normalizedMode
        }
    }
    fun selectWidgetModeByOffset(offset: Int) {
        selectWidgetMode(selectedMode.widgetModeAtOffset(offset))
    }
    val widgetSwipeModifier = Modifier.pointerInput(selectedMode, swipeThresholdPx) {
        detectHorizontalDragGestures(
            onDragStart = {
                horizontalSwipeDistance = 0f
                isWidgetDragging = true
            },
            onHorizontalDrag = { _, dragAmount ->
                horizontalSwipeDistance += dragAmount
            },
            onDragCancel = {
                horizontalSwipeDistance = 0f
                isWidgetDragging = false
            },
            onDragEnd = {
                if (abs(horizontalSwipeDistance) >= swipeThresholdPx) {
                    if (horizontalSwipeDistance < 0f) {
                        selectWidgetModeByOffset(1)
                    } else {
                        selectWidgetModeByOffset(-1)
                    }
                }
                horizontalSwipeDistance = 0f
                isWidgetDragging = false
            }
        )
    }
    LaunchedEffect(locationUiState.status) {
        useCurrentLocation = when (locationUiState.status) {
            LocationStatus.REQUESTING_PERMISSION,
            LocationStatus.FETCHING_LOCATION,
            LocationStatus.SUCCESS -> true
            LocationStatus.IDLE,
            LocationStatus.ERROR,
            LocationStatus.DENIED -> false
        }
    }
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        DesktopWeatherBackdrop(
            visualThemeKey = visualTheme.key,
            backgroundBrush = Brush.verticalGradient(
                listOf(
                    visualTheme.defaultWeatherGradient.top,
                    visualTheme.defaultWeatherGradient.middle,
                    visualTheme.defaultWeatherGradient.bottom
                )
            ),
            customBackground = customWidgetBackground,
            colors = colors
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = YunJiUiTokens.ScreenHorizontalPadding)
                .padding(top = YunJiUiTokens.ImmersiveContentTopPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WidgetModeTabs(
                selectedMode = selectedMode,
                colors = colors,
                onModeSelected = ::selectWidgetMode
            )
            Spacer(Modifier.weight(0.72f))
            Box(
                modifier = widgetSwipeModifier
                    .fillMaxWidth()
                    .height(176.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    modifier = Modifier.graphicsLayer {
                        translationX = dragOffset * 0.35f
                        alpha = 1f - dragProgress * 0.18f
                        val scale = 1f - dragProgress * 0.03f
                        scaleX = scale
                        scaleY = scale
                    },
                    targetState = selectedMode,
                    transitionSpec = {
                        val direction = widgetSwitchDirection
                        val slideDuration = if (animationEnabled) 260 else 0
                        val fadeDuration = if (animationEnabled) 160 else 0
                        val enter = slideInHorizontally(
                            animationSpec = tween(slideDuration, easing = FastOutSlowInEasing)
                        ) { width -> width * direction / 3 } + fadeIn(
                            animationSpec = tween(fadeDuration, easing = FastOutSlowInEasing)
                        )
                        val exit = slideOutHorizontally(
                            animationSpec = tween(if (animationEnabled) 220 else 0, easing = FastOutSlowInEasing)
                        ) { width -> -width * direction / 3 } + fadeOut(
                            animationSpec = tween(if (animationEnabled) 140 else 0, easing = FastOutSlowInEasing)
                        )
                        (enter togetherWith exit).using(SizeTransform(clip = false))
                    },
                    label = "desktop-widget-mode"
                ) { animatedMode ->
                    DesktopWidgetPreview(
                        selectedMode = animatedMode,
                        snapshot = previewSnapshot,
                        customBackground = customWidgetBackground
                    )
                }
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = selectedMode.widgetModeTitle(),
                style = MaterialTheme.typography.titleMedium,
                color = colors.secondaryText
            )
            PageDots(active = selectedMode.ordinalIndex(), colors = colors)
            Spacer(Modifier.height(34.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "\u6253\u5f00\u5f53\u524d\u4f4d\u7f6e\uff0c\u5c55\u793a\u5b9e\u65f6\u4f4d\u7f6e\u5929\u6c14",
                style = MaterialTheme.typography.titleMedium,
                color = colors.secondaryText
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                shape = RoundedCornerShape(24.dp),
                color = colors.cardContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "\u5f53\u524d\u4f4d\u7f6e",
                            fontSize = YunJiUiTokens.PrimaryActionTextSize,
                            fontWeight = FontWeight.Medium,
                            color = colors.primaryText
                        )
                        Text(
                            text = locationStatusText(locationUiState.status),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.secondaryText
                        )
                    }
                    Switch(
                        checked = useCurrentLocation,
                        enabled = !locationUiState.isBusy,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = colors.primaryButton,
                            uncheckedThumbColor = colors.switchThumb,
                            uncheckedTrackColor = colors.switchTrack,
                            uncheckedBorderColor = colors.switchBorder,
                            disabledCheckedThumbColor = Color.White.copy(alpha = 0.72f),
                            disabledCheckedTrackColor = colors.primaryButton.copy(alpha = 0.52f),
                            disabledUncheckedThumbColor = colors.switchThumb.copy(alpha = 0.52f),
                            disabledUncheckedTrackColor = colors.switchTrack.copy(alpha = 0.52f),
                            disabledUncheckedBorderColor = colors.switchBorder.copy(alpha = 0.52f)
                        ),
                        onCheckedChange = { enabled ->
                            useCurrentLocation = enabled
                            if (enabled) {
                                onRequestLocation()
                            }
                        }
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(YunJiUiTokens.PrimaryButtonHeight),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryButton,
                    contentColor = Color.White
                ),
                onClick = { onRequestWidget(selectedMode) }
            ) {
                Text(
                    text = "\u6dfb\u52a0\u81f3\u684c\u9762",
                    fontSize = YunJiUiTokens.PrimaryActionTextSize,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun DesktopWeatherBackdrop(
    visualThemeKey: String,
    backgroundBrush: Brush,
    customBackground: CustomThemeImage,
    colors: DesktopWeatherColors
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .background(backgroundBrush)
    ) {
        when {
            visualThemeKey == VisualThemeUtils.THEME_CUSTOM_1 && customBackground.uri.isNotBlank() -> {
                UriImage(
                    uriString = customBackground.uri,
                    cropAnchor = customBackground.cropAnchor,
                    mediaType = customBackground.mediaType,
                    modifier = Modifier.fillMaxSize()
                )
            }
            visualThemeKey == VisualThemeUtils.THEME_PANORAMA -> {
                Image(
                    painter = painterResource(R.drawable.theme_panorama_day),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.82f),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.overlayTop,
                            colors.overlayMiddle,
                            colors.overlayBottom
                        )
                    )
                )
        )
    }
}

@Composable
private fun rememberDesktopWeatherColors(darkPalette: Boolean): DesktopWeatherColors {
    return if (darkPalette) {
        DesktopWeatherColors(
            background = Color(0xFF102A43),
            overlayTop = Color(0x8836455A),
            overlayMiddle = Color(0xAA163A5E),
            overlayBottom = Color(0xD0163A5E),
            primaryText = Color.White,
            secondaryText = Color.White.copy(alpha = 0.66f),
            cardContainer = Color.White.copy(alpha = 0.14f),
            primaryButton = Color(0xFF2D7DF6),
            tabSelectedText = Color.White,
            tabUnselectedText = Color.White.copy(alpha = 0.62f),
            dotActive = Color.White,
            dotInactive = Color.White.copy(alpha = 0.34f),
            switchThumb = Color(0xFFD8D2DD),
            switchTrack = Color(0x663C3443),
            switchBorder = Color.White.copy(alpha = 0.40f)
        )
    } else {
        DesktopWeatherColors(
            background = Color(0xFF2F6EA9),
            overlayTop = Color(0x3338536B),
            overlayMiddle = Color(0x552B72AC),
            overlayBottom = Color(0x882B72AC),
            primaryText = Color(0xFF102A37),
            secondaryText = Color(0xB3102A37),
            cardContainer = Color.White.copy(alpha = 0.42f),
            primaryButton = Color(0xFF2D7DF6),
            tabSelectedText = Color(0xFF102A37),
            tabUnselectedText = Color(0x80102A37),
            dotActive = Color(0xCC102A37),
            dotInactive = Color(0x33102A37),
            switchThumb = Color(0xFF756F7B),
            switchTrack = Color.White.copy(alpha = 0.42f),
            switchBorder = Color(0x66102A37)
        )
    }
}

private data class DesktopWeatherColors(
    val background: Color,
    val overlayTop: Color,
    val overlayMiddle: Color,
    val overlayBottom: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val cardContainer: Color,
    val primaryButton: Color,
    val tabSelectedText: Color,
    val tabUnselectedText: Color,
    val dotActive: Color,
    val dotInactive: Color,
    val switchThumb: Color,
    val switchTrack: Color,
    val switchBorder: Color
)

private fun customWidgetBackground(
    visualThemeKey: String,
    customThemeOptions: com.litroenade.yunjiweather.ui.compose.theme.CustomThemeOptions,
    homeWeatherData: HomeWeatherData?,
    minuteOfDay: Int
): CustomThemeImage {
    if (visualThemeKey != VisualThemeUtils.THEME_CUSTOM_1) {
        return CustomThemeImage()
    }
    val resolvedAsset = homeWeatherData?.let { data ->
        WeatherWidgetSnapshotFactory.customBackgroundForHomeWeather(
            visualThemeKey,
            customThemeOptions.profile,
            data,
            minuteOfDay
        )
    } ?: WeatherWidgetSnapshotFactory.customBackgroundForFallback(
        visualThemeKey,
        customThemeOptions.profile,
        minuteOfDay
    )
    if (!resolvedAsset.isEmpty) {
        return CustomThemeImage(
            assetId = resolvedAsset.id,
            uri = resolvedAsset.uri,
            cropAnchor = resolvedAsset.cropAnchor,
            mediaType = resolvedAsset.mediaType
        )
    }
    return CustomThemeImage(
        uri = customThemeOptions.imageUri,
        cropAnchor = customThemeOptions.cropAnchor,
        mediaType = if (customThemeOptions.imageUri.trim().lowercase().endsWith(".gif")) {
            CustomThemeAsset.MEDIA_GIF
        } else {
            CustomThemeAsset.MEDIA_IMAGE
        }
    )
}

private fun currentWidgetMinuteOfDay(): Int {
    val calendar = Calendar.getInstance()
    return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
}

private fun CustomThemeImage.toCustomThemeAsset(): CustomThemeAsset {
    if (uri.isBlank()) {
        return CustomThemeAsset.empty()
    }
    return CustomThemeAsset(
        assetId.ifBlank { "widget-preview" },
        uri,
        mediaType,
        cropAnchor,
        ""
    )
}

@Composable
internal fun WidgetPreviewBackground(
    snapshot: WeatherWidgetSnapshot,
    customBackground: CustomThemeImage
) {
    if (customBackground.uri.isBlank()) {
        Image(
            painter = painterResource(widgetBackgroundResId(snapshot.visualThemeKey, snapshot.iconCode)),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else {
        UriImage(
            uriString = customBackground.uri,
            cropAnchor = customBackground.cropAnchor,
            mediaType = customBackground.mediaType,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun DesktopWidgetPreview(
    selectedMode: WeatherWidgetLayoutMode,
    snapshot: WeatherWidgetSnapshot,
    customBackground: CustomThemeImage
) {
    when (selectedMode.normalizedWidgetMode()) {
        WeatherWidgetLayoutMode.COMPACT -> CompactWidgetPreview(snapshot, customBackground)
        WeatherWidgetLayoutMode.AUTO,
        WeatherWidgetLayoutMode.STANDARD -> StandardWidgetPreview(snapshot, customBackground)
        WeatherWidgetLayoutMode.EXPANDED -> LifeAdviceWidgetPreview(snapshot, customBackground)
    }
}

@Composable
private fun WidgetModeTabs(
    selectedMode: WeatherWidgetLayoutMode,
    colors: DesktopWeatherColors,
    onModeSelected: (WeatherWidgetLayoutMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = YunJiUiTokens.PageHeaderVerticalPadding),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        WidgetModeTab("\u5929\u6c14\u65f6\u949f", WeatherWidgetLayoutMode.COMPACT, selectedMode, colors, onModeSelected)
        WidgetModeTab("\u57fa\u7840\u5929\u6c14", WeatherWidgetLayoutMode.STANDARD, selectedMode, colors, onModeSelected)
        WidgetModeTab("\u751f\u6d3b\u5efa\u8bae", WeatherWidgetLayoutMode.EXPANDED, selectedMode, colors, onModeSelected)
    }
}

private fun widgetBackgroundResId(visualThemeKey: String, iconCode: String): Int {
    if (visualThemeKey != VisualThemeUtils.THEME_PANORAMA) {
        return R.drawable.widget_weather_background
    }
    return when (WeatherIconUtils.getWeatherCategory(iconCode)) {
        WeatherIconUtils.WeatherCategory.RAIN -> R.drawable.theme_panorama_rain
        WeatherIconUtils.WeatherCategory.SNOW -> R.drawable.theme_panorama_snow
        WeatherIconUtils.WeatherCategory.NIGHT -> R.drawable.theme_panorama_night
        else -> R.drawable.theme_panorama_day
    }
}

@Composable
private fun WidgetModeTab(
    title: String,
    mode: WeatherWidgetLayoutMode,
    selectedMode: WeatherWidgetLayoutMode,
    colors: DesktopWeatherColors,
    onModeSelected: (WeatherWidgetLayoutMode) -> Unit
) {
    Column(
        modifier = Modifier.clickable { onModeSelected(mode) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            text = title,
            color = if (mode == selectedMode) colors.tabSelectedText else colors.tabUnselectedText,
            fontSize = YunJiUiTokens.PageTabTextSize,
            fontWeight = if (mode == selectedMode) FontWeight.SemiBold else FontWeight.Normal
        )
        Box(
            modifier = Modifier
                .size(width = 74.dp, height = 3.dp)
                .background(
                    if (mode == selectedMode) colors.tabSelectedText else Color.Transparent,
                    RoundedCornerShape(2.dp)
                )
                .padding(top = 4.dp)
        )
    }
}

@Composable
internal fun LifeAdviceWidgetPreview(
    snapshot: WeatherWidgetSnapshot,
    customBackground: CustomThemeImage
) {
    val spec = WidgetStyleSpec.forMode(WeatherWidgetLayoutMode.EXPANDED)
    Surface(
        modifier = Modifier
            .width(spec.previewWidthDp.dp)
            .height(spec.previewHeightDp.dp),
        shape = RoundedCornerShape(spec.cornerRadiusDp.dp),
        color = Color.White.copy(alpha = 0.20f)
    ) {
        Box {
            WidgetPreviewBackground(snapshot, customBackground)
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0x664AA0E0))
            )
            Image(
                painter = painterResource(widgetPreviewIconResId(snapshot)),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(18.dp)
                    .size(32.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spec.contentPaddingDp.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(0.85f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(snapshot.cityName, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text(snapshot.temperatureText, color = Color.White, fontSize = spec.temperatureTextSizeSp.sp, fontWeight = FontWeight.Light)
                    Text(snapshot.rangeText, color = Color.White.copy(alpha = 0.86f), fontSize = 13.sp)
                }
                Column(
                    modifier = Modifier
                        .weight(1.35f)
                        .padding(start = 12.dp, end = 34.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(snapshot.conditionText, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text(snapshot.updateText, color = Color.White.copy(alpha = 0.86f), fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LifeIndexColumn(snapshot.clothingValue, "\u7a7f\u8863")
                        LifeIndexColumn(snapshot.fishingValue, "\u9493\u9c7c")
                        LifeIndexColumn(snapshot.sunsetValue, "\u665a\u971e")
                        LifeIndexColumn(snapshot.coldValue, "\u611f\u5192")
                    }
                    Text(snapshot.adviceText, color = Color.White.copy(alpha = 0.84f), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
internal fun StandardWidgetPreview(
    snapshot: WeatherWidgetSnapshot,
    customBackground: CustomThemeImage
) {
    val spec = WidgetStyleSpec.forMode(WeatherWidgetLayoutMode.STANDARD)
    Surface(
        modifier = Modifier
            .width(spec.previewWidthDp.dp)
            .height(spec.previewHeightDp.dp),
        shape = RoundedCornerShape(spec.cornerRadiusDp.dp),
        color = Color(0xAA68B8F2)
    ) {
        Box {
            WidgetPreviewBackground(snapshot, customBackground)
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0x664AA0E0))
            )
            Image(
                painter = painterResource(widgetPreviewIconResId(snapshot)),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(18.dp)
                    .size(32.dp)
            )
            Column(
                modifier = Modifier.padding(spec.contentPaddingDp.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(snapshot.cityName, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text(snapshot.temperatureText, color = Color.White, fontSize = spec.temperatureTextSizeSp.sp, fontWeight = FontWeight.Light)
                Text(snapshot.rangeText, color = Color.White.copy(alpha = 0.86f), fontSize = 14.sp)
                Text(snapshot.conditionText, color = Color.White, fontSize = 16.sp)
                Text(snapshot.updateText, color = Color.White.copy(alpha = 0.80f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
internal fun CompactWidgetPreview(
    snapshot: WeatherWidgetSnapshot,
    customBackground: CustomThemeImage
) {
    val spec = WidgetStyleSpec.forMode(WeatherWidgetLayoutMode.COMPACT)
    Surface(
        modifier = Modifier
            .width(spec.previewWidthDp.dp)
            .height(spec.previewHeightDp.dp),
        shape = RoundedCornerShape(spec.cornerRadiusDp.dp),
        color = Color(0xAA5FABE8)
    ) {
        Box {
            WidgetPreviewBackground(snapshot, customBackground)
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0x664AA0E0))
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spec.contentPaddingDp.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = snapshot.temperatureText,
                    color = Color.White,
                    fontSize = spec.temperatureTextSizeSp.sp,
                    fontWeight = FontWeight.Light
                )
                Column(
                    modifier = Modifier.weight(0.95f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(snapshot.cityName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(snapshot.conditionText, color = Color.White.copy(alpha = 0.95f), fontSize = 10.sp)
                    Text(snapshot.rangeText, color = Color.White.copy(alpha = 0.88f), fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun LifeIndexColumn(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(label, color = Color.White.copy(alpha = 0.70f), fontSize = 9.sp)
    }
}
@Composable
private fun PageDots(active: Int, colors: DesktopWeatherColors) {
    Row(
        modifier = Modifier.padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(width = if (index == active) 24.dp else 8.dp, height = 8.dp)
                    .background(
                        if (index == active) colors.dotActive else colors.dotInactive,
                        RoundedCornerShape(8.dp)
                    )
            )
        }
    }
}

private fun WeatherWidgetLayoutMode.ordinalIndex(): Int {
    return when (this) {
        WeatherWidgetLayoutMode.AUTO -> 1
        WeatherWidgetLayoutMode.COMPACT -> 0
        WeatherWidgetLayoutMode.STANDARD -> 1
        WeatherWidgetLayoutMode.EXPANDED -> 2
    }
}

private fun WeatherWidgetLayoutMode.normalizedWidgetMode(): WeatherWidgetLayoutMode {
    return if (this == WeatherWidgetLayoutMode.AUTO) WeatherWidgetLayoutMode.STANDARD else this
}

private fun WeatherWidgetLayoutMode.widgetModeAtOffset(offset: Int): WeatherWidgetLayoutMode {
    val currentIndex = normalizedWidgetMode().ordinalIndex()
    val nextIndex = (currentIndex + offset + DesktopWidgetModes.size) % DesktopWidgetModes.size
    return DesktopWidgetModes[nextIndex]
}

private fun WeatherWidgetLayoutMode.widgetModeTitle(): String {
    return when (normalizedWidgetMode()) {
        WeatherWidgetLayoutMode.COMPACT -> "\u5929\u6c14\u65f6\u949f"
        WeatherWidgetLayoutMode.AUTO,
        WeatherWidgetLayoutMode.STANDARD -> "\u57fa\u7840\u5929\u6c14"
        WeatherWidgetLayoutMode.EXPANDED -> "\u751f\u6d3b\u5efa\u8bae"
    }
}

private fun locationStatusText(status: LocationStatus): String {
    return when (status) {
        LocationStatus.REQUESTING_PERMISSION -> "\u6b63\u5728\u8bf7\u6c42\u5b9a\u4f4d\u6743\u9650"
        LocationStatus.FETCHING_LOCATION -> "\u6b63\u5728\u83b7\u53d6\u5b9e\u65f6\u4f4d\u7f6e"
        LocationStatus.SUCCESS -> "\u5df2\u4f7f\u7528\u5f53\u524d\u4f4d\u7f6e\u5929\u6c14"
        LocationStatus.DENIED -> "\u672a\u6388\u6743\u5b9a\u4f4d\uff0c\u5f00\u542f\u540e\u5c06\u8bf7\u6c42\u6743\u9650"
        LocationStatus.ERROR -> "\u5b9a\u4f4d\u5931\u8d25\uff0c\u53ef\u91cd\u65b0\u5f00\u542f"
        LocationStatus.IDLE -> "\u5f00\u542f\u540e\u5c06\u540c\u6b65\u5f53\u524d\u4f4d\u7f6e\u5929\u6c14"
    }
}

private fun widgetPreviewIconResId(snapshot: WeatherWidgetSnapshot): Int {
    return when {
        snapshot.iconCode.startsWith("4") || snapshot.conditionText.contains("\u96ea") -> R.drawable.ic_weather_snow
        snapshot.iconCode.startsWith("3") ||
                snapshot.conditionText.contains("\u96e8") ||
                snapshot.conditionText.contains("\u96f7") -> R.drawable.ic_weather_rain
        snapshot.iconCode.startsWith("15") ||
                snapshot.conditionText.contains("\u4e91") ||
                snapshot.conditionText.contains("\u9634") -> R.drawable.ic_weather_cloudy
        else -> R.drawable.ic_weather_sunny
    }
}
