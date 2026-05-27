package com.litroenade.yunjiweather.ui.compose.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.litroenade.yunjiweather.data.entity.CityEntity
import com.litroenade.yunjiweather.data.model.CityWeatherSummary
import com.litroenade.yunjiweather.ui.city.CityViewModel
import com.litroenade.yunjiweather.ui.compose.InfoCard
import com.litroenade.yunjiweather.ui.compose.MetricTile
import com.litroenade.yunjiweather.ui.compose.ScreenHeader
import com.litroenade.yunjiweather.ui.compose.formatWeatherTime
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils

@Composable
fun CityScreen(
    modifier: Modifier = Modifier,
    temperatureUnit: String = WeatherDisplayUtils.TEMPERATURE_CELSIUS,
    respectStatusBar: Boolean = true,
    autoFocusSearch: Boolean = false,
    onDefaultCityChanged: () -> Unit = {},
    viewModel: CityViewModel = viewModel()
) {
    val cities by viewModel.getCities().observeAsState(emptyList())
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
        item {
            ScreenHeader(
                title = "城市",
                subtitle = "默认城市：$defaultCity"
            )
        }
        item {
            CitySearchCard(
                query = query,
                busy = busy,
                message = message,
                autoFocusSearch = autoFocusSearch,
                onQueryChange = { query = it },
                onQuickAdd = viewModel::addCity,
                onSubmit = {
                    viewModel.addCity(query)
                    query = ""
                }
            )
        }
        items(cities, key = { city -> city.locationId }) { city ->
            CityCard(
                city = city,
                summary = summaries[city.locationId],
                actionsEnabled = !busy,
                temperatureUnit = temperatureUnit,
                onSetDefault = { viewModel.setDefaultCity(city) },
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
    autoFocusSearch: Boolean,
    onQueryChange: (String) -> Unit,
    onQuickAdd: (String) -> Unit,
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
                onClick = onSubmit
            ) {
                Text("添加")
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
private fun CityCard(
    city: CityEntity,
    summary: CityWeatherSummary?,
    actionsEnabled: Boolean,
    temperatureUnit: String,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    InfoCard {
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (city.isDefault) {
                    Text(
                        text = "默认",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = "${city.province} · ${city.country}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                onClick = onSetDefault
            ) {
                Text("设为默认", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                enabled = actionsEnabled,
                onClick = onDelete
            ) {
                Text("删除", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (summary == null || summary.errorMessage.isNotBlank()) {
            Text(
                text = summary?.errorMessage ?: "正在读取天气摘要",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MetricTile("天气", summary.condition, Modifier.weight(1f))
                MetricTile("温度", WeatherDisplayUtils.formatTemperature(summary.temperature, temperatureUnit), Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MetricTile(
                    "范围",
                    "${WeatherDisplayUtils.formatTemperature(summary.tempMin, temperatureUnit)}/${WeatherDisplayUtils.formatTemperature(summary.tempMax, temperatureUnit)}",
                    Modifier.weight(1f)
                )
                MetricTile("更新", formatWeatherTime(summary.updateTime), Modifier.weight(1f))
            }
            Text(
                text = if (summary.isFromCache) {
                    "本地缓存"
                } else {
                    "实时更新"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
