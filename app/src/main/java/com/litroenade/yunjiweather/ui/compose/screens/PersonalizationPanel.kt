package com.litroenade.yunjiweather.ui.compose.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litroenade.yunjiweather.R
import com.litroenade.yunjiweather.ui.compose.theme.LocalYunJiVisualTheme
import com.litroenade.yunjiweather.ui.compose.theme.skins.ThemeSkinCatalog
import com.litroenade.yunjiweather.utils.HomeBlock
import com.litroenade.yunjiweather.utils.VisualTheme
import com.litroenade.yunjiweather.utils.VisualThemeUtils

@Composable
internal fun PersonalizationPanel(
    themes: List<VisualTheme>,
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val visibleThemes = themes.take(MAX_THEME_CARDS)
    val selectedThemeModel = visibleThemes.firstOrNull { theme -> theme.key == selectedTheme }
        ?: visibleThemes.firstOrNull()
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        ThemeAppliedStatus(themeName = selectedThemeModel?.displayName ?: "默认主题")
        ThemePreviewCarousel(
            themes = visibleThemes,
            selectedTheme = selectedTheme,
            onThemeSelected = onThemeSelected
        )
        if (selectedThemeModel != null) {
            SelectedThemePanel(
                theme = selectedThemeModel,
                selected = selectedThemeModel.key == selectedTheme,
                onThemeSelected = onThemeSelected
            )
        }
        ThemeCardGrid(
            themes = visibleThemes,
            selectedTheme = selectedTheme,
            onThemeSelected = onThemeSelected
        )
    }
}

@Composable
internal fun PersonalizationBlockEditor(
    homeBlockOrder: List<HomeBlock>,
    homeBlockEnabled: Map<HomeBlock, Boolean>,
    onHomeBlockEnabledChange: (HomeBlock, Boolean) -> Unit,
    onMoveHomeBlockUp: (HomeBlock) -> Unit,
    onMoveHomeBlockDown: (HomeBlock) -> Unit,
    onResetHomeBlocks: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "首页模块",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "当前主题会单独保存下方模块的显示和顺序。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        homeBlockOrder.forEachIndexed { index, block ->
            HomeBlockRow(
                block = block,
                enabled = homeBlockEnabled[block] ?: true,
                canMoveUp = index > 0,
                canMoveDown = index < homeBlockOrder.lastIndex,
                onEnabledChange = { enabled -> onHomeBlockEnabledChange(block, enabled) },
                onMoveUp = { onMoveHomeBlockUp(block) },
                onMoveDown = { onMoveHomeBlockDown(block) }
            )
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onResetHomeBlocks
        ) {
            Text("恢复默认模块布局")
        }
    }
}

@Composable
private fun ThemeAppliedStatus(themeName: String) {
    val visualTheme = LocalYunJiVisualTheme.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, visualTheme.cardStroke)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            text = "主题/个性化已应用：$themeName",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ThemePreviewCarousel(
    themes: List<VisualTheme>,
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val visualTheme = LocalYunJiVisualTheme.current
    val selectedIndex = themes.indexOfFirst { theme -> theme.key == selectedTheme }
        .coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    LaunchedEffect(selectedIndex) {
        if (themes.isNotEmpty()) {
            listState.animateScrollToItem(selectedIndex)
        }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF0D1A1D),
        border = BorderStroke(1.dp, visualTheme.cardStroke)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(398.dp)
        ) {
            ThemePreviewBackdrop(
                themeKey = selectedTheme,
                modifier = Modifier.fillMaxSize()
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 22.dp),
                state = listState,
                contentPadding = PaddingValues(horizontal = 60.dp),
                horizontalArrangement = Arrangement.spacedBy(22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(themes, key = { theme -> theme.key }) { theme ->
                    val enabled = theme.isSelectable
                    val selected = theme.key == selectedTheme
                    val scale = if (selected) 1f else 0.84f
                    PhonePreview(
                        modifier = Modifier
                            .width(194.dp)
                            .height(334.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                alpha = if (enabled) {
                                    if (selected) 1f else 0.72f
                                } else {
                                    0.42f
                                }
                            }
                            .then(
                                if (enabled) {
                                    Modifier.clickable { onThemeSelected(theme.key) }
                                } else {
                                    Modifier
                                }
                            ),
                        selectedTheme = theme.key
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedThemePanel(
    theme: VisualTheme,
    selected: Boolean,
    onThemeSelected: (String) -> Unit
) {
    val skin = ThemeSkinCatalog.getSkin(theme.key)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(54.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier.background(themeBrush(theme.key)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = skin.previewTitle.take(1),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = theme.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (selected) "应用中 | ${skin.previewSubtitle}" else theme.shortDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (theme.isSelectable && !selected) {
            Button(onClick = { onThemeSelected(theme.key) }) {
                Text("应用")
            }
        } else {
            OutlinedButton(enabled = false, onClick = {}) {
                Text(if (selected) "已应用" else "未开放")
            }
        }
    }
}

@Composable
private fun PhonePreview(
    modifier: Modifier,
    selectedTheme: String
) {
    val skin = ThemeSkinCatalog.getSkin(selectedTheme)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.42f)),
        shadowElevation = 14.dp
    ) {
        Box(
            modifier = Modifier.background(themeBrush(selectedTheme))
        ) {
            ThemePreviewBackdrop(
                themeKey = selectedTheme,
                modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "龙岗区 · ${skin.previewTitle}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "26",
                        fontSize = 58.sp,
                        lineHeight = 58.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Light
                    )
                    Text(
                        text = "°C",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
                Text(
                    text = "30 / 18°C",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.82f)
                )
                Text(
                    text = if (skin.runtimeSelectable) "晴  |  空气优" else "预留皮肤位",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.88f)
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        text = if (skin.runtimeSelectable) {
                            "未来8小时晴天，明日转多云"
                        } else {
                            "后续版本开放"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { index ->
                        MiniForecastTile(index)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.MiniForecastTile(index: Int) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "${11 + index}:00",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.76f),
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(Color.White.copy(alpha = 0.88f), CircleShape)
        )
        Text(
            text = "${26 + index}°",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            maxLines = 1
        )
    }
}

@Composable
private fun ThemeCardGrid(
    themes: List<VisualTheme>,
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "更多皮肤",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        themes.chunked(3).forEach { rowThemes ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowThemes.forEach { theme ->
                    ThemeCard(
                        modifier = Modifier.weight(1f),
                        theme = theme,
                        selected = theme.key == selectedTheme,
                        onThemeSelected = onThemeSelected
                    )
                }
                repeat(3 - rowThemes.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(
    modifier: Modifier,
    theme: VisualTheme,
    selected: Boolean,
    onThemeSelected: (String) -> Unit
) {
    val enabled = theme.isSelectable
    Column(
        modifier = modifier.alpha(if (enabled) 1f else 0.58f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
                .then(
                    if (enabled) {
                        Modifier.clickable { onThemeSelected(theme.key) }
                    } else {
                        Modifier
                    }
                ),
            shape = RoundedCornerShape(18.dp),
            color = Color.Transparent,
            border = BorderStroke(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            )
        ) {
            Box(
                modifier = Modifier
                    .background(themeBrush(theme.key))
            ) {
                ThemePreviewBackdrop(
                    themeKey = theme.key,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    modifier = Modifier.padding(14.dp),
                    text = if (enabled) "26°C" else "待开放",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Light
                )
                if (selected) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            text = "✓",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp),
                    text = if (enabled) "晴  |  空气优" else "预留皮肤位",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.86f)
                )
            }
        }
        Text(
            text = theme.displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = if (selected) "应用中" else theme.shortDescription,
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
}

private fun themeBrush(themeKey: String): Brush {
    val skin = ThemeSkinCatalog.getSkin(themeKey)
    return Brush.verticalGradient(
        listOf(skin.previewTop, skin.previewMiddle, skin.previewBottom)
    )
}

@Composable
private fun ThemePreviewBackdrop(
    themeKey: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.background(themeBrush(themeKey))) {
        when (themeKey) {
            VisualThemeUtils.THEME_PANORAMA -> {
                Image(
                    painter = painterResource(R.drawable.theme_panorama_preview),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                PanoramaWeatherBackdrop(Modifier.fillMaxSize())
            }
            VisualThemeUtils.THEME_SKY -> OfficialWeatherBackdrop(Modifier.fillMaxSize())
            else -> ReservedThemeBackdrop(Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun OfficialWeatherBackdrop(modifier: Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(
            color = Color.White.copy(alpha = 0.26f),
            radius = size.width * 0.42f,
            center = Offset(size.width * 0.76f, size.height * 0.12f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.18f),
            radius = size.width * 0.28f,
            center = Offset(size.width * 0.28f, size.height * 0.36f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.14f),
            radius = size.width * 0.34f,
            center = Offset(size.width * 0.76f, size.height * 0.42f)
        )
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color.Transparent, Color.White.copy(alpha = 0.34f)),
                startY = size.height * 0.50f,
                endY = size.height
            )
        )
    }
}

@Composable
private fun PanoramaWeatherBackdrop(modifier: Modifier) {
    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.radialGradient(
                listOf(
                    Color.White.copy(alpha = 0.20f),
                    Color(0xFFE3F7FF).copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.62f, size.height * 0.10f),
                radius = size.width * 0.72f
            )
        )
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color.Transparent,
                    Color(0xFF062126).copy(alpha = 0.10f),
                    Color(0xFF061619).copy(alpha = 0.30f)
                ),
                startY = size.height * 0.42f,
                endY = size.height
            )
        )
    }
}

@Composable
private fun ReservedThemeBackdrop(modifier: Modifier) {
    Canvas(modifier = modifier) {
        drawRect(color = Color(0xFF223135).copy(alpha = 0.36f))
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color.Transparent, Color.Black.copy(alpha = 0.24f))
            )
        )
    }
}

private const val MAX_THEME_CARDS = 4
