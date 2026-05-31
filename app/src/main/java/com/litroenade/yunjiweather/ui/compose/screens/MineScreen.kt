package com.litroenade.yunjiweather.ui.compose.screens

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.litroenade.yunjiweather.ui.compose.ScreenHeader
import com.litroenade.yunjiweather.ui.compose.SectionTitle
import com.litroenade.yunjiweather.ui.compose.theme.LocalYunJiVisualTheme
import com.litroenade.yunjiweather.ui.compose.theme.YunJiUiTokens
import com.litroenade.yunjiweather.ui.mine.MineViewModel
import com.litroenade.yunjiweather.utils.PermissionUtils
import com.litroenade.yunjiweather.utils.VisualThemeCatalog
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils

@Composable
fun MineScreen(
    modifier: Modifier = Modifier,
    respectStatusBar: Boolean = true,
    showHeader: Boolean = true,
    viewModel: MineViewModel = viewModel()
) {
    val context = LocalContext.current
    val localSpaceText by viewModel.localSpaceText.observeAsState("\u672c\u673a\u5929\u6c14\u7a7a\u95f4")
    val defaultCity by viewModel.defaultCity.observeAsState("\u9ed8\u8ba4\u57ce\u5e02\u8bfb\u53d6\u4e2d")
    val warningEnabled by viewModel.warningEnabled.observeAsState(true)
    val dailyReminderEnabled by viewModel.dailyReminderEnabled.observeAsState(false)
    val animationEnabled by viewModel.animationEnabled.observeAsState(true)
    val darkModeEnabled by viewModel.darkModeEnabled.observeAsState(false)
    val developerToolsEnabled by viewModel.developerToolsEnabled.observeAsState(false)
    val temperatureUnit by viewModel.temperatureUnit.observeAsState(WeatherDisplayUtils.TEMPERATURE_CELSIUS)
    val windUnit by viewModel.windUnit.observeAsState(WeatherDisplayUtils.WIND_SCALE)
    val selectedTheme by viewModel.visualTheme.observeAsState(viewModel.currentVisualTheme)
    val dataUpdateTime by viewModel.dataUpdateTime.observeAsState("\u6682\u65e0\u66f4\u65b0")
    val localStorageSummary by viewModel.localStorageSummary.observeAsState("")
    val message by viewModel.message.observeAsState("")
    val selectedThemeName = remember(selectedTheme) {
        VisualThemeCatalog.getThemeOrDefault(selectedTheme).displayName
    }
    var infoDialog by remember { mutableStateOf<MineInfoDialog?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    val listModifier = if (respectStatusBar) modifier.statusBarsPadding() else modifier
    LazyColumn(
        modifier = listModifier.padding(horizontal = YunJiUiTokens.ScreenHorizontalPadding),
        contentPadding = PaddingValues(
            top = YunJiUiTokens.PageHeaderVerticalPadding,
            bottom = 72.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showHeader) {
            item {
                ScreenHeader(
                    title = "\u8bbe\u7f6e",
                    subtitle = localSpaceText
                )
            }
        }
        item {
            SettingsCard {
                Text(
                    text = defaultCity,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = dataUpdateTime,
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
        item { SectionTitle("\u5929\u6c14\u504f\u597d") }
        item {
            SettingsCard {
                SettingRow(
                    title = "\u5929\u6c14\u9884\u8b66",
                    checked = warningEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            (context as? Activity)?.let(PermissionUtils::requestNotificationPermission)
                        }
                        viewModel.setWarningEnabled(enabled)
                    }
                )
                SettingRow(
                    title = "\u6bcf\u65e5\u63d0\u9192",
                    checked = dailyReminderEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            (context as? Activity)?.let(PermissionUtils::requestNotificationPermission)
                        }
                        viewModel.setDailyReminderEnabled(enabled)
                    }
                )
                SettingRow("\u5929\u6c14\u52a8\u753b", animationEnabled, viewModel::setAnimationEnabled)
                SettingRow("\u6df1\u8272\u6a21\u5f0f", darkModeEnabled, viewModel::setDarkModeEnabled)
                SettingRow("\u5141\u8bb8\u5f00\u53d1\u8005\u5de5\u5177", developerToolsEnabled, viewModel::setDeveloperToolsEnabled)
            }
        }
        item { SectionTitle("\u5355\u4f4d\u4e0e\u4e3b\u9898") }
        item {
            SettingsCard {
                SettingLabel("\u6e29\u5ea6\u5355\u4f4d")
                OptionRow {
                    CompactSegmentedButton(
                        text = "\u6444\u6c0f\u5ea6",
                        selected = temperatureUnit == WeatherDisplayUtils.TEMPERATURE_CELSIUS,
                        onClick = { viewModel.setTemperatureUnit(WeatherDisplayUtils.TEMPERATURE_CELSIUS) }
                    )
                    CompactSegmentedButton(
                        text = "\u534e\u6c0f\u5ea6",
                        selected = temperatureUnit == WeatherDisplayUtils.TEMPERATURE_FAHRENHEIT,
                        onClick = { viewModel.setTemperatureUnit(WeatherDisplayUtils.TEMPERATURE_FAHRENHEIT) }
                    )
                }
                SettingLabel("\u98ce\u901f\u5355\u4f4d")
                OptionRow {
                    CompactSegmentedButton(
                        text = "\u98ce\u529b\u7b49\u7ea7",
                        selected = windUnit == WeatherDisplayUtils.WIND_SCALE,
                        onClick = { viewModel.setWindUnit(WeatherDisplayUtils.WIND_SCALE) }
                    )
                    CompactSegmentedButton(
                        text = "\u7c73/\u79d2",
                        selected = windUnit == WeatherDisplayUtils.WIND_METER_PER_SECOND,
                        onClick = { viewModel.setWindUnit(WeatherDisplayUtils.WIND_METER_PER_SECOND) }
                    )
                }
                SettingLabel("\u5f53\u524d\u4e3b\u9898")
                Text(
                    text = selectedThemeName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "\u5728\u4e2a\u6027\u6362\u80a4\u4e2d\u8c03\u6574\u4e3b\u9898\u3001\u81ea\u5b9a\u4e49\u7d20\u6750\u548c\u9996\u9875\u6a21\u5757\u987a\u5e8f\u3002",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item { SectionTitle("\u5e94\u7528\u4fe1\u606f") }
        item {
            AppInfoPanel(
                localStorageSummary = localStorageSummary,
                onClearCache = viewModel::clearCache,
                onOpenInfo = { infoDialog = it }
            )
        }
    }

    infoDialog?.let { dialog ->
        AlertDialog(
            onDismissRequest = { infoDialog = null },
            title = { Text(dialog.title) },
            text = { Text(text = dialog.message, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { infoDialog = null }) {
                    Text("\u77e5\u9053\u4e86")
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    val visualTheme = LocalYunJiVisualTheme.current
    val darkPalette = visualTheme.background.luminance() < 0.35f ||
            visualTheme.defaultWeatherGradient.top.luminance() < 0.35f
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = if (darkPalette) 0.30f else 0.62f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = if (darkPalette) 0.09f else 0.12f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 46.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
    )
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
private fun RowScope.CompactSegmentedButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    val modifier = Modifier
        .weight(1f)
        .heightIn(min = 44.dp)
    if (selected) {
        Button(
            modifier = modifier,
            shape = shape,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            onClick = onClick
        ) {
            Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
        }
    } else {
        OutlinedButton(
            modifier = modifier,
            shape = shape,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            onClick = onClick
        ) {
            Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun AppInfoPanel(
    localStorageSummary: String,
    onClearCache: () -> Unit,
    onOpenInfo: (MineInfoDialog) -> Unit
) {
    SettingsCard {
        SettingLabel("\u672c\u5730\u5b58\u50a8\u72b6\u6001")
        Text(
            text = localStorageSummary.ifBlank { "\u6b63\u5728\u8bfb\u53d6\u672c\u5730\u6570\u636e" },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SettingLabel("\u9879\u76ee\u8fd0\u884c\u72b6\u6001")
        Text(
            text = "\u672c\u5730\u6570\u636e\u3001Compose \u754c\u9762\u548c\u540e\u53f0\u63d0\u9192\u6b63\u5e38\u8fd0\u884c\u3002",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            onClick = onClearCache
        ) {
            Text("\u6e05\u7406\u5929\u6c14\u7f13\u5b58")
        }
        InformationRow("\u6570\u636e\u6765\u6e90\u8bf4\u660e") { onOpenInfo(MineInfoDialog.DataSource) }
        InformationRow("\u5f00\u53d1\u4e0e\u9a8c\u8bc1\u8bf4\u660e") { onOpenInfo(MineInfoDialog.Developer) }
        InformationRow("\u4f7f\u7528\u5e2e\u52a9") { onOpenInfo(MineInfoDialog.Help) }
        InformationRow("\u5173\u4e8e\u5e94\u7528") { onOpenInfo(MineInfoDialog.About) }
    }
}

@Composable
private fun InformationRow(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        TextButton(onClick = onClick) {
            Text("\u67e5\u770b", style = MaterialTheme.typography.bodySmall)
        }
    }
}

private enum class MineInfoDialog(
    val title: String,
    val message: String
) {
    DataSource(
        "\u6570\u636e\u6765\u6e90",
        "\u5929\u6c14\u3001\u7a7a\u6c14\u8d28\u91cf\u548c\u57ce\u5e02\u641c\u7d22\u4f7f\u7528\u672c\u5730\u7f13\u5b58\u4e0e Open-Meteo \u6570\u636e\uff0c\u65e0\u9700\u767b\u5f55\u3002"
    ),
    Developer(
        "\u5f00\u53d1\u4e0e\u9a8c\u8bc1",
        "\u5f53\u524d\u5e94\u7528\u4f7f\u7528 Kotlin\u3001Compose\u3001Room \u548c Worker\uff0c\u5efa\u8bae\u901a\u8fc7 Gradle \u7f16\u8bd1\u4e0e\u5355\u5143\u6d4b\u8bd5\u9a8c\u8bc1\u3002"
    ),
    Help(
        "\u4f7f\u7528\u5e2e\u52a9",
        "\u9996\u9875\u67e5\u770b\u9ed8\u8ba4\u57ce\u5e02\u5929\u6c14\uff0c\u7ba1\u7406\u57ce\u5e02\u9875\u53ef\u6dfb\u52a0\u3001\u6392\u5e8f\u548c\u5220\u9664\u57ce\u5e02\uff0c\u4e2a\u6027\u5316\u9875\u53ef\u8c03\u6574\u4e3b\u9898\u548c\u9996\u9875\u6a21\u5757\u3002"
    ),
    About(
        "\u5173\u4e8e\u4e91\u8ff9\u5929\u6c14",
        "\u4e91\u8ff9\u5929\u6c14\u662f\u4ee5\u672c\u5730\u7f13\u5b58\u548c\u4e2a\u6027\u5316\u89c6\u89c9\u4e3a\u6838\u5fc3\u7684\u5929\u6c14\u5e94\u7528\u3002"
    )
}
