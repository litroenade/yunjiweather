package com.litroenade.yunjiweather.ui.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.litroenade.yunjiweather.ui.compose.InfoCard
import com.litroenade.yunjiweather.widget.WeatherWidgetLayoutMode

@Composable
fun DesktopWeatherScreen(
    modifier: Modifier = Modifier,
    onRequestWidget: (WeatherWidgetLayoutMode) -> Unit
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            InfoCard {
                Text(
                    text = "桌面天气小组件",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "小组件读取本地默认城市天气缓存；首页刷新和后台同步成功后会更新桌面数据。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                WidgetPresetButton("紧凑卡片", "只显示城市、温度和天气概况。") {
                    onRequestWidget(WeatherWidgetLayoutMode.COMPACT)
                }
                WidgetPresetButton("标准卡片", "显示天气概况和更新时间。") {
                    onRequestWidget(WeatherWidgetLayoutMode.STANDARD)
                }
                WidgetPresetButton("详细卡片", "显示湿度、风力、空气质量和生活建议。") {
                    onRequestWidget(WeatherWidgetLayoutMode.EXPANDED)
                }
            }
        }
        item {
            InfoCard {
                Text(
                    text = "系统限制",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "部分桌面启动器不支持应用主动请求添加小组件；如果没有系统弹窗，请长按桌面后从小组件列表中选择云迹天气。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WidgetPresetButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Text("$title · $subtitle")
    }
}
