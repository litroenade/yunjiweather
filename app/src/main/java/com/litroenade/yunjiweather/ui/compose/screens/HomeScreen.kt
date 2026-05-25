package com.litroenade.yunjiweather.ui.compose.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.litroenade.yunjiweather.R
import com.litroenade.yunjiweather.common.UiState
import com.litroenade.yunjiweather.data.model.HomeWeatherData
import com.litroenade.yunjiweather.data.model.WeatherDailyData
import com.litroenade.yunjiweather.data.model.WeatherHourlyData
import com.litroenade.yunjiweather.ui.compose.InfoCard
import com.litroenade.yunjiweather.ui.compose.MessageCard
import com.litroenade.yunjiweather.ui.compose.MetricTile
import com.litroenade.yunjiweather.ui.compose.SectionTitle
import com.litroenade.yunjiweather.ui.compose.WeatherAnimation
import com.litroenade.yunjiweather.ui.compose.formatWeatherTime
import com.litroenade.yunjiweather.ui.compose.theme.LocalYunJiVisualTheme
import com.litroenade.yunjiweather.ui.home.HomeViewModel
import com.litroenade.yunjiweather.ui.home.WeatherAnimationType
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils
import com.litroenade.yunjiweather.utils.WeatherIconUtils

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    animationEnabled: Boolean = true,
    temperatureUnit: String = WeatherDisplayUtils.TEMPERATURE_CELSIUS,
    windUnit: String = WeatherDisplayUtils.WIND_SCALE,
    onManageCities: () -> Unit = {},
    onSearchCity: () -> Unit = {},
    onSettings: () -> Unit = {},
    onDesktopWeather: () -> Unit = {},
    onPersonalize: () -> Unit = {},
    onOpenAlerts: () -> Unit = {},
    onOpenLifeIndex: () -> Unit = {},
    onFeedbackWeather: () -> Unit = {},
    onShareWeather: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.getUiState().observeAsState()
    val message by viewModel.getMessage().observeAsState("")
    LaunchedEffect(Unit) {
        viewModel.loadHomeWeather()
    }

    val weatherData = uiState?.data
    val headerCityName = weatherData?.cityName ?: "云迹天气"
    val headerConditionText = message.ifBlank { weatherData?.condition ?: "实时天气" }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(weatherBackground(weatherData?.iconCode))
    ) {
        if (animationEnabled && weatherData?.iconCode != null) {
            WeatherAnimation(
                iconCode = weatherData.iconCode,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.16f)
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 18.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                WeatherTopActions(
                    cityName = headerCityName,
                    conditionText = headerConditionText,
                    onManageCities = onManageCities,
                    onSearchCity = onSearchCity,
                    onSettings = onSettings,
                    onDesktopWeather = onDesktopWeather,
                    onPersonalize = onPersonalize,
                    onOpenAlerts = onOpenAlerts,
                    onOpenLifeIndex = onOpenLifeIndex,
                    onFeedbackWeather = onFeedbackWeather,
                    onShareWeather = onShareWeather
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
                            item {
                                CurrentWeatherSection(
                                    data = data,
                                    state = state,
                                    animationEnabled = animationEnabled,
                                    temperatureUnit = temperatureUnit
                                )
                            }
                            item { WeatherMetricPanel(data, temperatureUnit, windUnit) }
                            item { AdvicePanel(data) }
                            item { FeatureEntryPanel(onOpenAlerts, onOpenLifeIndex) }
                            item { HourlyForecast(data.hourlyForecasts, temperatureUnit) }
                            item { DailyForecast(data.dailyForecasts, temperatureUnit) }
                        }
                    }
                }
            }
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
    temperatureUnit: String
) {
    val visualTheme = LocalYunJiVisualTheme.current
    val configuration = LocalConfiguration.current
    val compact = configuration.screenHeightDp < 760
    val currentWeatherHeight = if (compact) 292.dp else 330.dp
    val animationWidth = if (compact) 176.dp else 210.dp
    val animationHeight = if (compact) 142.dp else 170.dp
    val animationTop = if (compact) 34.dp else 44.dp
    val temperatureSize = if (compact) 76.sp else 86.sp
    val temperatureLineHeight = if (compact) 76.sp else 86.sp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(currentWeatherHeight)
    ) {
        if (animationEnabled) {
            WeatherAnimation(
                iconCode = data.iconCode,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = animationTop)
                    .width(animationWidth)
                    .height(animationHeight)
                    .alpha(0.92f)
            )
        } else {
            Image(
                painter = painterResource(WeatherIconUtils.getWeatherIconRes(data.iconCode)),
                contentDescription = data.condition,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 58.dp, end = 18.dp)
                    .size(124.dp)
                    .alpha(0.72f),
                colorFilter = ColorFilter.tint(visualTheme.primaryWeatherText.copy(alpha = 0.72f))
            )
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
                    color = visualTheme.primaryWeatherText,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatTemperatureDisplay(data.tempMax, temperatureUnit)} / ${formatTemperatureDisplay(data.tempMin, temperatureUnit)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = visualTheme.primaryWeatherText,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${data.condition}  空气${data.airQualityCategory}",
                    style = MaterialTheme.typography.titleSmall,
                    color = visualTheme.secondaryWeatherText,
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
                color = visualTheme.secondaryWeatherText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WeatherTopActions(
    cityName: String,
    conditionText: String,
    onManageCities: () -> Unit,
    onSearchCity: () -> Unit,
    onSettings: () -> Unit,
    onDesktopWeather: () -> Unit,
    onPersonalize: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenLifeIndex: () -> Unit,
    onFeedbackWeather: () -> Unit,
    onShareWeather: () -> Unit
) {
    val visualTheme = LocalYunJiVisualTheme.current
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WeatherTopIcon(
            iconRes = R.drawable.ic_city_black_24dp,
            contentDescription = "管理城市",
            onClick = onManageCities
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = cityName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = visualTheme.primaryWeatherText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = conditionText,
                style = MaterialTheme.typography.bodySmall,
                color = visualTheme.secondaryWeatherText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            WeatherTopIcon(
                iconRes = R.drawable.ic_search_24,
                contentDescription = "搜索城市",
                onClick = onSearchCity
            )
            Box {
                WeatherTopIcon(
                    iconRes = R.drawable.ic_more_vertical_24,
                    contentDescription = "更多",
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
                        text = { Text("个性换肤") },
                        onClick = {
                            expanded = false
                            onPersonalize()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("天气预警") },
                        onClick = {
                            expanded = false
                            onOpenAlerts()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("生活指数") },
                        onClick = {
                            expanded = false
                            onOpenLifeIndex()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("反馈当前天气") },
                        onClick = {
                            expanded = false
                            onFeedbackWeather()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("分享") },
                        onClick = {
                            expanded = false
                            onShareWeather()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("我的") },
                        onClick = {
                            expanded = false
                            onSettings()
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
private fun FeatureEntryPanel(
    onOpenAlerts: () -> Unit,
    onOpenLifeIndex: () -> Unit
) {
    InfoCard {
        SectionTitle("更多天气服务")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureEntryTile(
                title = "天气预警",
                subtitle = "查看当前城市官方预警",
                onClick = onOpenAlerts,
                modifier = Modifier.weight(1f)
            )
            FeatureEntryTile(
                title = "生活指数",
                subtitle = "穿衣、出行、运动建议",
                onClick = onOpenLifeIndex,
                modifier = Modifier.weight(1f)
            )
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
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.56f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f))
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
    onClick: () -> Unit
) {
    val visualTheme = LocalYunJiVisualTheme.current
    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
    ) {
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                tint = visualTheme.primaryWeatherText,
                modifier = Modifier.size(22.dp)
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
private fun AirQualitySummary(data: HomeWeatherData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
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
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 2.dp)
        ) {
            items(forecasts.take(12)) { item ->
                HourlyForecastCard(item, temperatureUnit)
            }
        }
    }
}

@Composable
private fun HourlyForecastCard(item: WeatherHourlyData, temperatureUnit: String) {
    Surface(
        modifier = Modifier.width(92.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.58f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                modifier = Modifier.size(28.dp),
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
}

@Composable
private fun DailyForecast(items: List<WeatherDailyData>, temperatureUnit: String) {
    InfoCard {
        SectionTitle("未来几天")
        if (items.isEmpty()) {
            Text("暂无日预报数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@InfoCard
        }
        items.take(7).forEach { item ->
            DailyForecastRow(item, temperatureUnit)
        }
    }
}

@Composable
private fun DailyForecastRow(item: WeatherDailyData, temperatureUnit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.dateText,
            modifier = Modifier.weight(0.24f),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Image(
            painter = painterResource(WeatherIconUtils.getWeatherIconRes(item.iconCode)),
            contentDescription = item.condition,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )
        Text(
            text = item.condition,
            modifier = Modifier.weight(0.28f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        TemperatureTrend(
            modifier = Modifier.weight(0.30f),
            tempMin = item.tempMin,
            tempMax = item.tempMax
        )
        Text(
            text = "${formatTemperatureDisplay(item.tempMin, temperatureUnit)} / ${formatTemperatureDisplay(item.tempMax, temperatureUnit)}",
            modifier = Modifier.weight(0.28f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TemperatureTrend(
    tempMin: String,
    tempMax: String,
    modifier: Modifier = Modifier
) {
    val min = tempMin.toFloatOrNull()
    val max = tempMax.toFloatOrNull()
    if (min == null || max == null) {
        Box(
            modifier = modifier
                .height(6.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
                    shape = RoundedCornerShape(99.dp)
                )
        )
        return
    }
    val widthFraction = ((max - min).coerceIn(2f, 16f) / 16f).coerceIn(0.28f, 1f)
    Box(
        modifier = modifier
            .height(6.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
                shape = RoundedCornerShape(99.dp)
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(widthFraction)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF75B7D9), Color(0xFFFFC65A))
                    ),
                    shape = RoundedCornerShape(99.dp)
                )
        )
    }
}

private fun formatTemperatureDisplay(value: String, unit: String): String {
    return WeatherDisplayUtils.formatTemperature(value, unit)
}

private fun formatWindDisplay(data: HomeWeatherData, unit: String): String {
    return WeatherDisplayUtils.formatWind(data.windDir, data.windScale, data.windSpeed, unit)
}

@Composable
private fun weatherBackground(iconCode: String?): Brush {
    val visualTheme = LocalYunJiVisualTheme.current
    if (iconCode == null) {
        return Brush.verticalGradient(
            listOf(
                visualTheme.defaultWeatherGradient.top,
                visualTheme.defaultWeatherGradient.middle,
                visualTheme.defaultWeatherGradient.bottom
            )
        )
    }
    return when (WeatherAnimationType.fromIconCode(iconCode)) {
        WeatherAnimationType.SUNNY -> Brush.verticalGradient(
            listOf(Color(0xFF85C9F3), Color(0xFFF6DCA9), Color(0xFFF8F4EA))
        )
        WeatherAnimationType.CLOUDY -> Brush.verticalGradient(
            listOf(Color(0xFFB9D4DE), Color(0xFFDCE6E7), Color(0xFFF5F2E9))
        )
        WeatherAnimationType.RAIN -> Brush.verticalGradient(
            listOf(Color(0xFF7896A5), Color(0xFFB7C7CA), Color(0xFFF0F0E8))
        )
        WeatherAnimationType.SNOW -> Brush.verticalGradient(
            listOf(Color(0xFFBFD8EA), Color(0xFFEAF2F6), Color(0xFFF8F8F2))
        )
        WeatherAnimationType.NIGHT -> Brush.verticalGradient(
            listOf(Color(0xFF182D46), Color(0xFF405B75), Color(0xFFE8E5D8))
        )
    }
}
