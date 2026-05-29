package com.litroenade.yunjiweather.ui.compose.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.litroenade.yunjiweather.data.entity.CityEntity
import com.litroenade.yunjiweather.data.model.CityWeatherSummary
import com.litroenade.yunjiweather.ui.city.CityViewModel
import com.litroenade.yunjiweather.ui.compose.InfoCard
import com.litroenade.yunjiweather.ui.compose.ScreenHeader
import com.litroenade.yunjiweather.ui.compose.formatWeatherTime
import com.litroenade.yunjiweather.ui.location.LocationStatus
import com.litroenade.yunjiweather.ui.location.LocationUiState
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils

@Composable
fun CityScreen(
    modifier: Modifier = Modifier,
    temperatureUnit: String = WeatherDisplayUtils.TEMPERATURE_CELSIUS,
    respectStatusBar: Boolean = true,
    autoFocusSearch: Boolean = false,
    showHeader: Boolean = true,
    locationUiState: LocationUiState = LocationUiState.idle(),
    onRequestLocation: () -> Unit = {},
    onDefaultCityChanged: () -> Unit = {},
    viewModel: CityViewModel = viewModel()
) {
    val cities by viewModel.getCities().observeAsState(emptyList())
    val searchResults by viewModel.getSearchResults().observeAsState(emptyList())
    val summaries by viewModel.getCitySummaries().observeAsState(emptyMap())
    val defaultCity by viewModel.getDefaultCity().observeAsState("未设置")
    val message by viewModel.getMessage().observeAsState("")
    val busy by viewModel.getBusy().observeAsState(false)
    val defaultCityChangeVersion by viewModel.getDefaultCityChangeVersion().observeAsState(0L)
    var query by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(defaultCityChangeVersion) {
        if (defaultCityChangeVersion > 0L) {
            onDefaultCityChanged()
        }
    }

    val listModifier = if (respectStatusBar) {
        modifier.statusBarsPadding()
    } else {
        modifier
    }
    LazyColumn(
        modifier = listModifier
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (showHeader) {
            item {
                ScreenHeader(
                    title = "城市",
                    subtitle = "默认城市：$defaultCity"
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
                locationUiState = locationUiState,
                onQueryChange = { query = it },
                onRequestLocation = onRequestLocation,
                onQuickAdd = viewModel::addCity,
                onSearch = { viewModel.searchCities(query) },
                onAddCandidate = viewModel::addCity,
                onSubmit = {
                    viewModel.searchCities(query)
                }
            )
        }
        items(cities, key = { city -> city.locationId }) { city ->
            val cityIndex = cities.indexOf(city)
            CityCard(
                city = city,
                summary = summaries[city.locationId],
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
}

@Composable
private fun CitySearchCard(
    query: String,
    busy: Boolean,
    message: String,
    searchResults: List<CityEntity>,
    autoFocusSearch: Boolean,
    locationUiState: LocationUiState,
    onQueryChange: (String) -> Unit,
    onRequestLocation: () -> Unit,
    onQuickAdd: (String) -> Unit,
    onSearch: () -> Unit,
    onAddCandidate: (CityEntity) -> Unit,
    onSubmit: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(autoFocusSearch) {
        if (autoFocusSearch) {
            focusRequester.requestFocus()
        }
    }
    InfoCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                value = query,
                onValueChange = onQueryChange,
                enabled = !busy,
                label = { Text("城市名称") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (!busy) {
                            onSubmit()
                        }
                    }
                )
            )
            Button(
                enabled = !busy,
                onClick = onSearch
            ) {
                Text("搜索")
            }
        }
        if (searchResults.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                searchResults.forEach { candidate ->
                    CitySearchResultRow(
                        candidate = candidate,
                        enabled = !busy,
                        onAddCandidate = onAddCandidate
                    )
                }
            }
        }
        Text(
            text = "热门城市",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("北京", "上海", "广州", "深圳").forEach { cityName ->
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = !busy,
                    onClick = { onQuickAdd(cityName) }
                ) {
                    Text(cityName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy && !locationUiState.isBusy,
            onClick = onRequestLocation
        ) {
            Text(locationActionText(locationUiState.status))
        }
        if (locationUiState.message.isNotBlank()) {
            Text(
                text = locationUiState.message,
                style = MaterialTheme.typography.bodySmall,
                color = when (locationUiState.status) {
                    LocationStatus.ERROR,
                    LocationStatus.DENIED -> MaterialTheme.colorScheme.error
                    LocationStatus.SUCCESS -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        if (message.isNotBlank()) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (busy) {
            Text(
                text = "正在处理城市数据",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CitySearchResultRow(
    candidate: CityEntity,
    enabled: Boolean,
    onAddCandidate: (CityEntity) -> Unit
) {
    SurfaceLikeSearchResult {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                onClick = { onAddCandidate(candidate) }
            ) {
                Text("添加")
            }
        }
    }
}

@Composable
private fun SurfaceLikeSearchResult(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.34f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        content()
    }
}

private fun locationActionText(status: LocationStatus): String {
    return when (status) {
        LocationStatus.REQUESTING_PERMISSION -> "等待定位授权"
        LocationStatus.FETCHING_LOCATION -> "正在定位"
        LocationStatus.SUCCESS -> "重新定位当前城市"
        else -> "定位当前城市"
    }
}

@Composable
private fun CityCard(
    city: CityEntity,
    summary: CityWeatherSummary?,
    actionsEnabled: Boolean,
    temperatureUnit: String,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onSetDefault: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    val cardShape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
    val gradient = cityWeatherCardGradient(summary)
    val primaryText = Color.White
    val secondaryText = Color.White.copy(alpha = 0.78f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient, cardShape)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)), cardShape)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = city.cityName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (city.isDefault) {
                    Text(
                        text = "默认",
                        style = MaterialTheme.typography.labelMedium,
                        color = primaryText
                    )
                }
            }
            Text(
                text = "${city.province} · ${city.country}",
                style = MaterialTheme.typography.bodySmall,
                color = secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                enabled = actionsEnabled && !city.isDefault,
                onClick = onSetDefault,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = primaryText,
                    disabledContentColor = secondaryText
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = if (city.isDefault) 0.22f else 0.54f))
            ) {
                Text("设为默认", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                enabled = actionsEnabled,
                onClick = onDelete,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryText),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.54f))
            ) {
                Text("删除", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                enabled = actionsEnabled && canMoveUp,
                onClick = onMoveUp,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = primaryText,
                    disabledContentColor = secondaryText
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = if (canMoveUp) 0.54f else 0.22f))
            ) {
                Text("上移", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                enabled = actionsEnabled && canMoveDown,
                onClick = onMoveDown,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = primaryText,
                    disabledContentColor = secondaryText
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = if (canMoveDown) 0.54f else 0.22f))
            ) {
                Text("下移", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (summary == null || summary.errorMessage.isNotBlank()) {
            Text(
                text = summary?.errorMessage ?: "正在读取天气摘要",
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryText
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CityMetricTile("天气", summary.condition, Modifier.weight(1f))
                CityMetricTile("温度", WeatherDisplayUtils.formatTemperature(summary.temperature, temperatureUnit), Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CityMetricTile(
                    "范围",
                    "${WeatherDisplayUtils.formatTemperature(summary.tempMin, temperatureUnit)}/${WeatherDisplayUtils.formatTemperature(summary.tempMax, temperatureUnit)}",
                    Modifier.weight(1f)
                )
                CityMetricTile("更新", formatWeatherTime(summary.updateTime), Modifier.weight(1f))
            }
            Text(
                text = if (summary.isFromCache) {
                    "本地缓存"
                } else {
                    "实时更新"
                },
                style = MaterialTheme.typography.labelSmall,
                color = secondaryText
            )
        }
    }
}

@Composable
private fun CityMetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.72f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun cityWeatherCardGradient(summary: CityWeatherSummary?): Brush {
    if (summary == null || summary.errorMessage.isNotBlank()) {
        return Brush.linearGradient(
            listOf(
                Color(0xFF728096),
                Color(0xFF435062)
            )
        )
    }
    val condition = summary.condition
    return when {
        "雨" in condition -> Brush.linearGradient(listOf(Color(0xFF6F93AE), Color(0xFF40576F)))
        "雪" in condition -> Brush.linearGradient(listOf(Color(0xFF9BB5C7), Color(0xFF60788C)))
        "晴" in condition -> Brush.linearGradient(listOf(Color(0xFF77B5E6), Color(0xFF497EAF)))
        else -> Brush.linearGradient(listOf(Color(0xFF89B7D9), Color(0xFF537C9E)))
    }
}
