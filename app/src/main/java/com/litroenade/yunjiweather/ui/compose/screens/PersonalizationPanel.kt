package com.litroenade.yunjiweather.ui.compose.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litroenade.yunjiweather.R
import com.litroenade.yunjiweather.data.model.CustomThemeAsset
import com.litroenade.yunjiweather.data.model.CustomThemeCropAnchor
import com.litroenade.yunjiweather.data.model.CustomThemeProfile
import com.litroenade.yunjiweather.data.model.CustomThemeResolver
import com.litroenade.yunjiweather.data.model.CustomThemeRule
import com.litroenade.yunjiweather.data.model.CustomThemeWeatherKey
import com.litroenade.yunjiweather.ui.compose.UriImage
import com.litroenade.yunjiweather.ui.compose.home.modules.HomeModuleDefinition
import com.litroenade.yunjiweather.ui.compose.theme.CustomThemeImage
import com.litroenade.yunjiweather.ui.compose.theme.skins.ThemeSkinCatalog
import com.litroenade.yunjiweather.utils.VisualTheme
import com.litroenade.yunjiweather.utils.VisualThemeUtils
import kotlin.math.abs

private val PersonalizationSheetShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
private val PersonalizationPhoneShape = RoundedCornerShape(22.dp)
private val PersonalizationCardShape = RoundedCornerShape(8.dp)
private val PersonalizationSmallShape = RoundedCornerShape(6.dp)
private val PersonalizationIndicatorShape = RoundedCornerShape(99.dp)

@Composable
internal fun PersonalizationPanel(
    themes: List<VisualTheme>,
    selectedTheme: String,
    customThemeImageUri: String = "",
    customThemeCropAnchor: String = CustomThemeCropAnchor.CENTER,
    customThemeImageUris: Map<String, String> = emptyMap(),
    customThemeCropAnchors: Map<String, String> = emptyMap(),
    customThemeProfile: CustomThemeProfile = CustomThemeProfile.empty(),
    draftCustomThemeImageUris: Map<String, String> = emptyMap(),
    draftCustomThemeCropAnchors: Map<String, String> = emptyMap(),
    onThemeSelected: (String) -> Unit,
    onOpenCustomThemeEditor: () -> Unit = {}
) {
    val visibleThemes = themes
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
    val previewSkin = ThemeSkinCatalog.getSkin(previewThemeKey)
    val isDarkPalette = previewSkin.previewBottom.luminance() < 0.35f
    val marketContainerColor = previewSkin.previewBottom.copy(alpha = if (isDarkPalette) 0.94f else 0.96f)
    val marketTitleColor = if (isDarkPalette) Color.White else Color(0xFF15242B)
    val marketSecondaryColor = if (isDarkPalette) {
        Color.White.copy(alpha = 0.58f)
    } else {
        Color(0x9915242B)
    }
    val hasCustomAssets = customThemeProfile.assets.isNotEmpty() ||
            customThemeImageUris.any { entry -> entry.value.isNotBlank() } ||
            customThemeImageUri.isNotBlank() ||
            hasCustomThemeDraft
    var detailThemeKey by remember { mutableStateOf<String?>(null) }
    BackHandler(enabled = detailThemeKey != null) {
        detailThemeKey = null
    }
    AnimatedContent(
        targetState = detailThemeKey,
        transitionSpec = {
            val direction = if (targetState == null) -1 else 1
            val enter = slideInHorizontally(
                animationSpec = tween(260, easing = FastOutSlowInEasing)
            ) { width -> width * direction / 4 } + fadeIn(
                animationSpec = tween(160, easing = FastOutSlowInEasing)
            )
            val exit = slideOutHorizontally(
                animationSpec = tween(220, easing = FastOutSlowInEasing)
            ) { width -> -width * direction / 4 } + fadeOut(
                animationSpec = tween(140, easing = FastOutSlowInEasing)
            )
            (enter togetherWith exit).using(SizeTransform(clip = false))
        },
        label = "personalization-theme-page"
    ) { openedThemeKey ->
        val openedTheme = openedThemeKey?.let { key -> visibleThemes.firstOrNull { theme -> theme.key == key } }
        if (openedTheme == null) {
            ThemeStorePage(
                previewThemeKey = previewThemeKey,
                previewCustomImageUri = previewCustomImageUri,
                previewCustomCropAnchor = previewCustomCropAnchor,
                customThemeImageUris = customThemeImageUris,
                customThemeCropAnchors = customThemeCropAnchors,
                customThemeProfile = customThemeProfile,
                draftCustomThemeImageUris = draftCustomThemeImageUris,
                draftCustomThemeCropAnchors = draftCustomThemeCropAnchors,
                fadeColor = marketContainerColor,
                selectedThemeModel = selectedThemeModel,
                visibleThemes = visibleThemes,
                selectedTheme = selectedTheme,
                hasCustomAssets = hasCustomAssets,
                customAssetCount = customThemeProfile.assets.size,
                customRuleCount = customThemeProfile.rules.size,
                titleColor = marketTitleColor,
                secondaryColor = marketSecondaryColor,
                onOpenThemeDetail = { themeKey -> detailThemeKey = themeKey },
                onOpenCustomThemeEditor = onOpenCustomThemeEditor
            )
        } else {
            ThemeDetailPage(
                theme = openedTheme,
                selectedTheme = selectedTheme,
                customThemeImageUri = previewCustomImageUri,
                customThemeCropAnchor = previewCustomCropAnchor,
                customThemeImageUris = customThemeImageUris,
                customThemeCropAnchors = customThemeCropAnchors,
                customThemeProfile = customThemeProfile,
                draftCustomThemeImageUris = draftCustomThemeImageUris,
                draftCustomThemeCropAnchors = draftCustomThemeCropAnchors,
                hasCustomAssets = hasCustomAssets,
                customAssetCount = customThemeProfile.assets.size,
                customRuleCount = customThemeProfile.rules.size,
                onBack = { detailThemeKey = null },
                onApplyTheme = {
                    onThemeSelected(openedTheme.key)
                    detailThemeKey = null
                },
                onOpenCustomThemeEditor = {
                    onThemeSelected(VisualThemeUtils.THEME_CUSTOM_1)
                    onOpenCustomThemeEditor()
                }
            )
        }
    }
}

@Composable
internal fun CustomThemeEditorPanel(
    customThemeImageUri: String = "",
    customThemeCropAnchor: String = CustomThemeCropAnchor.CENTER,
    customThemeImageUris: Map<String, String> = emptyMap(),
    customThemeCropAnchors: Map<String, String> = emptyMap(),
    customThemeProfile: CustomThemeProfile = CustomThemeProfile.empty(),
    draftCustomThemeImageUris: Map<String, String> = emptyMap(),
    draftCustomThemeCropAnchors: Map<String, String> = emptyMap(),
    customThemeEditorMessage: String = "",
    customThemeImporting: Boolean = false,
    onBackToThemeStore: () -> Unit = {},
    onOpenHomeBlockEditor: () -> Unit = {},
    onPickCustomThemeImage: (String) -> Unit = {},
    onPickMultipleCustomThemeImages: () -> Unit = {},
    onCustomThemeCropAnchorChanged: (String, String) -> Unit = { _, _ -> },
    onDraftCustomThemeCropAnchorChanged: (String, String) -> Unit = { _, _ -> },
    onApplyCustomThemeDraft: (Map<String, String>, Map<String, String>) -> Unit = { _, _ -> },
    onDiscardCustomThemeDraft: () -> Unit = {},
    onClearCustomThemeWeatherImage: (String) -> Unit = {},
    onClearCustomThemeImage: () -> Unit = {}
) {
    var selectedSection by remember { mutableStateOf(CustomThemeEditorSection.MATERIALS) }
    val editorSkin = ThemeSkinCatalog.getSkin(VisualThemeUtils.THEME_CUSTOM_1)
    val editorDarkPalette = editorSkin.previewBottom.luminance() < 0.35f
    val editorPanelColor = editorSkin.previewBottom.copy(alpha = if (editorDarkPalette) 0.94f else 0.96f)
    val editorContentColor = if (editorDarkPalette) Color.White else Color(0xFF15242B)
    val editorSecondaryColor = if (editorDarkPalette) {
        Color.White.copy(alpha = 0.64f)
    } else {
        Color(0x9915242B)
    }
    val editorPreviewImageUri = firstThemeImage(draftCustomThemeImageUris)
        .ifBlank { firstThemeImage(customThemeImageUris) }
        .ifBlank { customThemeImageUri }
    val editorPreviewCropAnchor = cropAnchorForPreview(
        editorPreviewImageUri,
        draftCustomThemeImageUris,
        draftCustomThemeCropAnchors,
        customThemeImageUris,
        customThemeCropAnchors,
        customThemeCropAnchor
    )
    CompositionLocalProvider(LocalContentColor provides editorContentColor) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            CustomThemeEditorHero(
                imageUri = editorPreviewImageUri,
                cropAnchor = editorPreviewCropAnchor,
                mediaType = customThemeMediaTypeForPreview(editorPreviewImageUri),
                fadeColor = editorPanelColor
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-18).dp),
                shape = PersonalizationSheetShape,
                color = editorPanelColor,
                contentColor = editorContentColor
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "素材、规则、首页布局和桌面小组件分开配置。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = editorSecondaryColor
                        )
                        OutlinedButton(onClick = onBackToThemeStore) {
                            Text("皮肤商店")
                        }
                    }
                    CustomThemeEditorTabs(
                        selectedSection = selectedSection,
                        onSectionSelected = { selectedSection = it }
                    )
                    AnimatedContent(
                        targetState = selectedSection,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(160, easing = FastOutSlowInEasing)) togetherWith
                                    fadeOut(animationSpec = tween(120, easing = FastOutSlowInEasing))
                        },
                        label = "custom-theme-editor-section"
                    ) { section ->
                        when (section) {
                            CustomThemeEditorSection.MATERIALS -> CustomThemeControls(
                                customThemeImageUri = customThemeImageUri,
                                customThemeCropAnchor = customThemeCropAnchor,
                                customThemeImageUris = customThemeImageUris,
                                customThemeCropAnchors = customThemeCropAnchors,
                                customThemeProfile = customThemeProfile,
                                draftCustomThemeImageUris = draftCustomThemeImageUris,
                                draftCustomThemeCropAnchors = draftCustomThemeCropAnchors,
                                customThemeEditorMessage = customThemeEditorMessage,
                                customThemeImporting = customThemeImporting,
                                onPickCustomThemeImage = onPickCustomThemeImage,
                                onPickMultipleCustomThemeImages = onPickMultipleCustomThemeImages,
                                onCustomThemeCropAnchorChanged = onCustomThemeCropAnchorChanged,
                                onDraftCustomThemeCropAnchorChanged = onDraftCustomThemeCropAnchorChanged,
                                onApplyCustomThemeDraft = onApplyCustomThemeDraft,
                                onDiscardCustomThemeDraft = onDiscardCustomThemeDraft,
                                onClearCustomThemeWeatherImage = onClearCustomThemeWeatherImage,
                                onClearCustomThemeImage = onClearCustomThemeImage
                            )
                            CustomThemeEditorSection.RULES -> CustomThemeRuleStudio(customThemeProfile)
                            CustomThemeEditorSection.LAYOUT -> CustomThemeLayoutStudio(
                                customThemeProfile = customThemeProfile,
                                onOpenHomeBlockEditor = onOpenHomeBlockEditor
                            )
                            CustomThemeEditorSection.WIDGETS -> CustomThemeWidgetStudio(
                                customThemeImageUri = customThemeImageUri,
                                customThemeCropAnchor = customThemeCropAnchor,
                                customThemeImageUris = customThemeImageUris,
                                customThemeCropAnchors = customThemeCropAnchors,
                                customThemeProfile = customThemeProfile,
                                draftCustomThemeImageUris = draftCustomThemeImageUris,
                                draftCustomThemeCropAnchors = draftCustomThemeCropAnchors
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomThemeEditorHero(
    imageUri: String,
    cropAnchor: String,
    mediaType: String,
    fadeColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(278.dp)
            .background(themeBrush(VisualThemeUtils.THEME_CUSTOM_1))
    ) {
        ThemePreviewBackdrop(
            themeKey = VisualThemeUtils.THEME_CUSTOM_1,
            modifier = Modifier.fillMaxSize(),
            customThemeImageUri = imageUri,
            customThemeCropAnchor = cropAnchor,
            customThemeMediaType = mediaType
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(118.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, fadeColor),
                        startY = 0f
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 22.dp, vertical = 34.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "自定义主题",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "多素材底图、天气规则和桌面小组件共用同一套配置",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
            style = MaterialTheme.typography.titleSmall,
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
private fun ThemeStorePage(
    previewThemeKey: String,
    previewCustomImageUri: String,
    previewCustomCropAnchor: String,
    customThemeImageUris: Map<String, String>,
    customThemeCropAnchors: Map<String, String>,
    customThemeProfile: CustomThemeProfile,
    draftCustomThemeImageUris: Map<String, String>,
    draftCustomThemeCropAnchors: Map<String, String>,
    fadeColor: Color,
    selectedThemeModel: VisualTheme?,
    visibleThemes: List<VisualTheme>,
    selectedTheme: String,
    hasCustomAssets: Boolean,
    customAssetCount: Int,
    customRuleCount: Int,
    titleColor: Color,
    secondaryColor: Color,
    onOpenThemeDetail: (String) -> Unit,
    onOpenCustomThemeEditor: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        CurrentThemePreview(
            themeKey = previewThemeKey,
            customThemeImageUri = previewCustomImageUri,
            customThemeCropAnchor = previewCustomCropAnchor,
            customThemeImageUris = customThemeImageUris,
            customThemeCropAnchors = customThemeCropAnchors,
            customThemeProfile = customThemeProfile,
            draftCustomThemeImageUris = draftCustomThemeImageUris,
            draftCustomThemeCropAnchors = draftCustomThemeCropAnchors,
            fadeColor = fadeColor
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-18).dp),
            shape = PersonalizationSheetShape,
            color = fadeColor
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                CurrentThemeInfo(
                    theme = selectedThemeModel,
                    hasCustomAssets = hasCustomAssets,
                    customAssetCount = customAssetCount,
                    customRuleCount = customRuleCount,
                    titleColor = titleColor,
                    secondaryColor = secondaryColor,
                    onOpenCustomThemeEditor = onOpenCustomThemeEditor
                )
                ThemeCardGrid(
                    themes = visibleThemes,
                    selectedTheme = selectedTheme,
                    customThemeImageUri = previewCustomImageUri,
                    customThemeCropAnchor = previewCustomCropAnchor,
                    titleColor = titleColor,
                    secondaryColor = secondaryColor,
                    onThemeClicked = onOpenThemeDetail
                )
            }
        }
    }
}

@Composable
private fun ThemeDetailPage(
    theme: VisualTheme,
    selectedTheme: String,
    customThemeImageUri: String,
    customThemeCropAnchor: String,
    customThemeImageUris: Map<String, String>,
    customThemeCropAnchors: Map<String, String>,
    customThemeProfile: CustomThemeProfile,
    draftCustomThemeImageUris: Map<String, String>,
    draftCustomThemeCropAnchors: Map<String, String>,
    hasCustomAssets: Boolean,
    customAssetCount: Int,
    customRuleCount: Int,
    onBack: () -> Unit,
    onApplyTheme: () -> Unit,
    onOpenCustomThemeEditor: () -> Unit
) {
    val isCustomTheme = theme.key == VisualThemeUtils.THEME_CUSTOM_1
    val isApplied = theme.key == selectedTheme
    val skin = ThemeSkinCatalog.getSkin(theme.key)
    val isDarkPalette = skin.previewBottom.luminance() < 0.35f
    val detailContainerColor = skin.previewBottom.copy(alpha = if (isDarkPalette) 0.94f else 0.96f)
    val detailTitleColor = if (isDarkPalette) Color.White else Color(0xFF15242B)
    val detailSecondaryColor = if (isDarkPalette) {
        Color.White.copy(alpha = 0.62f)
    } else {
        Color(0x9915242B)
    }
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        ThemeDetailPreview(
            themeKey = theme.key,
            customThemeImageUri = customThemeImageUri,
            customThemeCropAnchor = customThemeCropAnchor,
            customThemeImageUris = customThemeImageUris,
            customThemeCropAnchors = customThemeCropAnchors,
            customThemeProfile = customThemeProfile,
            draftCustomThemeImageUris = draftCustomThemeImageUris,
            draftCustomThemeCropAnchors = draftCustomThemeCropAnchors,
            fadeColor = detailContainerColor
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-18).dp),
            shape = PersonalizationSheetShape,
            color = detailContainerColor
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 26.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "皮肤详情",
                        style = MaterialTheme.typography.titleMedium,
                        color = detailTitleColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = onBack) {
                        Text("返回")
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(58.dp),
                        shape = PersonalizationSmallShape,
                        color = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isCustomTheme) "自\n定" else "云\n迹",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF18242A),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = theme.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = detailTitleColor,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isCustomTheme && hasCustomAssets) {
                                "$customAssetCount 个素材 | $customRuleCount 条规则"
                            } else if (isCustomTheme) {
                                "上传多张图片/GIF，按天气和时间自动切换"
                            } else {
                                theme.shortDescription
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = detailSecondaryColor
                        )
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = PersonalizationCardShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.26f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "资源简介",
                            style = MaterialTheme.typography.titleSmall,
                            color = detailTitleColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isCustomTheme) {
                                "支持多张图片/GIF，按天气、夜间、清晨/黄昏和雨夜/雪夜规则自动切换。桌面组件沿用同一套素材预览。"
                            } else {
                                theme.shortDescription
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = detailSecondaryColor
                        )
                    }
                }
                if (isCustomTheme) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onOpenCustomThemeEditor
                    ) {
                        Text(if (hasCustomAssets) "继续编辑自定义主题" else "创建自定义主题")
                    }
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isApplied,
                        onClick = onApplyTheme
                    ) {
                        Text(if (isApplied) "已应用" else "应用自定义主题")
                    }
                } else {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isApplied,
                        onClick = onApplyTheme
                    ) {
                        Text(if (isApplied) "已应用" else "应用皮肤")
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeDetailPreview(
    themeKey: String,
    customThemeImageUri: String,
    customThemeCropAnchor: String,
    customThemeImageUris: Map<String, String>,
    customThemeCropAnchors: Map<String, String>,
    customThemeProfile: CustomThemeProfile,
    draftCustomThemeImageUris: Map<String, String>,
    draftCustomThemeCropAnchors: Map<String, String>,
    fadeColor: Color
) {
    var showDesktopCard by remember(themeKey) { mutableStateOf(false) }
    val previewMode = if (showDesktopCard) ThemePreviewMode.DESKTOP_CARD else ThemePreviewMode.HOME
    val backdropAsset = customThemePreviewAsset(
        previewMode = previewMode,
        customThemeImageUri = customThemeImageUri,
        customThemeCropAnchor = customThemeCropAnchor,
        customThemeImageUris = customThemeImageUris,
        customThemeCropAnchors = customThemeCropAnchors,
        customThemeProfile = customThemeProfile,
        draftCustomThemeImageUris = draftCustomThemeImageUris,
        draftCustomThemeCropAnchors = draftCustomThemeCropAnchors
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(392.dp)
            .background(themeBrush(themeKey))
    ) {
        ThemePreviewBackdrop(
            themeKey = themeKey,
            modifier = Modifier.fillMaxSize(),
            includeImage = true,
            customThemeImageUri = backdropAsset.uri,
            customThemeCropAnchor = backdropAsset.cropAnchor,
            customThemeMediaType = backdropAsset.mediaType
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(132.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, fadeColor),
                        startY = 0f
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 116.dp, bottom = 42.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(54.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                ThemeDetailPreviewTab("首页", selected = !showDesktopCard) {
                    showDesktopCard = false
                }
                ThemeDetailPreviewTab("桌面卡片", selected = showDesktopCard) {
                    showDesktopCard = true
                }
            }
            Spacer(Modifier.height(34.dp))
            AnimatedContent(
                targetState = showDesktopCard,
                transitionSpec = {
                    fadeIn(animationSpec = tween(170, easing = FastOutSlowInEasing)) togetherWith
                            fadeOut(animationSpec = tween(130, easing = FastOutSlowInEasing))
                },
                label = "theme-detail-preview-tab"
            ) { desktopCard ->
                if (desktopCard) {
                    ThemeDetailWidgetCard(
                        themeKey = themeKey,
                        customThemeImage = backdropAsset.toCustomThemeImage()
                    )
                } else {
                    PhonePreview(
                        modifier = Modifier
                            .width(154.dp)
                            .height(248.dp),
                        selectedTheme = themeKey,
                        customThemeImageUri = customThemeImageUri,
                        customThemeCropAnchor = customThemeCropAnchor,
                        customThemeImageUris = customThemeImageUris,
                        customThemeCropAnchors = customThemeCropAnchors,
                        customThemeProfile = customThemeProfile,
                        draftCustomThemeImageUris = draftCustomThemeImageUris,
                        draftCustomThemeCropAnchors = draftCustomThemeCropAnchors,
                        previewMode = ThemePreviewMode.HOME
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeDetailPreviewTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.58f),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
        Box(
            modifier = Modifier
                .width(if (selected) 58.dp else 0.dp)
                .height(3.dp)
                .background(
                    if (selected) Color.White else Color.Transparent,
                    PersonalizationIndicatorShape
                )
        )
    }
}

@Composable
private fun ThemeDetailWidgetCard(
    themeKey: String,
    customThemeImage: CustomThemeImage
) {
    Surface(
        modifier = Modifier
            .width(280.dp)
            .height(146.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.40f)),
        shadowElevation = 12.dp
    ) {
        Box(modifier = Modifier.background(themeBrush(themeKey))) {
            ThemePreviewBackdrop(
                themeKey = themeKey,
                modifier = Modifier.fillMaxSize(),
                customThemeImageUri = customThemeImage.uri,
                customThemeCropAnchor = customThemeImage.cropAnchor,
                customThemeMediaType = customThemeImage.mediaType
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "龙岗区",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "26°",
                        fontSize = 44.sp,
                        lineHeight = 46.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Light
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "晴  空气优",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "30 / 18°C",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.82f)
                    )
                    Text(
                        text = "11:00  12:00  13:00",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.74f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentThemePreview(
    themeKey: String,
    customThemeImageUri: String,
    customThemeCropAnchor: String,
    customThemeImageUris: Map<String, String>,
    customThemeCropAnchors: Map<String, String>,
    customThemeProfile: CustomThemeProfile,
    draftCustomThemeImageUris: Map<String, String>,
    draftCustomThemeCropAnchors: Map<String, String>,
    fadeColor: Color
) {
    val previewModes = ThemePreviewMode.values()
    var selectedPreviewMode by remember(themeKey) { mutableStateOf(ThemePreviewMode.HOME) }
    var previewSwitchDirection by remember { mutableStateOf(1) }
    var horizontalSwipeDistance by remember { mutableStateOf(0f) }
    val swipeThresholdPx = with(LocalDensity.current) { 54.dp.toPx() }
    val activePreviewIndex = selectedPreviewMode.ordinalIndex()
    fun selectPreviewMode(mode: ThemePreviewMode) {
        if (mode != selectedPreviewMode) {
            previewSwitchDirection = if (mode.ordinalIndex() > selectedPreviewMode.ordinalIndex()) 1 else -1
            selectedPreviewMode = mode
        }
    }
    fun selectPreviewModeByOffset(offset: Int) {
        selectPreviewMode(selectedPreviewMode.modeAtOffset(offset))
    }
    val previewSwipeModifier = Modifier.pointerInput(selectedPreviewMode, swipeThresholdPx) {
        detectHorizontalDragGestures(
            onDragStart = { horizontalSwipeDistance = 0f },
            onHorizontalDrag = { _, dragAmount -> horizontalSwipeDistance += dragAmount },
            onDragCancel = { horizontalSwipeDistance = 0f },
            onDragEnd = {
                if (abs(horizontalSwipeDistance) >= swipeThresholdPx) {
                    if (horizontalSwipeDistance < 0f) {
                        selectPreviewModeByOffset(1)
                    } else {
                        selectPreviewModeByOffset(-1)
                    }
                }
                horizontalSwipeDistance = 0f
            }
        )
    }
    val backdropAsset = customThemePreviewAsset(
        previewMode = ThemePreviewMode.HOME,
        customThemeImageUri = customThemeImageUri,
        customThemeCropAnchor = customThemeCropAnchor,
        customThemeImageUris = customThemeImageUris,
        customThemeCropAnchors = customThemeCropAnchors,
        customThemeProfile = customThemeProfile,
        draftCustomThemeImageUris = draftCustomThemeImageUris,
        draftCustomThemeCropAnchors = draftCustomThemeCropAnchors
    )
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeBrush(themeKey))
    ) {
        val phoneWidth = (maxWidth * 0.38f).coerceIn(140.dp, 168.dp)
        val phoneHeight = (phoneWidth * 1.60f).coerceIn(224.dp, 270.dp)
        val stageHeight = (phoneHeight + 142.dp).coerceIn(368.dp, 412.dp)
        val sideOffset = (phoneWidth.value * 0.88f).dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(stageHeight)
        ) {
            ThemePreviewBackdrop(
                themeKey = themeKey,
                modifier = Modifier.fillMaxSize(),
                includeImage = true,
                customThemeImageUri = backdropAsset.uri,
                customThemeCropAnchor = backdropAsset.cropAnchor,
                customThemeMediaType = backdropAsset.mediaType
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(154.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, fadeColor),
                            startY = 0f
                        )
                    )
            )
            Box(
                modifier = previewSwipeModifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(phoneHeight + 28.dp),
                contentAlignment = Alignment.Center
            ) {
                PhonePreview(
                    modifier = Modifier
                        .offset(x = -sideOffset)
                        .width(phoneWidth)
                        .height(phoneHeight)
                        .clickable { selectPreviewModeByOffset(-1) }
                        .graphicsLayer {
                            scaleX = 0.86f
                            scaleY = 0.86f
                            alpha = 0.42f
                        },
                    selectedTheme = themeKey,
                    customThemeImageUri = customThemeImageUri,
                    customThemeCropAnchor = customThemeCropAnchor,
                    customThemeImageUris = customThemeImageUris,
                    customThemeCropAnchors = customThemeCropAnchors,
                    customThemeProfile = customThemeProfile,
                    draftCustomThemeImageUris = draftCustomThemeImageUris,
                    draftCustomThemeCropAnchors = draftCustomThemeCropAnchors,
                    previewMode = selectedPreviewMode.modeAtOffset(-1)
                )
                PhonePreview(
                    modifier = Modifier
                        .offset(x = sideOffset)
                        .width(phoneWidth)
                        .height(phoneHeight)
                        .clickable { selectPreviewModeByOffset(1) }
                        .graphicsLayer {
                            scaleX = 0.86f
                            scaleY = 0.86f
                            alpha = 0.42f
                        },
                    selectedTheme = themeKey,
                    customThemeImageUri = customThemeImageUri,
                    customThemeCropAnchor = customThemeCropAnchor,
                    customThemeImageUris = customThemeImageUris,
                    customThemeCropAnchors = customThemeCropAnchors,
                    customThemeProfile = customThemeProfile,
                    draftCustomThemeImageUris = draftCustomThemeImageUris,
                    draftCustomThemeCropAnchors = draftCustomThemeCropAnchors,
                    previewMode = selectedPreviewMode.modeAtOffset(1)
                )
                AnimatedContent(
                    modifier = Modifier
                        .width(phoneWidth)
                        .height(phoneHeight),
                    targetState = selectedPreviewMode,
                    transitionSpec = {
                        val direction = previewSwitchDirection
                        val enter = slideInHorizontally(
                            animationSpec = tween(260, easing = FastOutSlowInEasing)
                        ) { width -> width * direction / 3 } + fadeIn(
                            animationSpec = tween(160, easing = FastOutSlowInEasing)
                        )
                        val exit = slideOutHorizontally(
                            animationSpec = tween(220, easing = FastOutSlowInEasing)
                        ) { width -> -width * direction / 3 } + fadeOut(
                            animationSpec = tween(140, easing = FastOutSlowInEasing)
                        )
                        (enter togetherWith exit).using(SizeTransform(clip = false))
                    },
                    label = "personalization-preview-mode"
                ) { previewMode ->
                    PhonePreview(
                        modifier = Modifier.fillMaxSize(),
                        selectedTheme = themeKey,
                        customThemeImageUri = customThemeImageUri,
                        customThemeCropAnchor = customThemeCropAnchor,
                        customThemeImageUris = customThemeImageUris,
                        customThemeCropAnchors = customThemeCropAnchors,
                        customThemeProfile = customThemeProfile,
                        draftCustomThemeImageUris = draftCustomThemeImageUris,
                        draftCustomThemeCropAnchors = draftCustomThemeCropAnchors,
                        previewMode = previewMode
                    )
                }
            }
            PersonalizationPageDots(
                active = activePreviewIndex,
                count = previewModes.size,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp)
            )
        }
    }
}

private enum class ThemePreviewMode {
    HOME,
    DESKTOP_CARD,
    CITY_MANAGEMENT
}

private fun ThemePreviewMode.ordinalIndex(): Int {
    return when (this) {
        ThemePreviewMode.HOME -> 0
        ThemePreviewMode.DESKTOP_CARD -> 1
        ThemePreviewMode.CITY_MANAGEMENT -> 2
    }
}

private fun ThemePreviewMode.modeAtOffset(offset: Int): ThemePreviewMode {
    val modes = ThemePreviewMode.values()
    val nextIndex = (ordinalIndex() + offset + modes.size) % modes.size
    return modes[nextIndex]
}

@Composable
private fun PersonalizationPageDots(
    active: Int,
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val selected = index == active.coerceIn(0, count - 1)
            val dotWidth by animateDpAsState(
                targetValue = if (selected) 22.dp else 6.dp,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = "personalization-dot-width"
            )
            val dotAlpha by animateFloatAsState(
                targetValue = if (selected) 0.92f else 0.20f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = "personalization-dot-alpha"
            )
            Box(
                modifier = Modifier
                    .width(dotWidth)
                    .height(if (selected) 7.dp else 6.dp)
                    .background(
                        Color.White.copy(alpha = dotAlpha),
                        PersonalizationIndicatorShape
                    )
            )
        }
    }
}

private enum class CustomThemeEditorSection(val title: String) {
    MATERIALS("素材"),
    RULES("规则"),
    LAYOUT("布局"),
    WIDGETS("小组件")
}

@Composable
private fun CurrentThemeInfo(
    theme: VisualTheme?,
    hasCustomAssets: Boolean,
    customAssetCount: Int,
    customRuleCount: Int,
    titleColor: Color,
    secondaryColor: Color,
    onOpenCustomThemeEditor: () -> Unit
) {
    if (theme == null) {
        return
    }
    val isCustomTheme = theme.key == VisualThemeUtils.THEME_CUSTOM_1
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = PersonalizationSmallShape,
            color = Color.White
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (isCustomTheme) "自\n定" else "云\n迹",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF18242A),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = theme.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = titleColor,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (isCustomTheme) {
                    if (hasCustomAssets) {
                        "$customAssetCount 个素材 | $customRuleCount 条规则"
                    } else {
                        "上传多张图片/GIF，按天气和时间自动切换"
                    }
                } else {
                    theme.shortDescription
                },
                style = MaterialTheme.typography.bodySmall,
                color = secondaryColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (isCustomTheme) {
            Button(onClick = onOpenCustomThemeEditor) {
                Text("编辑")
            }
        } else {
            OutlinedButton(enabled = false, onClick = {}) {
                Text("已应用")
            }
        }
    }
}

@Composable
private fun CustomThemeEditorTabs(
    selectedSection: CustomThemeEditorSection,
    onSectionSelected: (CustomThemeEditorSection) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CustomThemeEditorSection.values().forEach { section ->
            val selected = section == selectedSection
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSectionSelected(section) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(alpha = 0.70f),
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(width = 26.dp, height = 3.dp)
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            PersonalizationIndicatorShape
                        )
                )
            }
        }
    }
}

@Composable
private fun CustomThemeRuleStudio(customThemeProfile: CustomThemeProfile) {
    val secondaryColor = LocalContentColor.current.copy(alpha = 0.68f)
    Column(
        modifier = Modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "规则工作台",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "当前规则会按优先级匹配天气、昼夜和时间段。素材页的每个槽位都会生成一条可运行规则。",
            style = MaterialTheme.typography.bodySmall,
            color = secondaryColor
        )
        if (customThemeProfile.rules.isEmpty()) {
            Text(
                text = "还没有规则。先到“素材”添加默认图或天气图。",
                style = MaterialTheme.typography.bodySmall,
                color = secondaryColor
            )
        } else {
            val contentColor = LocalContentColor.current
            customThemeProfile.rules.sortedByDescending { rule -> rule.priority }.forEach { rule ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = PersonalizationCardShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                    contentColor = contentColor,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = customThemeRuleWeatherText(rule),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${customThemeRuleLightText(rule)} · ${customThemeRuleTimeText(rule)} · 优先级 ${rule.priority}",
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.68f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomThemeLayoutStudio(
    customThemeProfile: CustomThemeProfile,
    onOpenHomeBlockEditor: () -> Unit
) {
    val secondaryColor = LocalContentColor.current.copy(alpha = 0.68f)
    Column(
        modifier = Modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "首页布局",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "模块开关和拖动排序会写入当前自定义主题。",
            style = MaterialTheme.typography.bodySmall,
            color = secondaryColor
        )
        Text(
            text = "已保存顺序 ${customThemeProfile.homeModuleOrder.size} 项，隐藏模块 ${customThemeProfile.disabledHomeModules.size} 项。",
            style = MaterialTheme.typography.bodySmall,
            color = secondaryColor
        )
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenHomeBlockEditor
        ) {
            Text("调整首页模块")
        }
    }
}

@Composable
private fun CustomThemeWidgetStudio(
    customThemeImageUri: String,
    customThemeCropAnchor: String,
    customThemeImageUris: Map<String, String>,
    customThemeCropAnchors: Map<String, String>,
    customThemeProfile: CustomThemeProfile,
    draftCustomThemeImageUris: Map<String, String>,
    draftCustomThemeCropAnchors: Map<String, String>
) {
    val contentColor = LocalContentColor.current
    val secondaryColor = contentColor.copy(alpha = 0.68f)
    val widgetBackground = customThemeWidgetBackground(
        customThemeImageUri = customThemeImageUri,
        customThemeCropAnchor = customThemeCropAnchor,
        customThemeImageUris = customThemeImageUris,
        customThemeCropAnchors = customThemeCropAnchors,
        customThemeProfile = customThemeProfile,
        draftCustomThemeImageUris = draftCustomThemeImageUris,
        draftCustomThemeCropAnchors = draftCustomThemeCropAnchors
    )
    val savedWidgetSlotCount = customThemeImageUris.values.count { imageUri -> imageUri.isNotBlank() } +
            if (customThemeImageUri.isNotBlank() && customThemeImageUris[CustomThemeWeatherKey.FALLBACK].isNullOrBlank()) 1 else 0
    val widgetAssetCount = maxOf(customThemeProfile.assets.size, savedWidgetSlotCount) +
            draftCustomThemeImageUris.count { entry -> entry.value.isNotBlank() }
    val widgetRuleCount = customThemeProfile.rules.size
    Column(
        modifier = Modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "自定义小组件",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "桌面天气、基础天气和生活建议将沿用同一套自定义主题素材；GIF 在系统小组件中会按静态封面降级。",
            style = MaterialTheme.typography.bodySmall,
            color = secondaryColor
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            CustomThemeWidgetPreviewCard("天气时钟") {
                CompactWidgetPreview(widgetBackground)
            }
            CustomThemeWidgetPreviewCard("基础天气") {
                StandardWidgetPreview(widgetBackground)
            }
            CustomThemeWidgetPreviewCard("生活建议") {
                LifeAdviceWidgetPreview(widgetBackground)
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = PersonalizationCardShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
            contentColor = contentColor,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "桌面小组件素材 / 规则",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "$widgetAssetCount / $widgetRuleCount",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CustomThemeWidgetPreviewCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        content()
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = LocalContentColor.current
        )
    }
}

private fun customThemeWidgetBackground(
    customThemeImageUri: String,
    customThemeCropAnchor: String,
    customThemeImageUris: Map<String, String>,
    customThemeCropAnchors: Map<String, String>,
    customThemeProfile: CustomThemeProfile,
    draftCustomThemeImageUris: Map<String, String>,
    draftCustomThemeCropAnchors: Map<String, String>
): CustomThemeImage {
    val draftAsset = firstCustomThemeWidgetAsset(
        imageUris = draftCustomThemeImageUris,
        cropAnchors = draftCustomThemeCropAnchors,
        assetPrefix = "widget-draft"
    )
    if (!draftAsset.isEmpty) {
        return draftAsset.toCustomThemeImage()
    }
    if (!customThemeProfile.isEmpty) {
        val resolved = CustomThemeResolver.resolve(
            customThemeProfile,
            CustomThemeWeatherKey.CLOUDY,
            false,
            15 * 60
        )
        val profileAsset = if (resolved.isEmpty) {
            customThemeProfile.assets.firstOrNull { asset -> !asset.isEmpty } ?: CustomThemeAsset.empty()
        } else {
            resolved
        }
        if (!profileAsset.isEmpty) {
            return profileAsset.toCustomThemeImage()
        }
    }
    val savedAsset = firstCustomThemeWidgetAsset(
        imageUris = customThemeImageUris,
        cropAnchors = customThemeCropAnchors,
        assetPrefix = "widget-saved"
    )
    if (!savedAsset.isEmpty) {
        return savedAsset.toCustomThemeImage()
    }
    val fallbackUri = customThemeImageUris[CustomThemeWeatherKey.FALLBACK].orEmpty()
        .ifBlank { customThemeImageUri }
    if (fallbackUri.isBlank()) {
        return CustomThemeImage()
    }
    return CustomThemeImage(
        uri = fallbackUri,
        cropAnchor = customThemeCropAnchors[CustomThemeWeatherKey.FALLBACK] ?: customThemeCropAnchor,
        mediaType = customThemeMediaTypeForPreview(fallbackUri)
    )
}

private fun firstCustomThemeWidgetAsset(
    imageUris: Map<String, String>,
    cropAnchors: Map<String, String>,
    assetPrefix: String
): CustomThemeAsset {
    CustomThemeWeatherKey.orderedKeys().forEach { weatherKey ->
        val asset = customThemeAssetFromSlot(
            weatherKey = weatherKey,
            imageUris = imageUris,
            cropAnchors = cropAnchors,
            assetPrefix = assetPrefix
        )
        if (!asset.isEmpty) {
            return asset
        }
    }
    return CustomThemeAsset.empty()
}

private fun CustomThemeAsset.toCustomThemeImage(): CustomThemeImage {
    return CustomThemeImage(
        assetId = id,
        uri = uri,
        cropAnchor = cropAnchor,
        mediaType = mediaType
    )
}

@Composable
private fun CustomThemeControls(
    customThemeImageUri: String,
    customThemeCropAnchor: String,
    customThemeImageUris: Map<String, String>,
    customThemeCropAnchors: Map<String, String>,
    customThemeProfile: CustomThemeProfile,
    draftCustomThemeImageUris: Map<String, String>,
    draftCustomThemeCropAnchors: Map<String, String>,
    customThemeEditorMessage: String,
    customThemeImporting: Boolean,
    onPickCustomThemeImage: (String) -> Unit,
    onPickMultipleCustomThemeImages: () -> Unit,
    onCustomThemeCropAnchorChanged: (String, String) -> Unit,
    onDraftCustomThemeCropAnchorChanged: (String, String) -> Unit,
    onApplyCustomThemeDraft: (Map<String, String>, Map<String, String>) -> Unit,
    onDiscardCustomThemeDraft: () -> Unit,
    onClearCustomThemeWeatherImage: (String) -> Unit,
    onClearCustomThemeImage: () -> Unit
) {
    val hasDraft = draftCustomThemeImageUris.isNotEmpty()
    val hasSavedImages = customThemeImageUris.isNotEmpty() || customThemeImageUri.isNotBlank()
    Column(
        modifier = Modifier.fillMaxWidth(),
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
                        customThemeImporting -> "正在导入本地底图"
                        hasDraft -> "已有未保存底图，确认后保存为自定义主题"
                        !hasSavedImages -> "上传图片或 GIF；可按天气、夜间、清晨/黄昏和雨夜/雪夜组合切换"
                        else -> "当前自定义主题按天气、时间和组合规则切换素材，动效实时叠加"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalContentColor.current.copy(alpha = 0.68f)
                )
            }
            OutlinedButton(
                enabled = !customThemeImporting,
                onClick = onPickMultipleCustomThemeImages
            ) {
                Text("批量导入")
            }
        }
        CustomThemeProfileSummary(
            customThemeProfile = customThemeProfile,
            draftCount = draftCustomThemeImageUris.size,
            savedSlotCount = customThemeImageUris.count { entry -> entry.value.isNotBlank() }
        )
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
                ruleText = customThemeRuleSummary(weatherKey),
                draft = hasSlotDraft,
                enabled = !customThemeImporting,
                onPickCustomThemeImage = onPickCustomThemeImage,
                onClearCustomThemeImage = onClearCustomThemeWeatherImage,
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
                TextButton(
                    enabled = !customThemeImporting,
                    onClick = onDiscardCustomThemeDraft
                ) {
                    Text("放弃草稿")
                }
                Button(
                    enabled = !customThemeImporting,
                    onClick = { onApplyCustomThemeDraft(draftCustomThemeImageUris, draftCustomThemeCropAnchors) }
                ) {
                    Text("保存并应用")
                }
            } else if (hasSavedImages) {
                TextButton(
                    enabled = !customThemeImporting,
                    onClick = onClearCustomThemeImage
                ) {
                    Text("移除自定义主题")
                }
            }
        }
    }
}

@Composable
private fun CustomThemeProfileSummary(
    customThemeProfile: CustomThemeProfile,
    draftCount: Int,
    savedSlotCount: Int
) {
    val assetCount = customThemeProfile.assets.size
    val ruleCount = customThemeProfile.rules.size
    val gifCount = customThemeProfile.assets.count { asset -> asset.mediaType == CustomThemeAsset.MEDIA_GIF }
    val contentColor = LocalContentColor.current
    val secondaryColor = contentColor.copy(alpha = 0.68f)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PersonalizationCardShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.30f),
        contentColor = contentColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "素材库与规则",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "已保存 $assetCount 个素材 / $ruleCount 条规则，GIF $gifCount 个，草稿 $draftCount 个，槽位 $savedSlotCount 个。",
                style = MaterialTheme.typography.bodySmall,
                color = secondaryColor
            )
            if (customThemeProfile.rules.isNotEmpty()) {
                Text(
                    text = customThemeProfile.rules.take(3).joinToString("；") { rule ->
                        "${customThemeRuleWeatherText(rule)} ${customThemeRuleLightText(rule)} ${customThemeRuleTimeText(rule)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CustomThemeWeatherSlotRow(
    weatherKey: String,
    imageUri: String,
    cropAnchor: String,
    ruleText: String,
    draft: Boolean,
    enabled: Boolean,
    onPickCustomThemeImage: (String) -> Unit,
    onClearCustomThemeImage: (String) -> Unit,
    onCropAnchorChanged: (String) -> Unit
) {
    val contentColor = LocalContentColor.current
    val secondaryColor = contentColor.copy(alpha = 0.68f)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PersonalizationCardShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.22f),
        contentColor = contentColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(9.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(width = 66.dp, height = 86.dp),
                shape = PersonalizationSmallShape,
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(themeBrush(VisualThemeUtils.THEME_CUSTOM_1))
                ) {
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
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = when {
                        draft -> "待保存"
                        imageUri.isNotBlank() -> "已配置 · $ruleText${customThemeMediaBadge(imageUri)}"
                        else -> "未配置，将回退默认图"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryColor
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CropAnchorButton("顶", CustomThemeCropAnchor.TOP, cropAnchor, onCropAnchorChanged)
                    CropAnchorButton("中", CustomThemeCropAnchor.CENTER, cropAnchor, onCropAnchorChanged)
                    CropAnchorButton("底", CustomThemeCropAnchor.BOTTOM, cropAnchor, onCropAnchorChanged)
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedButton(
                    enabled = enabled,
                    onClick = { onPickCustomThemeImage(weatherKey) }
                ) {
                    Text(if (imageUri.isBlank()) "选择" else "替换")
                }
                if (imageUri.isNotBlank()) {
                    TextButton(
                        enabled = enabled,
                        onClick = { onClearCustomThemeImage(weatherKey) }
                    ) {
                        Text("移除")
                    }
                }
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
    Surface(
        modifier = Modifier
            .weight(1f)
            .height(30.dp)
            .clickable { onCustomThemeCropAnchorChanged(anchor) },
        shape = PersonalizationIndicatorShape,
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else Color.Transparent,
        contentColor = if (selected) MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(alpha = 0.70f),
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.36f) else LocalContentColor.current.copy(alpha = 0.24f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PhonePreview(
    modifier: Modifier,
    selectedTheme: String,
    customThemeImageUri: String,
    customThemeCropAnchor: String,
    customThemeImageUris: Map<String, String>,
    customThemeCropAnchors: Map<String, String>,
    customThemeProfile: CustomThemeProfile,
    draftCustomThemeImageUris: Map<String, String>,
    draftCustomThemeCropAnchors: Map<String, String>,
    previewMode: ThemePreviewMode = ThemePreviewMode.HOME
) {
    val skin = ThemeSkinCatalog.getSkin(selectedTheme)
    val previewAsset = if (selectedTheme == VisualThemeUtils.THEME_CUSTOM_1) {
        customThemePreviewAsset(
            previewMode = previewMode,
            customThemeImageUri = customThemeImageUri,
            customThemeCropAnchor = customThemeCropAnchor,
            customThemeImageUris = customThemeImageUris,
            customThemeCropAnchors = customThemeCropAnchors,
            customThemeProfile = customThemeProfile,
            draftCustomThemeImageUris = draftCustomThemeImageUris,
            draftCustomThemeCropAnchors = draftCustomThemeCropAnchors
        )
    } else {
        CustomThemeAsset.empty()
    }
    Surface(
        modifier = modifier,
        shape = PersonalizationPhoneShape,
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
                customThemeImageUri = previewAsset.uri.ifBlank { customThemeImageUri },
                customThemeCropAnchor = previewAsset.cropAnchor.ifBlank { customThemeCropAnchor },
                customThemeMediaType = previewAsset.mediaType
            )
            HomePhonePreviewContent(skin.runtimeSelectable, previewMode)
        }
    }
}

@Composable
private fun HomePhonePreviewContent(runtimeSelectable: Boolean, previewMode: ThemePreviewMode) {
    val temperature = when (previewMode) {
        ThemePreviewMode.HOME -> "26"
        ThemePreviewMode.DESKTOP_CARD -> "22"
        ThemePreviewMode.CITY_MANAGEMENT -> "18"
    }
    val detail = when (previewMode) {
        ThemePreviewMode.HOME -> "30 / 18°C"
        ThemePreviewMode.DESKTOP_CARD -> "24 / 17°C"
        ThemePreviewMode.CITY_MANAGEMENT -> "20 / 15°C"
    }
    val summary = when (previewMode) {
        ThemePreviewMode.HOME -> "晴  |  空气优"
        ThemePreviewMode.DESKTOP_CARD -> "多云  |  空气优"
        ThemePreviewMode.CITY_MANAGEMENT -> "小雨  |  空气良"
    }
    val notice = when (previewMode) {
        ThemePreviewMode.HOME -> "未来8小时晴天，明日转多云"
        ThemePreviewMode.DESKTOP_CARD -> "今天多云微风，适合通勤"
        ThemePreviewMode.CITY_MANAGEMENT -> "雨势转弱，出门记得带伞"
    }
    Column(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Text(
            text = "龙岗区",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = temperature,
                fontSize = 48.sp,
                lineHeight = 50.sp,
                color = Color.White,
                fontWeight = FontWeight.Light
            )
            Text(
                text = "°C",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
        }
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.82f)
        )
        Text(
            text = if (runtimeSelectable) summary else "预留皮肤位",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.88f)
        )
        Surface(
            shape = PersonalizationCardShape,
            color = Color.White.copy(alpha = 0.16f)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                text = if (runtimeSelectable) {
                    notice
                } else {
                    "后续版本开放"
                },
                style = MaterialTheme.typography.labelSmall,
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
    titleColor: Color,
    secondaryColor: Color,
    onThemeClicked: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "更多皮肤",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = titleColor
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
                        titleColor = titleColor,
                        secondaryColor = secondaryColor,
                        onThemeClicked = onThemeClicked
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
    titleColor: Color,
    secondaryColor: Color,
    onThemeClicked: (String) -> Unit
) {
    val enabled = theme.isSelectable
    val sampleTemperature = when (theme.key) {
        VisualThemeUtils.THEME_PANORAMA -> "22°C"
        VisualThemeUtils.THEME_CUSTOM_1 -> "18°C"
        else -> "26°C"
    }
    val sampleSummary = when (theme.key) {
        VisualThemeUtils.THEME_PANORAMA -> "多云  |  空气优"
        VisualThemeUtils.THEME_CUSTOM_1 -> "小雨  |  空气良"
        else -> "晴  |  空气优"
    }
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
                            onThemeClicked(theme.key)
                        }
                    } else {
                        Modifier
                    }
                ),
            shape = PersonalizationCardShape,
            color = Color.Transparent,
            border = BorderStroke(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.22f)
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
                    modifier = Modifier.padding(12.dp),
                    text = if (enabled) sampleTemperature else "待开放",
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Light
                )
                Text(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    text = if (enabled) sampleSummary else "预留皮肤位",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.86f)
                )
            }
        }
        Text(
            text = theme.displayName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = titleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = if (selected) "应用中" else theme.shortDescription,
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) MaterialTheme.colorScheme.primary else secondaryColor,
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
    var dragOffsetY by remember { mutableStateOf(0f) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = dragOffsetY }
            .pointerInput(canMoveUp, canMoveDown) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { dragOffsetY = 0f },
                    onDragEnd = { dragOffsetY = 0f },
                    onDragCancel = { dragOffsetY = 0f },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetY += dragAmount.y
                        when {
                            dragOffsetY > 56f && canMoveDown -> {
                                onMoveDown()
                                dragOffsetY = 0f
                            }
                            dragOffsetY < -56f && canMoveUp -> {
                                onMoveUp()
                                dragOffsetY = 0f
                            }
                        }
                    }
                )
            },
        shape = PersonalizationCardShape,
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
                Icon(
                    painter = painterResource(R.drawable.ic_drag_handle_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    modifier = Modifier.size(24.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = module.displayName,
                        style = MaterialTheme.typography.bodyMedium,
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

private fun customThemePreviewAsset(
    previewMode: ThemePreviewMode,
    customThemeImageUri: String,
    customThemeCropAnchor: String,
    customThemeImageUris: Map<String, String>,
    customThemeCropAnchors: Map<String, String>,
    customThemeProfile: CustomThemeProfile,
    draftCustomThemeImageUris: Map<String, String>,
    draftCustomThemeCropAnchors: Map<String, String>
): CustomThemeAsset {
    val directKey = customThemePreviewWeatherKey(previewMode)
    val draftAsset = customThemeAssetFromSlot(
        weatherKey = directKey,
        imageUris = draftCustomThemeImageUris,
        cropAnchors = draftCustomThemeCropAnchors,
        assetPrefix = "preview-draft"
    )
    if (!draftAsset.isEmpty) {
        return draftAsset
    }
    if (!customThemeProfile.isEmpty) {
        val resolved = CustomThemeResolver.resolve(
            customThemeProfile,
            customThemeResolverWeatherKey(previewMode),
            customThemePreviewNight(previewMode),
            customThemePreviewMinute(previewMode)
        )
        if (!resolved.isEmpty) {
            return resolved
        }
    }
    val savedAsset = customThemeAssetFromSlot(
        weatherKey = directKey,
        imageUris = customThemeImageUris,
        cropAnchors = customThemeCropAnchors,
        assetPrefix = "preview-saved"
    )
    if (!savedAsset.isEmpty) {
        return savedAsset
    }
    val fallbackUri = customThemeImageUris[CustomThemeWeatherKey.FALLBACK].orEmpty()
        .ifBlank { customThemeImageUri }
    if (fallbackUri.isBlank()) {
        return CustomThemeAsset.empty()
    }
    return CustomThemeAsset(
        "preview-fallback",
        fallbackUri,
        customThemeMediaTypeForPreview(fallbackUri),
        customThemeCropAnchors[CustomThemeWeatherKey.FALLBACK] ?: customThemeCropAnchor,
        ""
    )
}

private fun customThemeAssetFromSlot(
    weatherKey: String,
    imageUris: Map<String, String>,
    cropAnchors: Map<String, String>,
    assetPrefix: String
): CustomThemeAsset {
    val imageUri = imageUris[weatherKey].orEmpty()
    if (imageUri.isBlank()) {
        return CustomThemeAsset.empty()
    }
    return CustomThemeAsset(
        "$assetPrefix-$weatherKey",
        imageUri,
        customThemeMediaTypeForPreview(imageUri),
        cropAnchors[weatherKey] ?: CustomThemeCropAnchor.CENTER,
        ""
    )
}

private fun customThemePreviewWeatherKey(previewMode: ThemePreviewMode): String {
    return when (previewMode) {
        ThemePreviewMode.HOME -> CustomThemeWeatherKey.SUNNY
        ThemePreviewMode.DESKTOP_CARD -> CustomThemeWeatherKey.CLOUDY
        ThemePreviewMode.CITY_MANAGEMENT -> CustomThemeWeatherKey.RAIN_NIGHT
    }
}

private fun customThemeResolverWeatherKey(previewMode: ThemePreviewMode): String {
    return when (previewMode) {
        ThemePreviewMode.HOME -> CustomThemeWeatherKey.SUNNY
        ThemePreviewMode.DESKTOP_CARD -> CustomThemeWeatherKey.CLOUDY
        ThemePreviewMode.CITY_MANAGEMENT -> CustomThemeWeatherKey.RAIN
    }
}

private fun customThemePreviewNight(previewMode: ThemePreviewMode): Boolean {
    return previewMode == ThemePreviewMode.CITY_MANAGEMENT
}

private fun customThemePreviewMinute(previewMode: ThemePreviewMode): Int {
    return when (previewMode) {
        ThemePreviewMode.HOME -> 11 * 60
        ThemePreviewMode.DESKTOP_CARD -> 15 * 60
        ThemePreviewMode.CITY_MANAGEMENT -> 21 * 60
    }
}

private fun customThemeMediaTypeForPreview(imageUri: String): String {
    return if (imageUri.trim().lowercase().endsWith(".gif")) {
        CustomThemeAsset.MEDIA_GIF
    } else {
        CustomThemeAsset.MEDIA_IMAGE
    }
}

private fun customThemeRuleSummary(weatherKey: String): String {
    return when (CustomThemeWeatherKey.normalize(weatherKey)) {
        CustomThemeWeatherKey.RAIN_NIGHT -> "雨天 + 夜间"
        CustomThemeWeatherKey.SNOW_NIGHT -> "雪天 + 夜间"
        CustomThemeWeatherKey.DAWN -> "05:00-08:00"
        CustomThemeWeatherKey.DUSK -> "17:00-20:00"
        CustomThemeWeatherKey.NIGHT -> "夜间通用"
        CustomThemeWeatherKey.FALLBACK -> "默认回退"
        else -> "天气匹配"
    }
}

private fun customThemeMediaBadge(imageUri: String): String {
    return if (imageUri.trim().lowercase().endsWith(".gif")) " · GIF" else " · 图片"
}

private fun customThemeRuleWeatherText(rule: CustomThemeRule): String {
    return when {
        rule.weatherKey == CustomThemeWeatherKey.RAIN && rule.lightMode == CustomThemeRule.LIGHT_NIGHT -> "雨夜"
        rule.weatherKey == CustomThemeWeatherKey.SNOW && rule.lightMode == CustomThemeRule.LIGHT_NIGHT -> "雪夜"
        else -> CustomThemeWeatherKey.displayName(rule.weatherKey)
    }
}

private fun customThemeRuleLightText(rule: CustomThemeRule): String {
    return when (rule.lightMode) {
        CustomThemeRule.LIGHT_DAY -> "白天"
        CustomThemeRule.LIGHT_NIGHT -> "夜间"
        else -> "全天"
    }
}

private fun customThemeRuleTimeText(rule: CustomThemeRule): String {
    return if (rule.hasTimeWindow()) {
        "${minuteText(rule.startMinute)}-${minuteText(rule.endMinute)}"
    } else {
        "不限时段"
    }
}

private fun minuteText(minute: Int): String {
    if (minute < 0) {
        return "--:--"
    }
    val normalizedMinute = ((minute % (24 * 60)) + (24 * 60)) % (24 * 60)
    val hour = normalizedMinute / 60
    val minuteOfHour = normalizedMinute % 60
    return "${hour.toString().padStart(2, '0')}:${minuteOfHour.toString().padStart(2, '0')}"
}

@Composable
private fun ThemePreviewBackdrop(
    themeKey: String,
    modifier: Modifier = Modifier,
    includeImage: Boolean = true,
    customThemeImageUri: String = "",
    customThemeCropAnchor: String = CustomThemeCropAnchor.CENTER,
    customThemeMediaType: String = CustomThemeAsset.MEDIA_IMAGE
) {
    Box(modifier = modifier.background(themeBrush(themeKey))) {
        when (themeKey) {
            VisualThemeUtils.THEME_PANORAMA -> {
                if (includeImage) {
                    Image(
                        painter = painterResource(R.drawable.theme_panorama_day),
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
                        modifier = Modifier.fillMaxSize(),
                        mediaType = customThemeMediaType
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
        drawRect(color = Color.White.copy(alpha = 0.08f))
        drawCircle(
            color = Color.White.copy(alpha = 0.20f),
            radius = size.width * 0.36f,
            center = Offset(size.width * 0.28f, size.height * 0.28f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.14f),
            radius = size.width * 0.30f,
            center = Offset(size.width * 0.72f, size.height * 0.42f)
        )
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color.Transparent, Color.White.copy(alpha = 0.22f))
            )
        )
    }
}
