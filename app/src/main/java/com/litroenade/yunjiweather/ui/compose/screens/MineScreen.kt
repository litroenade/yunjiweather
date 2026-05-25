package com.litroenade.yunjiweather.ui.compose.screens

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.litroenade.yunjiweather.ui.compose.InfoCard
import com.litroenade.yunjiweather.ui.compose.ScreenHeader
import com.litroenade.yunjiweather.ui.mine.MineViewModel
import com.litroenade.yunjiweather.utils.PermissionUtils
import com.litroenade.yunjiweather.utils.VisualTheme
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils

@Composable
fun MineScreen(
    modifier: Modifier = Modifier,
    respectStatusBar: Boolean = true,
    viewModel: MineViewModel = viewModel()
) {
    val context = LocalContext.current
    val localSpaceText by viewModel.getLocalSpaceText().observeAsState("本机天气空间")
    val defaultCity by viewModel.getDefaultCity().observeAsState("默认城市读取中")
    val warningEnabled by viewModel.getWarningEnabled().observeAsState(true)
    val dailyReminderEnabled by viewModel.getDailyReminderEnabled().observeAsState(false)
    val animationEnabled by viewModel.getAnimationEnabled().observeAsState(true)
    val darkModeEnabled by viewModel.getDarkModeEnabled().observeAsState(false)
    val temperatureUnit by viewModel.getTemperatureUnit().observeAsState(WeatherDisplayUtils.TEMPERATURE_CELSIUS)
    val windUnit by viewModel.getWindUnit().observeAsState(WeatherDisplayUtils.WIND_SCALE)
    val selectedTheme by viewModel.getVisualTheme().observeAsState(viewModel.getCurrentVisualTheme())
    val dataUpdateTime by viewModel.getDataUpdateTime().observeAsState("暂无更新")
    val localStorageSummary by viewModel.getLocalStorageSummary().observeAsState("")
    val message by viewModel.getMessage().observeAsState("")
    val themes = remember(viewModel) { viewModel.getVisualThemes() }

    LaunchedEffect(Unit) {
        viewModel.refresh()
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
                title = "我的",
                subtitle = localSpaceText
            )
        }
        item {
            InfoCard {
                Text(
                    text = defaultCity,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = dataUpdateTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = localStorageSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (message.isNotBlank()) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        item {
            InfoCard {
                SettingSwitch(
                    title = "天气预警",
                    checked = warningEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            (context as? Activity)?.let(PermissionUtils::requestNotificationPermission)
                        }
                        viewModel.setWarningEnabled(enabled)
                    }
                )
                SettingSwitch(
                    title = "每日提醒",
                    checked = dailyReminderEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            (context as? Activity)?.let(PermissionUtils::requestNotificationPermission)
                        }
                        viewModel.setDailyReminderEnabled(enabled)
                    }
                )
                SettingSwitch("天气动画", animationEnabled, viewModel::setAnimationEnabled)
                SettingSwitch("深色模式", darkModeEnabled, viewModel::setDarkModeEnabled)
            }
        }
        item {
            InfoCard {
                Text("温度单位", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                OptionRow {
                    UnitButton(
                        text = "摄氏度",
                        selected = temperatureUnit == WeatherDisplayUtils.TEMPERATURE_CELSIUS,
                        onClick = { viewModel.setTemperatureUnit(WeatherDisplayUtils.TEMPERATURE_CELSIUS) }
                    )
                    UnitButton(
                        text = "华氏度",
                        selected = temperatureUnit == WeatherDisplayUtils.TEMPERATURE_FAHRENHEIT,
                        onClick = { viewModel.setTemperatureUnit(WeatherDisplayUtils.TEMPERATURE_FAHRENHEIT) }
                    )
                }
                Text("风速单位", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                OptionRow {
                    UnitButton(
                        text = "风力等级",
                        selected = windUnit == WeatherDisplayUtils.WIND_SCALE,
                        onClick = { viewModel.setWindUnit(WeatherDisplayUtils.WIND_SCALE) }
                    )
                    UnitButton(
                        text = "米/秒",
                        selected = windUnit == WeatherDisplayUtils.WIND_METER_PER_SECOND,
                        onClick = { viewModel.setWindUnit(WeatherDisplayUtils.WIND_METER_PER_SECOND) }
                    )
                }
            }
        }
        item {
            InfoCard {
                ThemeChooser(
                    themes = themes,
                    selectedTheme = selectedTheme,
                    onThemeSelected = viewModel::setVisualTheme
                )
            }
        }
        item {
            InfoCard {
                Button(onClick = viewModel::clearCache) {
                    Text("清理天气缓存")
                }
            }
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun OptionRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
private fun RowScope.UnitButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Button(
            modifier = Modifier.weight(1f),
            onClick = onClick
        ) {
            Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    } else {
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = onClick
        ) {
            Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ThemeChooser(
    themes: List<VisualTheme>,
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "视觉主题",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(themes, key = { theme -> theme.key }) { theme ->
                Column(
                    modifier = Modifier.width(178.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (theme.key == selectedTheme) {
                        Button(onClick = { onThemeSelected(theme.key) }) {
                            Text("${theme.displayName} · 已应用", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    } else {
                        OutlinedButton(onClick = { onThemeSelected(theme.key) }) {
                            Text(theme.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Text(
                        text = theme.shortDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
