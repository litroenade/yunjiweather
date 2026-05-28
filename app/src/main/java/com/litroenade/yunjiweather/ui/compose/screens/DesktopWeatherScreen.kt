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

@Composable
fun DesktopWeatherScreen(
    modifier: Modifier = Modifier,
    onRequestWidget: () -> Unit
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
                    text = "小组件读取本地默认城市天气缓存；首页刷新成功后会同步更新小组件数据。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRequestWidget
                ) {
                    Text("添加到桌面")
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
