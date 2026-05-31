package com.litroenade.yunjiweather.ui.compose.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.litroenade.yunjiweather.data.entity.WarningEntity
import com.litroenade.yunjiweather.ui.alert.AlertViewModel
import com.litroenade.yunjiweather.ui.compose.InfoCard
import com.litroenade.yunjiweather.ui.compose.MessageCard
import com.litroenade.yunjiweather.ui.compose.ScreenHeader
import com.litroenade.yunjiweather.ui.compose.formatWeatherTime
import com.litroenade.yunjiweather.ui.compose.theme.YunJiUiTokens

@Composable
fun AlertScreen(
    modifier: Modifier = Modifier,
    respectStatusBar: Boolean = true,
    viewModel: AlertViewModel = viewModel()
) {
    val warnings by viewModel.warnings.observeAsState(emptyList())
    val stateText by viewModel.alertStateText.observeAsState("正在读取预警")
    val loading by viewModel.loading.observeAsState(false)
    val message by viewModel.message.observeAsState("")
    var selectedWarning by remember { mutableStateOf<WarningEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .then(if (respectStatusBar) Modifier.statusBarsPadding() else Modifier)
            .padding(horizontal = YunJiUiTokens.ScreenHorizontalPadding),
        contentPadding = PaddingValues(
            top = YunJiUiTokens.PageHeaderVerticalPadding,
            bottom = YunJiUiTokens.PageHeaderVerticalPadding
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeader(
                title = "天气预警",
                subtitle = stateText,
                action = {
                    OutlinedButton(
                        enabled = !loading,
                        onClick = viewModel::refreshState
                    ) {
                        Text("刷新")
                    }
                }
            )
        }
        if (message.isNotBlank()) {
            item {
                InfoCard {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        if (loading) {
            item {
                InfoCard {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator()
                        Text("正在同步预警")
                    }
                }
            }
        }
        if (warnings.isEmpty() && !loading) {
            item {
                MessageCard("暂无预警", stateText, "刷新", viewModel::refreshState)
            }
        } else {
            items(warnings, key = { warning -> warning.warningId }) { warning ->
                WarningCard(
                    warning = warning,
                    onClick = { selectedWarning = warning }
                )
            }
        }
    }

    selectedWarning?.let { warning ->
        WarningDetailDialog(
            warning = warning,
            onDismiss = { selectedWarning = null },
            onMarkRead = {
                viewModel.markWarningRead(warning.warningId)
                selectedWarning = null
            }
        )
    }
}

@Composable
private fun WarningCard(
    warning: WarningEntity,
    onClick: () -> Unit
) {
    val levelColor = warningLevelColor(warning.level)
    InfoCard(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = warning.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${warning.type} · ${warning.level}",
                    style = MaterialTheme.typography.bodySmall,
                    color = levelColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            WarningReadBadge(isRead = warning.isRead, levelColor = levelColor)
        }
        Text(
            text = warning.content,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatWeatherTime(warning.publishTime),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WarningReadBadge(isRead: Boolean, levelColor: Color) {
    val container = if (isRead) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f)
    } else {
        levelColor.copy(alpha = 0.12f)
    }
    val content = if (isRead) MaterialTheme.colorScheme.onSurfaceVariant else levelColor
    Surface(
        color = container,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, content.copy(alpha = 0.18f))
    ) {
        Text(
            text = if (isRead) "已读" else "未读",
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = content
        )
    }
}

@Composable
private fun WarningDetailDialog(
    warning: WarningEntity,
    onDismiss: () -> Unit,
    onMarkRead: () -> Unit
) {
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(warning.title)
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("${warning.type} · ${warning.level}")
                Text(formatWeatherTime(warning.publishTime), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(warning.content)
            }
        },
        confirmButton = {
            Button(onClick = onMarkRead) {
                Text("标记已读")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

private fun warningLevelColor(level: String): Color {
    val normalized = level.lowercase()
    return when {
        "red" in normalized || "红" in level -> Color(0xFFB42318)
        "orange" in normalized || "橙" in level -> Color(0xFFB85C1E)
        "yellow" in normalized || "黄" in level -> Color(0xFF9A6A00)
        "blue" in normalized || "蓝" in level -> Color(0xFF2F6DAE)
        else -> Color(0xFF6B7280)
    }
}
