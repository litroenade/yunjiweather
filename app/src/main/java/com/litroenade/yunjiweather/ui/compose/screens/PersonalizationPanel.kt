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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litroenade.yunjiweather.R
import com.litroenade.yunjiweather.data.model.CustomThemeCropAnchor
import com.litroenade.yunjiweather.data.model.CustomThemeWeatherKey
import com.litroenade.yunjiweather.ui.compose.UriImage
import com.litroenade.yunjiweather.ui.compose.home.modules.HomeModuleDefinition
import com.litroenade.yunjiweather.ui.compose.theme.LocalYunJiVisualTheme
import com.litroenade.yunjiweather.ui.compose.theme.skins.ThemeSkinCatalog
import com.litroenade.yunjiweather.utils.VisualTheme
import com.litroenade.yunjiweather.utils.VisualThemeUtils

@Composable
internal fun PersonalizationPanel(
    themes: List<VisualTheme>,
    selectedTheme: String,
    customThemeImageUri: String = "",
    customThemeCropAnchor: String = CustomThemeCropAnchor.CENTER,
    customThemeImageUris: Map<String, String> = emptyMap(),
    customThemeCropAnchors: Map<String, String> = emptyMap(),
    draftCustomThemeImageUris: Map<String, String> = emptyMap(),
    draftCustomThemeCropAnchors: Map<String, String> = emptyMap(),
    customThemeEditorMessage: String = "",
    customThemeImporting: Boolean = false,
    onThemeSelected: (String) -> Unit,
    onPickCustomThemeImage: (String) -> Unit = {},
    onCustomThemeCropAnchorChanged: (String, String) -> Unit = { _, _ -> },
    onDraftCustomThemeCropAnchorChanged: (String, String) -> Unit = { _, _ -> },
    onApplyCustomThemeDraft: (Map<String, String>, Map<String, String>) -> Unit = { _, _ -> },
    onDiscardCustomThemeDraft: () -> Unit = {},
    onClearCustomThemeImage: () -> Unit = {}
) {
    val visibleThemes = themes.take(MAX_THEME_CARDS)
    val selectedThemeModel = visibleThemes.firstOrNull { theme -> theme.key == selectedTheme }
        ?: visibleThemes.firstOrNull()
    val hasCustomThemeDraft = draftCustomThemeImageUris.isNotEmpty()
    val previewThemeKey = if (hasCustomThemeDraft) VisualThemeUtils.THEME_CUSTOM_1 else selectedThemeModel?.key ?: selectedTheme
    val previewCustomImageUri = firstThemeImage(draftCustomThemeImageUris)
        .ifBlank { firstThemeImage(customThemeImageUris) }
        .ifBlank { customThemeImageUri }
    val previewCustomCropAnchor = cropAnchorForPreview(
        previewCustomImageUri,
        draftCustomThemeImageUris,
        draftCustomThemeCropAnchors,
        customThemeImageUris,
        customThemeCropAnchors,
        customThemeCropAnchor
    )
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        ThemeAppliedStatus(themeName = selectedThemeModel?.displayName ?: "默认主题")
        CurrentThemePreview(
            themeKey = previewThemeKey,
            customThemeImageUri = previewCustomImageUri,
            customThemeCropAnchor = previewCustomCropAnchor
        )
        if (selectedThemeModel != null) {
            SelectedThemePanel(
                theme = selectedThemeModel,
                selected = selectedThemeModel.key == selectedTheme,
                hasCustomThemeImage = customThemeImageUris.isNotEmpty() || customThemeImageUri.isNotBlank(),
                onThemeSelected = onThemeSelected,
                onPickCustomThemeImage = onPickCustomThemeImage
            )
        }
        if (selectedThemeModel?.key == VisualThemeUtils.THEME_CUSTOM_1 || hasCustomThemeDraft) {
            CustomThemeControls(
                customThemeImageUri = customThemeImageUri,
                customThemeCropAnchor = customThemeCropAnchor,
                customThemeImageUris = customThemeImageUris,
                customThemeCropAnchors = customThemeCropAnchors,
                draftCustomThemeImageUris = draftCustomThemeImageUris,
                draftCustomThemeCropAnchors = draftCustomThemeCropAnchors,
                customThemeEditorMessage = customThemeEditorMessage,
                customThemeImporting = customThemeImporting,
                onPickCustomThemeImage = onPickCustomThemeImage,
                onCustomThemeCropAnchorChanged = onCustomThemeCropAnchorChanged,
                onDraftCustomThemeCropAnchorChanged = onDraftCustomThemeCropAnchorChanged,
                onApplyCustomThemeDraft = onApplyCustomThemeDraft,
                onDiscardCustomThemeDraft = onDiscardCustomThemeDraft,
                onClearCustomThemeImage = onClearCustomThemeImage
            )
        }
        ThemeCardGrid(
            themes = visibleThemes,
            selectedTheme = selectedTheme,
            customThemeImageUri = previewCustomImageUri,
            customThemeCropAnchor = previewCustomCropAnchor,
            onThemeSelected = onThemeSelected,
            onPickCustomThemeImage = onPickCustomThemeImage
        )
    }
}

@Composable
internal fun PersonalizationBlockEditor(
    homeModules: List<HomeModuleDefinition>,
    homeModuleEnabled: Map<String, Boolean>,
    onHomeModuleEnabledChange: (HomeModuleDefinition, Boolean) -> Unit,
    onMoveHomeModuleUp: (HomeModuleDefinition) -> Unit,
    onMoveHomeModuleDown: (HomeModuleDefinition) -> Unit,
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
        homeModules.forEachIndexed { index, module ->
            HomeBlockRow(
                module = module,
                enabled = homeModuleEnabled[module.key] ?: module.defaultEnabled,
                canMoveUp = index > 0,
                canMoveDown = index < homeModules.lastIndex,
                onEnabledChange = { enabled -> onHomeModuleEnabledChange(module, enabled) },
                onMoveUp = { onMoveHomeModuleUp(module) },
                onMoveDown = { onMoveHomeModuleDown(module) }
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
private fun CurrentThemePreview(
    themeKey: String,
    customThemeImageUri: String,
    customThemeCropAnchor: String
) {
    val visualTheme = LocalYunJiVisualTheme.current
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
                themeKey = themeKey,
                modifier = Modifier.fillMaxSize(),
                includeImage = false,
                customThemeImageUri = customThemeImageUri,
                customThemeCropAnchor = customThemeCropAnchor
            )
            PhonePreview(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(214.dp)
                    .height(344.dp),
                selectedTheme = themeKey,
                customThemeImageUri = customThemeImageUri,
                customThemeCropAnchor = customThemeCropAnchor
            )
        }
    }
}

@Composable
private fun SelectedThemePanel(
    theme: VisualTheme,
    selected: Boolean,
    hasCustomThemeImage: Boolean,
    onThemeSelected: (String) -> Unit,
    onPickCustomThemeImage: (String) -> Unit
) {
    val skin = ThemeSkinCatalog.getSkin(theme.key)
    val needsCustomImage = theme.key == VisualThemeUtils.THEME_CUSTOM_1 && !hasCustomThemeImage
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        if (needsCustomImage) {
            Button(onClick = { onPickCustomThemeImage(CustomThemeWeatherKey.FALLBACK) }) {
                Text("选择图片")
            }
        } else if (theme.isSelectable && !selected) {
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
private fun CustomThemeControls(
    customThemeImageUri: String,
    customThemeCropAnchor: String,
    customThemeImageUris: Map<String, String>,
    customThemeCropAnchors: Map<String, String>,
    draftCustomThemeImageUris: Map<String, String>,
    draftCustomThemeCropAnchors: Map<String, String>,
    customThemeEditorMessage: String,
    customThemeImporting: Boolean,
    onPickCustomThemeImage: (String) -> Unit,
    onCustomThemeCropAnchorChanged: (String, String) -> Unit,
    onDraftCustomThemeCropAnchorChanged: (String, String) -> Unit,
    onApplyCustomThemeDraft: (Map<String, String>, Map<String, String>) -> Unit,
    onDiscardCustomThemeDraft: () -> Unit,
    onClearCustomThemeImage: () -> Unit
) {
    val hasDraft = draftCustomThemeImageUris.isNotEmpty()
    val hasSavedImages = customThemeImageUris.isNotEmpty() || customThemeImageUri.isNotBlank()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.36f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "自定义主题编辑器",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = when {
                            customThemeImporting -> "正在导入本地图片"
                            hasDraft -> "已有未保存图片，确认后保存为自定义主题"
                            !hasSavedImages -> "为不同天气上传图片，保存后会成为可应用主题"
                            else -> "当前自定义主题会按天气自动切换背景"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            CustomThemeWeatherKey.orderedKeys().forEach { weatherKey ->
                val draftImageUri = draftCustomThemeImageUris[weatherKey].orEmpty()
                val savedImageUri = customThemeImageUris[weatherKey].orEmpty().ifBlank {
                    if (weatherKey == CustomThemeWeatherKey.FALLBACK) customThemeImageUri else ""
                }
                val hasSlotDraft = draftImageUri.isNotBlank()
                val imageUri = draftImageUri.ifBlank { savedImageUri }
                val cropAnchor = if (hasSlotDraft) {
                    draftCustomThemeCropAnchors[weatherKey] ?: CustomThemeCropAnchor.CENTER
                } else {
                    customThemeCropAnchors[weatherKey]
                        ?: if (weatherKey == CustomThemeWeatherKey.FALLBACK) customThemeCropAnchor else CustomThemeCropAnchor.CENTER
                }
                CustomThemeWeatherSlotRow(
                    weatherKey = weatherKey,
                    imageUri = imageUri,
                    cropAnchor = cropAnchor,
                    draft = hasSlotDraft,
                    enabled = !customThemeImporting,
                    onPickCustomThemeImage = onPickCustomThemeImage,
                    onCropAnchorChanged = { anchor ->
                        if (hasSlotDraft) {
                            onDraftCustomThemeCropAnchorChanged(weatherKey, anchor)
                        } else {
                            onCustomThemeCropAnchorChanged(weatherKey, anchor)
                        }
                    }
                )
            }
            if (customThemeEditorMessage.isNotBlank()) {
                Text(
                    text = customThemeEditorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                if (hasDraft) {
                    TextButton(onClick = onDiscardCustomThemeDraft) {
                        Text("放弃草稿")
                    }
                    Button(onClick = { onApplyCustomThemeDraft(draftCustomThemeImageUris, draftCustomThemeCropAnchors) }) {
                        Text("保存并应用")
                    }
                } else if (hasSavedImages) {
                    TextButton(onClick = onClearCustomThemeImage) {
                        Text("移除自定义主题")
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomThemeWeatherSlotRow(
    weatherKey: String,
    imageUri: String,
    cropAnchor: String,
    draft: Boolean,
    enabled: Boolean,
    onPickCustomThemeImage: (String) -> Unit,
    onCropAnchorChanged: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.30f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(width = 76.dp, height = 100.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1B2B30)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (imageUri.isNotBlank()) {
                        UriImage(
                            uriString = imageUri,
                            cropAnchor = cropAnchor,
                            modifier = Modifier.fillMaxSize()
                        )
                        CustomThemeBackdropScrim(Modifier.fillMaxSize())
                    } else {
                        ReservedThemeBackdrop(Modifier.fillMaxSize())
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = CustomThemeWeatherKey.displayName(weatherKey),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = when {
                        draft -> "待保存"
                        imageUri.isNotBlank() -> "已配置"
                        else -> "未配置，将回退默认图"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CropAnchorButton("顶", CustomThemeCropAnchor.TOP, cropAnchor, onCropAnchorChanged)
                    CropAnchorButton("中", CustomThemeCropAnchor.CENTER, cropAnchor, onCropAnchorChanged)
                    CropAnchorButton("底", CustomThemeCropAnchor.BOTTOM, cropAnchor, onCropAnchorChanged)
                }
            }
            OutlinedButton(
                enabled = enabled,
                onClick = { onPickCustomThemeImage(weatherKey) }
            ) {
                Text(if (imageUri.isBlank()) "选择" else "替换")
            }
        }
    }
}

@Composable
private fun RowScope.CropAnchorButton(
    text: String,
    anchor: String,
    selectedAnchor: String,
    onCustomThemeCropAnchorChanged: (String) -> Unit
) {
    val selected = anchor == selectedAnchor
    if (selected) {
        Button(
            modifier = Modifier.weight(1f),
            onClick = { onCustomThemeCropAnchorChanged(anchor) }
        ) {
            Text(text)
        }
    } else {
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = { onCustomThemeCropAnchorChanged(anchor) }
        ) {
            Text(text)
        }
    }
}

@Composable
private fun PhonePreview(
    modifier: Modifier,
    selectedTheme: String,
    customThemeImageUri: String,
    customThemeCropAnchor: String
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
                modifier = Modifier.fillMaxSize(),
                customThemeImageUri = customThemeImageUri,
                customThemeCropAnchor = customThemeCropAnchor
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
    customThemeImageUri: String,
    customThemeCropAnchor: String,
    onThemeSelected: (String) -> Unit,
    onPickCustomThemeImage: (String) -> Unit
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
                        customThemeImageUri = customThemeImageUri,
                        customThemeCropAnchor = customThemeCropAnchor,
                        onThemeSelected = onThemeSelected,
                        onPickCustomThemeImage = onPickCustomThemeImage
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
    customThemeImageUri: String,
    customThemeCropAnchor: String,
    onThemeSelected: (String) -> Unit,
    onPickCustomThemeImage: (String) -> Unit
) {
    val enabled = theme.isSelectable
    val needsCustomImage = theme.key == VisualThemeUtils.THEME_CUSTOM_1 && customThemeImageUri.isBlank()
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
                        Modifier.clickable {
                            if (needsCustomImage) {
                                onPickCustomThemeImage(CustomThemeWeatherKey.FALLBACK)
                            } else {
                                onThemeSelected(theme.key)
                            }
                        }
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
                    modifier = Modifier.fillMaxSize(),
                    customThemeImageUri = customThemeImageUri,
                    customThemeCropAnchor = customThemeCropAnchor
                )
                Text(
                    modifier = Modifier.padding(14.dp),
                    text = if (enabled) "26°C" else "待开放",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Light
                )
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
    module: HomeModuleDefinition,
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
                        text = module.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = module.shortDescription,
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

private fun firstThemeImage(images: Map<String, String>): String {
    return images[CustomThemeWeatherKey.FALLBACK]
        ?: CustomThemeWeatherKey.orderedKeys().firstNotNullOfOrNull { key -> images[key]?.takeIf { it.isNotBlank() } }
        ?: ""
}

private fun cropAnchorForPreview(
    previewImageUri: String,
    draftImages: Map<String, String>,
    draftCropAnchors: Map<String, String>,
    savedImages: Map<String, String>,
    savedCropAnchors: Map<String, String>,
    fallbackCropAnchor: String
): String {
    if (previewImageUri.isBlank()) {
        return fallbackCropAnchor
    }
    val draftKey = draftImages.entries.firstOrNull { entry -> entry.value == previewImageUri }?.key
    if (draftKey != null) {
        return draftCropAnchors[draftKey] ?: fallbackCropAnchor
    }
    val savedKey = savedImages.entries.firstOrNull { entry -> entry.value == previewImageUri }?.key
    return if (savedKey == null) {
        fallbackCropAnchor
    } else {
        savedCropAnchors[savedKey] ?: fallbackCropAnchor
    }
}

@Composable
private fun ThemePreviewBackdrop(
    themeKey: String,
    modifier: Modifier = Modifier,
    includeImage: Boolean = true,
    customThemeImageUri: String = "",
    customThemeCropAnchor: String = CustomThemeCropAnchor.CENTER
) {
    Box(modifier = modifier.background(themeBrush(themeKey))) {
        when (themeKey) {
            VisualThemeUtils.THEME_PANORAMA -> {
                if (includeImage) {
                    Image(
                        painter = painterResource(R.drawable.theme_panorama_preview),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                PanoramaWeatherBackdrop(Modifier.fillMaxSize())
            }
            VisualThemeUtils.THEME_SKY -> OfficialWeatherBackdrop(Modifier.fillMaxSize())
            VisualThemeUtils.THEME_CUSTOM_1 -> {
                if (includeImage && customThemeImageUri.isNotBlank()) {
                    UriImage(
                        uriString = customThemeImageUri,
                        cropAnchor = customThemeCropAnchor,
                        modifier = Modifier.fillMaxSize()
                    )
                    CustomThemeBackdropScrim(Modifier.fillMaxSize())
                } else {
                    ReservedThemeBackdrop(Modifier.fillMaxSize())
                }
            }
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
private fun CustomThemeBackdropScrim(modifier: Modifier) {
    Canvas(modifier = modifier) {
        drawRect(color = Color.Black.copy(alpha = 0.10f))
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.28f)
                ),
                startY = size.height * 0.38f,
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
