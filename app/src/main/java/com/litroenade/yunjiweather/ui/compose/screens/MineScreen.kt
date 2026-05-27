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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.litroenade.yunjiweather.ui.compose.InfoCard
import com.litroenade.yunjiweather.ui.compose.ScreenHeader
import com.litroenade.yunjiweather.ui.compose.SectionTitle
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
    var infoDialog by remember { mutableStateOf<MineInfoDialog?>(null) }

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
            SectionTitle("天气偏好")
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
            SectionTitle("单位与主题")
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
            SectionTitle("应用信息")
        }
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
            text = {
                Text(
                    text = dialog.message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { infoDialog = null }) {
                    Text("知道了")
                }
            }
        )
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

@Composable
private fun AppInfoPanel(
    localStorageSummary: String,
    onClearCache: () -> Unit,
    onOpenInfo: (MineInfoDialog) -> Unit
) {
    InfoCard {
        Text(
            text = "本地存储状态",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = localStorageSummary.ifBlank { "正在读取本地数据" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "项目运行状态",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "本机单用户数据空间；Compose 主界面；Room 本地缓存；Worker 负责后台提醒与预警刷新。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClearCache
        ) {
            Text("清理天气缓存")
        }
        InformationRow("数据来源说明") {
            onOpenInfo(MineInfoDialog.DataSource)
        }
        InformationRow("开发与验证说明") {
            onOpenInfo(MineInfoDialog.Developer)
        }
        InformationRow("使用帮助") {
            onOpenInfo(MineInfoDialog.Help)
        }
        InformationRow("关于应用") {
            onOpenInfo(MineInfoDialog.About)
        }
    }
}

@Composable
private fun InformationRow(
    title: String,
    onClick: () -> Unit
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
        TextButton(onClick = onClick) {
            Text("查看")
        }
    }
}

private enum class MineInfoDialog(
    val title: String,
    val message: String
) {
    DataSource(
        "数据来源",
        "天气、空气质量和城市搜索默认使用 Open-Meteo，可无 API Key 运行。配置 QWeather 后，会增强生活指数和官方天气预警能力；未配置 QWeather 时不会伪造官方预警。"
    ),
    Developer(
        "开发与验证说明",
        "当前重构为 Kotlin + Compose 主界面，后端本地层保留 Room、Repository、Worker。IntelliJ IDEA 下 AGP 保持 8.10.0-alpha05，Gradle Wrapper 为 8.11.1，Gradle JVM 使用 JDK 17。常用验证命令：:app:compileDebugKotlin、:app:testDebugUnitTest、:app:assembleDebug。"
    ),
    Help(
        "使用帮助",
        "首页查看默认城市天气；顶部搜索和管理入口维护城市；今日资讯进入预警和生活指数，更多菜单保留定位、分享、反馈、桌面天气和设置。我的页可开启通知、每日提醒、天气动画、深色模式，并切换单位和视觉主题。通知和定位权限只在功能需要时申请；清理缓存不会删除已关注城市。"
    ),
    About(
        "关于云迹天气",
        "云迹天气是本机天气应用，城市、天气缓存、预警状态和偏好都保留在设备本地。登录模块已移除，当前不再接入账号系统，也不会上传本地账号数据。"
    )
}
