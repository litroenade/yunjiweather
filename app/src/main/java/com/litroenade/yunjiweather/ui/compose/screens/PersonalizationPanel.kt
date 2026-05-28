package com.litroenade.yunjiweather.ui.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.litroenade.yunjiweather.utils.HomeBlock
import com.litroenade.yunjiweather.utils.VisualTheme
import com.litroenade.yunjiweather.utils.VisualThemeStyle

@Composable
internal fun PersonalizationPanel(
    themes: List<VisualTheme>,
    themeStyles: List<VisualThemeStyle>,
    selectedTheme: String,
    selectedThemeStyle: String,
    homeBlockOrder: List<HomeBlock>,
    homeBlockEnabled: Map<HomeBlock, Boolean>,
    onThemeSelected: (String) -> Unit,
    onStyleSelected: (String) -> Unit,
    onHomeBlockEnabledChange: (HomeBlock, Boolean) -> Unit,
    onMoveHomeBlockUp: (HomeBlock) -> Unit,
    onMoveHomeBlockDown: (HomeBlock) -> Unit,
    onResetHomeBlocks: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ThemeChooser(
            themes = themes,
            selectedTheme = selectedTheme,
            onThemeSelected = onThemeSelected
        )
        ThemeStyleChooser(
            styles = themeStyles,
            selectedStyle = selectedThemeStyle,
            onStyleSelected = onStyleSelected
        )
        HomeBlockEditor(
            blocks = homeBlockOrder,
            enabledBlocks = homeBlockEnabled,
            onEnabledChange = onHomeBlockEnabledChange,
            onMoveUp = onMoveHomeBlockUp,
            onMoveDown = onMoveHomeBlockDown,
            onReset = onResetHomeBlocks
        )
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
                            Text(
                                if (theme.isCustomSlot) "新建 ${theme.displayName}" else theme.displayName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
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
private fun ThemeStyleChooser(
    styles: List<VisualThemeStyle>,
    selectedStyle: String,
    onStyleSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "主题外观",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(styles, key = { style -> style.key }) { style ->
                Column(
                    modifier = Modifier.width(150.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (style.key == selectedStyle) {
                        Button(onClick = { onStyleSelected(style.key) }) {
                            Text("${style.displayName} · 已应用", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    } else {
                        OutlinedButton(onClick = { onStyleSelected(style.key) }) {
                            Text(style.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Text(
                        text = style.shortDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeBlockEditor(
    blocks: List<HomeBlock>,
    enabledBlocks: Map<HomeBlock, Boolean>,
    onEnabledChange: (HomeBlock, Boolean) -> Unit,
    onMoveUp: (HomeBlock) -> Unit,
    onMoveDown: (HomeBlock) -> Unit,
    onReset: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "首页模块",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "每个主题单独保存下方模块的显示和顺序。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        blocks.forEachIndexed { index, block ->
            HomeBlockRow(
                block = block,
                enabled = enabledBlocks[block] ?: true,
                canMoveUp = index > 0,
                canMoveDown = index < blocks.lastIndex,
                onEnabledChange = { enabled -> onEnabledChange(block, enabled) },
                onMoveUp = { onMoveUp(block) },
                onMoveDown = { onMoveDown(block) }
            )
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onReset
        ) {
            Text("恢复默认模块布局")
        }
    }
}

@Composable
private fun HomeBlockRow(
    block: HomeBlock,
    enabled: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = block.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = block.shortDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            TextButton(
                enabled = canMoveUp,
                onClick = onMoveUp
            ) {
                Text("上移")
            }
            TextButton(
                enabled = canMoveDown,
                onClick = onMoveDown
            ) {
                Text("下移")
            }
        }
    }
}
