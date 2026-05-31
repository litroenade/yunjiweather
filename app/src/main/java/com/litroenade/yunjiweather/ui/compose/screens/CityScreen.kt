package com.litroenade.yunjiweather.ui.compose.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.litroenade.yunjiweather.R
import com.litroenade.yunjiweather.data.entity.CityEntity
import com.litroenade.yunjiweather.data.model.CityWeatherSummary
import com.litroenade.yunjiweather.ui.city.CityViewModel
import com.litroenade.yunjiweather.ui.compose.formatWeatherTime
import com.litroenade.yunjiweather.ui.compose.theme.LocalYunJiVisualTheme
import com.litroenade.yunjiweather.ui.compose.theme.YunJiUiTokens
import com.litroenade.yunjiweather.ui.location.LocationStatus
import com.litroenade.yunjiweather.ui.location.LocationUiState
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils
import kotlinx.coroutines.delay

@Composable
fun CityScreen(
    modifier: Modifier = Modifier,
    temperatureUnit: String = WeatherDisplayUtils.TEMPERATURE_CELSIUS,
    respectStatusBar: Boolean = true,
    autoFocusSearch: Boolean = false,
    showHeader: Boolean = true,
    editing: Boolean = false,
    locationUiState: LocationUiState = LocationUiState.idle(),
    onDefaultCityChanged: () -> Unit = {},
    viewModel: CityViewModel = viewModel()
) {
    val cities by viewModel.cities.observeAsState(emptyList())
    val searchResults by viewModel.searchResults.observeAsState(emptyList())
    val summaries by viewModel.citySummaries.observeAsState(emptyMap())
    val defaultCity by viewModel.defaultCity.observeAsState("未设置")
    val message by viewModel.message.observeAsState("")
    val busy by viewModel.busy.observeAsState(false)
    val defaultCityChangeVersion by viewModel.defaultCityChangeVersion.observeAsState(0L)
    var query by rememberSaveable { mutableStateOf("") }
    var lastSubmittedQuery by rememberSaveable { mutableStateOf("") }
    var focusSearchToken by rememberSaveable { mutableStateOf(0) }
    val visualTheme = LocalYunJiVisualTheme.current
    val cityColors = rememberCityPageColors(visualTheme.background.luminance() < 0.35f)

    fun submitSearch() {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) {
            focusSearchToken += 1
            return
        }
        if (normalizedQuery == lastSubmittedQuery && searchResults.isNotEmpty()) {
            return
        }
        lastSubmittedQuery = normalizedQuery
        viewModel.searchCities(normalizedQuery)
    }

    LaunchedEffect(defaultCityChangeVersion) {
        if (defaultCityChangeVersion > 0L) {
            onDefaultCityChanged()
        }
    }
    LaunchedEffect(query, busy) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) {
            lastSubmittedQuery = ""
            return@LaunchedEffect
        }
        delay(360)
        if (!busy && normalizedQuery == query.trim() && normalizedQuery != lastSubmittedQuery) {
            lastSubmittedQuery = normalizedQuery
            viewModel.searchCities(normalizedQuery)
        }
    }

    val listModifier = if (respectStatusBar) {
        modifier.statusBarsPadding()
    } else {
        modifier
    }
    Box(
        modifier = listModifier
            .fillMaxSize()
            .background(cityColors.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = YunJiUiTokens.ScreenHorizontalPadding),
            contentPadding = PaddingValues(
                top = if (showHeader) {
                    YunJiUiTokens.PageHeaderVerticalPadding
                } else {
                    YunJiUiTokens.ImmersiveContentTopPadding
                },
                bottom = 118.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (showHeader) {
                item {
                    Text(
                        text = "默认城市：$defaultCity",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cityColors.secondaryText
                    )
                }
            }
            item {
                CitySearchCard(
                    query = query,
                    busy = busy,
                    message = message,
                    searchResults = searchResults,
                    autoFocusSearch = autoFocusSearch,
                    focusSearchToken = focusSearchToken,
                    locationUiState = locationUiState,
                    colors = cityColors,
                    onQueryChange = { query = it },
                    onQuickAdd = viewModel::addCity,
                    onSearch = ::submitSearch,
                    onAddCandidate = viewModel::addCity,
                    onSubmit = ::submitSearch
                )
            }
            items(cities, key = { city -> city.locationId }) { city ->
                val cityIndex = cities.indexOf(city)
                CityCard(
                    city = city,
                    summary = summaries[city.locationId],
                    editing = editing,
                    actionsEnabled = !busy,
                    temperatureUnit = temperatureUnit,
                    canMoveUp = !city.isDefault && cityIndex > 1,
                    canMoveDown = !city.isDefault && cityIndex < cities.lastIndex,
                    onSetDefault = { viewModel.setDefaultCity(city) },
                    onMoveUp = { viewModel.moveCityUp(city) },
                    onMoveDown = { viewModel.moveCityDown(city) },
                    onDelete = { viewModel.removeCity(city) }
                )
            }
        }
        Button(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    horizontal = YunJiUiTokens.BottomActionHorizontalPadding,
                    vertical = 28.dp
                )
                .fillMaxWidth()
                .height(YunJiUiTokens.PrimaryButtonHeight),
            enabled = !busy,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = cityColors.bottomButton),
            onClick = {
                if (query.isBlank()) {
                    focusSearchToken += 1
                } else {
                    submitSearch()
                }
            }
        ) {
            Text(
                text = if (query.isBlank()) "+ 添加城市" else "搜索并添加",
                fontSize = YunJiUiTokens.PrimaryActionTextSize,
                color = cityColors.onBottomButton
            )
        }
    }
}

@Composable
private fun CitySearchCard(
    query: String,
    busy: Boolean,
    message: String,
    searchResults: List<CityEntity>,
    autoFocusSearch: Boolean,
    focusSearchToken: Int,
    locationUiState: LocationUiState,
    colors: CityPageColors,
    onQueryChange: (String) -> Unit,
    onQuickAdd: (String) -> Unit,
    onSearch: () -> Unit,
    onAddCandidate: (CityEntity) -> Unit,
    onSubmit: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(autoFocusSearch, focusSearchToken) {
        if (autoFocusSearch || focusSearchToken > 0) {
            withFrameNanos { }
            focusRequester.requestFocus()
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .focusRequester(focusRequester),
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("搜索城市（中文/拼音）", color = colors.secondaryText.copy(alpha = 0.70f)) },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_search_24),
                    contentDescription = null,
                    tint = colors.secondaryText
                )
            },
            trailingIcon = {
                IconButton(
                    enabled = !busy && query.isNotBlank(),
                    onClick = onSearch
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search_24),
                        contentDescription = "搜索城市",
                        tint = colors.primaryText.copy(alpha = if (!busy && query.isNotBlank()) 0.88f else 0.28f)
                    )
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(32.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.primaryText,
                unfocusedTextColor = colors.primaryText,
                focusedContainerColor = colors.searchContainer,
                unfocusedContainerColor = colors.searchContainer,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = colors.primaryText
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (!busy) {
                        onSubmit()
                    }
                }
            )
        )
        if (searchResults.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                searchResults.forEach { candidate ->
                    CitySearchResultRow(
                        candidate = candidate,
                        enabled = !busy,
                        colors = colors,
                        onAddCandidate = onAddCandidate
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("北京", "上海", "广州", "深圳").forEach { cityName ->
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = !busy,
                    onClick = { onQuickAdd(cityName) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryText),
                    border = BorderStroke(1.dp, colors.cardStroke)
                ) {
                    Text(cityName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        if (locationUiState.message.isNotBlank() || message.isNotBlank() || busy) {
            Text(
                text = when {
                    busy -> "正在处理城市数据"
                    locationUiState.message.isNotBlank() -> locationUiState.message
                    else -> message
                },
                style = MaterialTheme.typography.bodySmall,
                color = when (locationUiState.status) {
                    LocationStatus.ERROR,
                    LocationStatus.DENIED -> MaterialTheme.colorScheme.error
                    LocationStatus.SUCCESS -> Color(0xFF6EA6FF)
                    else -> colors.secondaryText
                }
            )
        }
    }
}

@Composable
private fun CitySearchResultRow(
    candidate: CityEntity,
    enabled: Boolean,
    colors: CityPageColors,
    onAddCandidate: (CityEntity) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.28f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = candidate.cityName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${candidate.province} · ${candidate.country}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            OutlinedButton(
                enabled = enabled,
                onClick = { onAddCandidate(candidate) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryText),
                border = BorderStroke(1.dp, colors.cardStroke)
            ) {
                Text("添加")
            }
        }
    }
}

@Composable
private fun CityCard(
    city: CityEntity,
    summary: CityWeatherSummary?,
    editing: Boolean,
    actionsEnabled: Boolean,
    temperatureUnit: String,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onSetDefault: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    val canDragToReorder = editing && !city.isDefault && actionsEnabled && (canMoveUp || canMoveDown)
    val dragThresholdPx = with(LocalDensity.current) { 56.dp.toPx() }
    var dragOffsetY by remember(city.locationId, editing) { mutableStateOf(0f) }
    val reorderModifier = if (canDragToReorder) {
        Modifier
            .graphicsLayer { translationY = dragOffsetY }
            .pointerInput(city.locationId, canMoveUp, canMoveDown, actionsEnabled) {
                detectDragGesturesAfterLongPress(
                    onDragCancel = { dragOffsetY = 0f },
                    onDragEnd = {
                        when {
                            dragOffsetY <= -dragThresholdPx && canMoveUp -> onMoveUp()
                            dragOffsetY >= dragThresholdPx && canMoveDown -> onMoveDown()
                        }
                        dragOffsetY = 0f
                    },
                    onDrag = { _, dragAmount ->
                        val minOffset = if (canMoveUp) -dragThresholdPx * 1.35f else 0f
                        val maxOffset = if (canMoveDown) dragThresholdPx * 1.35f else 0f
                        dragOffsetY = (dragOffsetY + dragAmount.y).coerceIn(minOffset, maxOffset)
                    }
                )
            }
    } else {
        Modifier
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (city.isDefault) 150.dp else 164.dp)
            .then(reorderModifier),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF2F6CA8)
    ) {
        Box {
            Image(
                painter = painterResource(R.drawable.theme_panorama_day),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.72f),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xCC1E5C9C),
                                Color(0x88305F8E),
                                Color(0x553D526F)
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (editing && !city.isDefault) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(end = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_drag_handle_24),
                            contentDescription = "拖动排序",
                            tint = Color.White.copy(alpha = if (actionsEnabled) 0.88f else 0.32f)
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (city.isDefault) {
                            Text("●", color = Color.White, fontSize = 26.sp)
                        }
                        Text(
                            text = city.cityName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = summary?.condition?.takeIf { it.isNotBlank() } ?: "多云",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }
                if (editing) {
                    CityEditActions(
                        cityIsDefault = city.isDefault,
                        actionsEnabled = actionsEnabled,
                        onSetDefault = onSetDefault,
                        onDelete = onDelete
                    )
                } else {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatCitySummaryTemperature(summary?.temperature, temperatureUnit),
                            fontSize = 52.sp,
                            lineHeight = 54.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White
                        )
                        Text(
                            text = formatCitySummaryTemperatureRange(summary?.tempMax, summary?.tempMin, temperatureUnit),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.64f)
                        )
                        Text(
                            text = summary?.let { formatWeatherTime(it.updateTime) }.orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.48f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CityEditActions(
    cityIsDefault: Boolean,
    actionsEnabled: Boolean,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        CityEditAction(
            iconRes = R.drawable.ic_home_24,
            label = if (cityIsDefault) "常驻地" else "设为常驻地",
            enabled = actionsEnabled && !cityIsDefault,
            onClick = onSetDefault
        )
        if (!cityIsDefault) {
            CityEditAction(
                iconRes = R.drawable.ic_delete_24,
                label = "删除",
                enabled = actionsEnabled,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun CityEditAction(
    iconRes: Int,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            enabled = enabled,
            onClick = onClick
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = Color.White.copy(alpha = if (enabled) 0.90f else 0.36f)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = if (enabled) 0.90f else 0.36f),
            maxLines = 1
        )
    }
}

@Composable
private fun rememberCityPageColors(darkPalette: Boolean): CityPageColors {
    val colorScheme = MaterialTheme.colorScheme
    return if (darkPalette) {
        CityPageColors(
            background = Color.Black,
            primaryText = Color.White,
            secondaryText = Color.White.copy(alpha = 0.58f),
            searchContainer = Color(0xFF171717),
            cardStroke = Color.White.copy(alpha = 0.18f),
            bottomButton = Color(0xFF1E1E1E),
            onBottomButton = Color.White
        )
    } else {
        CityPageColors(
            background = colorScheme.background,
            primaryText = colorScheme.onBackground,
            secondaryText = colorScheme.onSurfaceVariant,
            searchContainer = colorScheme.surface.copy(alpha = 0.76f),
            cardStroke = colorScheme.outline.copy(alpha = 0.18f),
            bottomButton = colorScheme.surface,
            onBottomButton = colorScheme.onSurface
        )
    }
}

private data class CityPageColors(
    val background: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val searchContainer: Color,
    val cardStroke: Color,
    val bottomButton: Color,
    val onBottomButton: Color
)

internal fun formatCitySummaryTemperature(value: String?, unit: String): String {
    val temperature = value?.trim()
    return if (temperature.isNullOrEmpty()) {
        temperaturePlaceholder(unit)
    } else {
        WeatherDisplayUtils.formatTemperature(temperature, unit)
    }
}

internal fun formatCitySummaryTemperatureRange(tempMax: String?, tempMin: String?, unit: String): String {
    val max = tempMax?.trim()
    val min = tempMin?.trim()
    return if (max.isNullOrEmpty() || min.isNullOrEmpty()) {
        "读取中"
    } else {
        "${WeatherDisplayUtils.formatTemperature(max, unit)}/${WeatherDisplayUtils.formatTemperature(min, unit)}"
    }
}

private fun temperaturePlaceholder(unit: String): String {
    return if (WeatherDisplayUtils.TEMPERATURE_FAHRENHEIT == unit) "--°F" else "--℃"
}
