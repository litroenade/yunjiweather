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
import com.litroenade.yunjiweather.ui.compose.UriImage
import com.litroenade.yunjiweather.ui.compose.theme.CustomThemeImage
import com.litroenade.yunjiweather.ui.compose.theme.LocalCustomThemeOptions
import com.litroenade.yunjiweather.ui.compose.theme.LocalYunJiVisualTheme
import com.litroenade.yunjiweather.ui.compose.theme.YunJiUiTokens
import com.litroenade.yunjiweather.ui.location.LocationStatus
import com.litroenade.yunjiweather.ui.location.LocationUiState
import com.litroenade.yunjiweather.utils.VisualThemeUtils
import com.litroenade.yunjiweather.widget.WeatherWidgetLayoutMode
import com.litroenade.yunjiweather.widget.WidgetStyleSpec
import kotlin.math.abs

private val DesktopWidgetModes = listOf(
    WeatherWidgetLayoutMode.COMPACT,
    WeatherWidgetLayoutMode.STANDARD,
    WeatherWidgetLayoutMode.EXPANDED
)

@Composable
fun DesktopWeatherScreen(
    modifier: Modifier = Modifier,
    locationUiState: LocationUiState = LocationUiState.idle(),
    onRequestLocation: () -> Unit = {},
    onRequestWidget: (WeatherWidgetLayoutMode) -> Unit
) {
    var selectedMode by rememberSaveable { mutableStateOf(WeatherWidgetLayoutMode.EXPANDED) }
    var widgetSwitchDirection by rememberSaveable { mutableStateOf(1) }
    var horizontalSwipeDistance by remember { mutableStateOf(0f) }
    var useCurrentLocation by rememberSaveable { mutableStateOf(locationUiState.status == LocationStatus.SUCCESS) }
    val visualTheme = LocalYunJiVisualTheme.current
    val customThemeOptions = LocalCustomThemeOptions.current
    val customWidgetBackground = customWidgetBackground(visualTheme.key, customThemeOptions)
    val colors = rememberDesktopWeatherColors(visualTheme.background.luminance() < 0.35f)
    val swipeThresholdPx = with(LocalDensity.current) { 56.dp.toPx() }
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
            },
            onHorizontalDrag = { _, dragAmount ->
                horizontalSwipeDistance += dragAmount
            },
            onDragCancel = {
                horizontalSwipeDistance = 0f
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
                onModeSelected = ::selectWidgetMode
            )
            Spacer(Modifier.weight(0.72f))
            AnimatedContent(
                modifier = widgetSwipeModifier,
                targetState = selectedMode,
                transitionSpec = {
                    val direction = widgetSwitchDirection
                    val enter = slideInHorizontally(
                        animationSpec = tween(260, easing = FastOutSlowInEasing)
                    ) { width -> width * direction / 3 } + fadeIn(
                        animationSpec = tween(160, easing = FastOutSlowInEasing)
                    )
                    val exit = slideOutHorizontally(
                        animationSpec = tween(220, easing = FastOutSlowInEasing)
                    ) { width -> -width * direction / 3 } + fadeOut(
                        animationSpec = tween(140, easing = FastOutSlowInEasing)
                    )
                    (enter togetherWith exit).using(SizeTransform(clip = false))
                },
                label = "desktop-widget-mode"
            ) { animatedMode ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DesktopWidgetPreview(
                        selectedMode = animatedMode,
                        customBackground = customWidgetBackground
                    )
                    Spacer(Modifier.height(48.dp))
                    Text(
                        text = animatedMode.widgetModeTitle(),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.secondaryText
                    )
                    PageDots(active = animatedMode.ordinalIndex())
                }
            }
            Spacer(Modifier.height(34.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "打开当前位置，展示实时位置天气",
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
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "当前位置",
                        fontSize = YunJiUiTokens.PrimaryActionTextSize,
                        fontWeight = FontWeight.Medium,
                        color = colors.primaryText
                    )
                    Switch(
                        checked = useCurrentLocation,
                        enabled = !locationUiState.isBusy,
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
                    text = "添加至桌面",
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
            primaryButton = Color(0xFF2D7DF6)
        )
    } else {
        DesktopWeatherColors(
            background = Color(0xFF2F6EA9),
            overlayTop = Color(0x3338536B),
            overlayMiddle = Color(0x552B72AC),
            overlayBottom = Color(0x882B72AC),
            primaryText = Color.White,
            secondaryText = Color.White.copy(alpha = 0.70f),
            cardContainer = Color.White.copy(alpha = 0.20f),
            primaryButton = Color(0xFF2D7DF6)
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
    val primaryButton: Color
)

private fun customWidgetBackground(
    visualThemeKey: String,
    customThemeOptions: com.litroenade.yunjiweather.ui.compose.theme.CustomThemeOptions
): CustomThemeImage {
    if (visualThemeKey != VisualThemeUtils.THEME_CUSTOM_1) {
        return CustomThemeImage()
    }
    val firstAsset = customThemeOptions.profile.assets.firstOrNull { asset -> !asset.isEmpty }
    if (firstAsset != null) {
        return CustomThemeImage(
            assetId = firstAsset.id,
            uri = firstAsset.uri,
            cropAnchor = firstAsset.cropAnchor,
            mediaType = firstAsset.mediaType
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

@Composable
internal fun WidgetPreviewBackground(customBackground: CustomThemeImage) {
    if (customBackground.uri.isBlank()) {
        Image(
            painter = painterResource(R.drawable.theme_panorama_day),
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
    customBackground: CustomThemeImage
) {
    when (selectedMode.normalizedWidgetMode()) {
        WeatherWidgetLayoutMode.COMPACT -> CompactWidgetPreview(customBackground)
        WeatherWidgetLayoutMode.AUTO,
        WeatherWidgetLayoutMode.STANDARD -> StandardWidgetPreview(customBackground)
        WeatherWidgetLayoutMode.EXPANDED -> LifeAdviceWidgetPreview(customBackground)
    }
}

@Composable
private fun WidgetModeTabs(
    selectedMode: WeatherWidgetLayoutMode,
    onModeSelected: (WeatherWidgetLayoutMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = YunJiUiTokens.PageHeaderVerticalPadding),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        WidgetModeTab("天气时钟", WeatherWidgetLayoutMode.COMPACT, selectedMode, onModeSelected)
        WidgetModeTab("基础天气", WeatherWidgetLayoutMode.STANDARD, selectedMode, onModeSelected)
        WidgetModeTab("生活建议", WeatherWidgetLayoutMode.EXPANDED, selectedMode, onModeSelected)
    }
}

@Composable
private fun WidgetModeTab(
    title: String,
    mode: WeatherWidgetLayoutMode,
    selectedMode: WeatherWidgetLayoutMode,
    onModeSelected: (WeatherWidgetLayoutMode) -> Unit
) {
    Column(
        modifier = Modifier.clickable { onModeSelected(mode) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            text = title,
            color = if (mode == selectedMode) Color.White else Color.White.copy(alpha = 0.36f),
            fontSize = YunJiUiTokens.PageTabTextSize,
            fontWeight = if (mode == selectedMode) FontWeight.SemiBold else FontWeight.Normal
        )
        Box(
            modifier = Modifier
                .size(width = 74.dp, height = 3.dp)
                .background(
                    if (mode == selectedMode) Color.White else Color.Transparent,
                    RoundedCornerShape(2.dp)
                )
                .padding(top = 4.dp)
        )
    }
}

@Composable
internal fun LifeAdviceWidgetPreview(customBackground: CustomThemeImage) {
    val spec = WidgetStyleSpec.forMode(WeatherWidgetLayoutMode.EXPANDED)
    Surface(
        modifier = Modifier
            .width(spec.previewWidthDp.dp)
            .height(spec.previewHeightDp.dp),
        shape = RoundedCornerShape(spec.cornerRadiusDp.dp),
        color = Color.White.copy(alpha = 0.20f)
    ) {
        Box {
            WidgetPreviewBackground(customBackground)
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0x664AA0E0))
            )
            Image(
                painter = painterResource(R.drawable.ic_weather_cloudy),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text("北京", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text("25°", color = Color.White, fontSize = spec.temperatureTextSizeSp.sp, fontWeight = FontWeight.Light)
                    Text("35° / 25°", color = Color.White.copy(alpha = 0.86f), fontSize = 13.sp)
                }
                Column(
                    modifier = Modifier.padding(end = 42.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("阴  空气良", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text("2026-05-31 07:59", color = Color.White.copy(alpha = 0.86f), fontSize = 12.sp)
                    Text("短袖   适宜   一般   不易", color = Color.White, fontSize = 14.sp)
                    Text("穿衣     钓鱼     晚霞     感冒", color = Color.White.copy(alpha = 0.70f), fontSize = 10.sp)
                    Text("出行注意防晒，及时补水", color = Color.White.copy(alpha = 0.84f), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
internal fun StandardWidgetPreview(customBackground: CustomThemeImage) {
    val spec = WidgetStyleSpec.forMode(WeatherWidgetLayoutMode.STANDARD)
    Surface(
        modifier = Modifier
            .width(spec.previewWidthDp.dp)
            .height(spec.previewHeightDp.dp),
        shape = RoundedCornerShape(spec.cornerRadiusDp.dp),
        color = Color(0xAA68B8F2)
    ) {
        Box {
            WidgetPreviewBackground(customBackground)
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0x664AA0E0))
            )
            Image(
                painter = painterResource(R.drawable.ic_weather_cloudy),
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
                Text("北京", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text("25°", color = Color.White, fontSize = spec.temperatureTextSizeSp.sp, fontWeight = FontWeight.Light)
                Text("35° / 25°", color = Color.White.copy(alpha = 0.86f), fontSize = 14.sp)
                Text("阴", color = Color.White, fontSize = 16.sp)
                Text("2026-05-31 07:59", color = Color.White.copy(alpha = 0.80f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
internal fun CompactWidgetPreview(customBackground: CustomThemeImage) {
    val spec = WidgetStyleSpec.forMode(WeatherWidgetLayoutMode.COMPACT)
    Surface(
        modifier = Modifier
            .width(spec.previewWidthDp.dp)
            .height(spec.previewHeightDp.dp),
        shape = RoundedCornerShape(spec.cornerRadiusDp.dp),
        color = Color(0xAA5FABE8)
    ) {
        Box {
            WidgetPreviewBackground(customBackground)
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
                    text = "25°",
                    color = Color.White,
                    fontSize = spec.temperatureTextSizeSp.sp,
                    fontWeight = FontWeight.Light
                )
                Column(
                    modifier = Modifier.weight(0.95f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("北京", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("阴", color = Color.White.copy(alpha = 0.95f), fontSize = 10.sp)
                    Text("35° / 25°", color = Color.White.copy(alpha = 0.88f), fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun PageDots(active: Int) {
    Row(
        modifier = Modifier.padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(width = if (index == active) 24.dp else 8.dp, height = 8.dp)
                    .background(
                        if (index == active) Color.White else Color.White.copy(alpha = 0.16f),
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
        WeatherWidgetLayoutMode.COMPACT -> "天气时钟"
        WeatherWidgetLayoutMode.AUTO,
        WeatherWidgetLayoutMode.STANDARD -> "基础天气"
        WeatherWidgetLayoutMode.EXPANDED -> "生活建议"
    }
}
