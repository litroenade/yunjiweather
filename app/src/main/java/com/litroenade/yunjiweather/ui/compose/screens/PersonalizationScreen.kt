package com.litroenade.yunjiweather.ui.compose.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.litroenade.yunjiweather.data.local.prefs.CustomThemeImageStore
import com.litroenade.yunjiweather.data.model.CustomThemeAsset
import com.litroenade.yunjiweather.data.model.CustomThemeProfile
import com.litroenade.yunjiweather.data.model.CustomThemeRule
import com.litroenade.yunjiweather.data.model.CustomThemeWeatherKey
import com.litroenade.yunjiweather.ui.compose.InfoCard
import com.litroenade.yunjiweather.ui.compose.theme.LocalYunJiVisualTheme
import com.litroenade.yunjiweather.ui.compose.theme.YunJiUiTokens
import com.litroenade.yunjiweather.ui.mine.MineViewModel
import com.litroenade.yunjiweather.utils.VisualThemeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PersonalizationScreen(
    modifier: Modifier = Modifier,
    viewModel: MineViewModel = viewModel(),
    onOpenHomeBlockEditor: () -> Unit = {}
) {
    val context = LocalContext.current
    val selectedTheme by viewModel.visualTheme.observeAsState(viewModel.currentVisualTheme)
    val customThemeImageUri by viewModel.customThemeImageUri.observeAsState("")
    val customThemeCropAnchor by viewModel.customThemeCropAnchor.observeAsState("center")
    val customThemeImageUris by viewModel.customThemeImageUris.observeAsState(emptyMap())
    val customThemeCropAnchors by viewModel.customThemeCropAnchors.observeAsState(emptyMap())
    val customThemeProfile by viewModel.customThemeProfile.observeAsState(CustomThemeProfile.empty())
    val message by viewModel.message.observeAsState("")
    val themes = remember(viewModel) { viewModel.visualThemes }
    val visualTheme = LocalYunJiVisualTheme.current
    val scope = rememberCoroutineScope()
    val draftCustomThemeImageUris = remember { mutableStateMapOf<String, String>() }
    val draftCustomThemeCropAnchors = remember { mutableStateMapOf<String, String>() }
    var pendingCustomThemeWeatherKey by remember { mutableStateOf(CustomThemeWeatherKey.FALLBACK) }
    var customThemeEditorMessage by remember { mutableStateOf("") }
    var customThemeImporting by remember { mutableStateOf(false) }
    var showingCustomThemeEditor by rememberSaveable { mutableStateOf(false) }
    fun importDraftImage(sourceUri: android.net.Uri, weatherKey: String, deleteCacheAfterImport: Boolean) {
        customThemeImporting = true
        customThemeEditorMessage = "正在导入${CustomThemeWeatherKey.displayName(weatherKey)}..."
        scope.launch {
            val importResult = withContext(Dispatchers.IO) {
                runCatching {
                    CustomThemeImageStore.importImage(context, sourceUri)
                }.also {
                    if (deleteCacheAfterImport) {
                        CustomThemeImageStore.deleteCacheImage(context, sourceUri.toString())
                    }
                }
            }
            customThemeImporting = false
            importResult.onSuccess { importedUri ->
                val previousDraftUri = draftCustomThemeImageUris[weatherKey]
                draftCustomThemeImageUris[weatherKey] = importedUri
                draftCustomThemeCropAnchors[weatherKey] = customThemeCropAnchors[weatherKey]
                    ?: customThemeCropAnchor
                customThemeEditorMessage = "${CustomThemeWeatherKey.displayName(weatherKey)}已导入，确认后点击保存。"
                if (!previousDraftUri.isNullOrBlank() && previousDraftUri != importedUri) {
                    scope.launch(Dispatchers.IO) {
                        CustomThemeImageStore.deleteImportedImage(context, previousDraftUri)
                    }
                }
            }.onFailure { throwable ->
                customThemeEditorMessage = "底图导入失败：${throwable.message ?: "无法读取图片"}"
            }
        }
    }
    fun importDraftImages(sourceUris: List<android.net.Uri>) {
        if (sourceUris.isEmpty()) {
            customThemeEditorMessage = "底图选择已取消"
            return
        }
        val targetKeys = CustomThemeWeatherKey.orderedKeys()
            .filterNot { weatherKey ->
                draftCustomThemeImageUris[weatherKey].orEmpty().isNotBlank()
                        || customThemeImageUris[weatherKey].orEmpty().isNotBlank()
                        || (weatherKey == CustomThemeWeatherKey.FALLBACK && customThemeImageUri.isNotBlank())
            }
            .take(sourceUris.size)
        if (targetKeys.isEmpty()) {
            customThemeEditorMessage = "所有场景已有素材，请在对应槽位点替换。"
            return
        }
        customThemeImporting = true
        customThemeEditorMessage = "正在批量导入 ${targetKeys.size} 张自定义主题素材..."
        scope.launch {
            val importResult = withContext(Dispatchers.IO) {
                runCatching {
                    val imported = mutableListOf<Pair<String, String>>()
                    try {
                        targetKeys.forEachIndexed { index, weatherKey ->
                            imported += weatherKey to CustomThemeImageStore.importImage(context, sourceUris[index])
                        }
                    } catch (exception: Exception) {
                        imported.forEach { (_, importedUri) ->
                            CustomThemeImageStore.deleteImportedImage(context, importedUri)
                        }
                        throw exception
                    }
                    imported
                }
            }
            customThemeImporting = false
            importResult.onSuccess { imported ->
                imported.forEach { (weatherKey, importedUri) ->
                    draftCustomThemeImageUris[weatherKey] = importedUri
                    draftCustomThemeCropAnchors[weatherKey] = customThemeCropAnchors[weatherKey]
                        ?: customThemeCropAnchor
                }
                val skippedCount = sourceUris.size - imported.size
                val targetNames = imported.joinToString("、") { (weatherKey, _) ->
                    CustomThemeWeatherKey.displayName(weatherKey)
                }
                customThemeEditorMessage = if (skippedCount > 0) {
                    "已批量导入到：$targetNames；另有 $skippedCount 张因无空槽未分配。"
                } else {
                    "已批量导入到：$targetNames，确认后保存并应用。"
                }
            }.onFailure { throwable ->
                customThemeEditorMessage = "批量导入失败：${throwable.message ?: "无法读取图片"}"
            }
        }
    }
    val cropImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val croppedUri = CustomThemeCropActivity.resultUri(result.data)
            if (croppedUri == null) {
                customThemeEditorMessage = "底图裁剪失败：没有返回裁剪结果"
                return@rememberLauncherForActivityResult
            }
            val weatherKey = pendingCustomThemeWeatherKey
            importDraftImage(croppedUri, weatherKey, deleteCacheAfterImport = true)
        } else {
            customThemeEditorMessage = "底图裁剪已取消"
        }
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            customThemeEditorMessage = "底图选择已取消"
            return@rememberLauncherForActivityResult
        }
        if (CustomThemeImageStore.mediaTypeForUri(context, uri) == CustomThemeAsset.MEDIA_GIF) {
            importDraftImage(uri, pendingCustomThemeWeatherKey, deleteCacheAfterImport = false)
        } else {
            cropImageLauncher.launch(CustomThemeCropActivity.createIntent(context, uri))
        }
    }
    val multiImagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        importDraftImages(uris.take(CustomThemeWeatherKey.orderedKeys().size))
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    BackHandler(enabled = showingCustomThemeEditor) {
        showingCustomThemeEditor = false
    }

    LazyColumn(
        modifier = modifier
            .background(visualTheme.background),
        contentPadding = PaddingValues(
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            val commonOnPickCustomThemeImage: (String) -> Unit = { weatherKey ->
                pendingCustomThemeWeatherKey = weatherKey
                imagePickerLauncher.launch(arrayOf("image/*"))
            }
            val commonOnPickMultipleCustomThemeImages = {
                multiImagePickerLauncher.launch(arrayOf("image/*"))
            }
            val commonOnCustomThemeCropAnchorChanged: (String, String) -> Unit = { weatherKey, cropAnchor ->
                val imageUri = customThemeImageUris[weatherKey].orEmpty().ifBlank {
                    if (weatherKey == CustomThemeWeatherKey.FALLBACK) customThemeImageUri else ""
                }
                if (imageUri.isNotBlank()) {
                    val savedImageUris = customThemeImageUris.toMutableMap()
                    savedImageUris[weatherKey] = imageUri
                    val savedCropAnchors = customThemeCropAnchors.toMutableMap()
                    savedCropAnchors[weatherKey] = cropAnchor
                    viewModel.setCustomThemeProfile(
                        buildCustomThemeProfile(savedImageUris, savedCropAnchors, customThemeProfile)
                    )
                }
            }
            val commonOnDraftCustomThemeCropAnchorChanged: (String, String) -> Unit = { weatherKey, anchor ->
                draftCustomThemeCropAnchors[weatherKey] = anchor
            }
            val commonOnApplyCustomThemeDraft: (Map<String, String>, Map<String, String>) -> Unit = { imageUris, cropAnchors ->
                if (imageUris.isNotEmpty()) {
                    val savedImageUris = customThemeImageUris.toMutableMap()
                    val savedCropAnchors = customThemeCropAnchors.toMutableMap()
                    savedImageUris.putAll(imageUris)
                    savedCropAnchors.putAll(cropAnchors)
                    viewModel.setCustomThemeProfile(
                        buildCustomThemeProfile(savedImageUris, savedCropAnchors, customThemeProfile)
                    )
                    draftCustomThemeImageUris.clear()
                    draftCustomThemeCropAnchors.clear()
                    customThemeEditorMessage = ""
                    scope.launch(Dispatchers.IO) {
                        CustomThemeImageStore.pruneImportedImages(
                            context,
                            savedImageUris.values + listOf(customThemeImageUri)
                        )
                    }
                }
            }
            val commonOnDiscardCustomThemeDraft: () -> Unit = {
                val draftUris = draftCustomThemeImageUris.values.toList()
                draftCustomThemeImageUris.clear()
                draftCustomThemeCropAnchors.clear()
                customThemeEditorMessage = ""
                if (draftUris.isNotEmpty()) {
                    scope.launch(Dispatchers.IO) {
                        draftUris.forEach { draftUri ->
                            CustomThemeImageStore.deleteImportedImage(context, draftUri)
                        }
                    }
                }
            }
            val commonOnClearCustomThemeWeatherImage: (String) -> Unit = { weatherKey ->
                val removedDraftUri = draftCustomThemeImageUris.remove(weatherKey)
                draftCustomThemeCropAnchors.remove(weatherKey)
                if (!removedDraftUri.isNullOrBlank()) {
                    val remainingUris = draftCustomThemeImageUris.values + customThemeImageUris.values + listOf(customThemeImageUri)
                    if (removedDraftUri !in remainingUris) {
                        scope.launch(Dispatchers.IO) {
                            CustomThemeImageStore.deleteImportedImage(context, removedDraftUri)
                        }
                    }
                } else {
                    val savedImageUris = customThemeImageUris.toMutableMap()
                    if (customThemeImageUri.isNotBlank()) {
                        savedImageUris.putIfAbsent(CustomThemeWeatherKey.FALLBACK, customThemeImageUri)
                    }
                    val removedSavedUri = savedImageUris.remove(weatherKey).orEmpty()
                    viewModel.clearCustomThemeImage(weatherKey)
                    if (removedSavedUri.isNotBlank()) {
                        val remainingUris = savedImageUris.values + draftCustomThemeImageUris.values
                        if (removedSavedUri !in remainingUris) {
                            scope.launch(Dispatchers.IO) {
                                CustomThemeImageStore.deleteImportedImage(context, removedSavedUri)
                            }
                        }
                    }
                }
            }
            val commonOnClearCustomThemeImage: () -> Unit = {
                viewModel.clearCustomThemeImage()
                draftCustomThemeImageUris.clear()
                draftCustomThemeCropAnchors.clear()
                customThemeEditorMessage = ""
                scope.launch(Dispatchers.IO) {
                    CustomThemeImageStore.deleteAllImportedImages(context)
                }
            }
            if (showingCustomThemeEditor) {
                CustomThemeEditorPanel(
                    customThemeImageUri = customThemeImageUri,
                    customThemeCropAnchor = customThemeCropAnchor,
                    customThemeImageUris = customThemeImageUris,
                    customThemeCropAnchors = customThemeCropAnchors,
                    customThemeProfile = customThemeProfile,
                    draftCustomThemeImageUris = draftCustomThemeImageUris,
                    draftCustomThemeCropAnchors = draftCustomThemeCropAnchors,
                    customThemeEditorMessage = customThemeEditorMessage,
                    customThemeImporting = customThemeImporting,
                    onBackToThemeStore = { showingCustomThemeEditor = false },
                    onOpenHomeBlockEditor = onOpenHomeBlockEditor,
                    onPickCustomThemeImage = commonOnPickCustomThemeImage,
                    onPickMultipleCustomThemeImages = commonOnPickMultipleCustomThemeImages,
                    onCustomThemeCropAnchorChanged = commonOnCustomThemeCropAnchorChanged,
                    onDraftCustomThemeCropAnchorChanged = commonOnDraftCustomThemeCropAnchorChanged,
                    onApplyCustomThemeDraft = commonOnApplyCustomThemeDraft,
                    onDiscardCustomThemeDraft = commonOnDiscardCustomThemeDraft,
                    onClearCustomThemeWeatherImage = commonOnClearCustomThemeWeatherImage,
                    onClearCustomThemeImage = commonOnClearCustomThemeImage
                )
            } else {
                PersonalizationPanel(
                    themes = themes,
                    selectedTheme = selectedTheme,
                    customThemeImageUri = customThemeImageUri,
                    customThemeCropAnchor = customThemeCropAnchor,
                    customThemeImageUris = customThemeImageUris,
                    customThemeCropAnchors = customThemeCropAnchors,
                    customThemeProfile = customThemeProfile,
                    draftCustomThemeImageUris = draftCustomThemeImageUris,
                    draftCustomThemeCropAnchors = draftCustomThemeCropAnchors,
                    onThemeSelected = viewModel::setVisualTheme,
                    onOpenCustomThemeEditor = {
                        viewModel.setVisualTheme(VisualThemeUtils.THEME_CUSTOM_1)
                        showingCustomThemeEditor = true
                    },
                )
            }
        }
        if (message.isNotBlank() && !message.startsWith("主题/个性化")) {
            item {
                InfoCard(
                    modifier = Modifier.padding(
                        horizontal = YunJiUiTokens.ScreenHorizontalPadding,
                        vertical = 12.dp
                    )
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun buildCustomThemeProfile(
    imageUris: Map<String, String>,
    cropAnchors: Map<String, String>,
    currentProfile: CustomThemeProfile
): CustomThemeProfile {
    val slotAssetIds = CustomThemeWeatherKey.orderedKeys()
        .map { weatherKey -> customThemeAssetId(weatherKey) }
        .toSet()
    val normalizedImageUris = linkedMapOf<String, String>()
    imageUris.forEach { (weatherKey, imageUri) ->
        normalizedImageUris[CustomThemeWeatherKey.normalize(weatherKey)] = imageUri
    }
    val assets = currentProfile.assets
        .filterNot { asset -> slotAssetIds.contains(asset.id) }
        .toMutableList()
    val rules = currentProfile.rules
        .filterNot { rule -> slotAssetIds.contains(rule.assetId) }
        .toMutableList()
    CustomThemeWeatherKey.orderedKeys().forEach { weatherKey ->
        val imageUri = normalizedImageUris[weatherKey].orEmpty()
        if (imageUri.isNotBlank()) {
            val assetId = customThemeAssetId(weatherKey)
            assets += CustomThemeAsset(
                assetId,
                imageUri,
                customThemeMediaType(imageUri),
                cropAnchors[weatherKey] ?: "center",
                CustomThemeWeatherKey.displayName(weatherKey)
            )
            rules += customThemeRuleForSlot(weatherKey, assetId)
        }
    }
    return CustomThemeProfile.create(
        assets,
        rules,
        currentProfile.homeModuleOrder,
        currentProfile.disabledHomeModules
    )
}

private fun customThemeRuleForSlot(weatherKey: String, assetId: String): CustomThemeRule {
    return when (CustomThemeWeatherKey.normalize(weatherKey)) {
        CustomThemeWeatherKey.FALLBACK -> CustomThemeRule.fallback(assetId)
        CustomThemeWeatherKey.NIGHT -> CustomThemeRule(
            assetId,
            CustomThemeWeatherKey.FALLBACK,
            CustomThemeRule.LIGHT_NIGHT,
            CustomThemeRule.NO_TIME,
            CustomThemeRule.NO_TIME,
            40
        )
        CustomThemeWeatherKey.RAIN_NIGHT -> CustomThemeRule(
            assetId,
            CustomThemeWeatherKey.RAIN,
            CustomThemeRule.LIGHT_NIGHT,
            CustomThemeRule.NO_TIME,
            CustomThemeRule.NO_TIME,
            80
        )
        CustomThemeWeatherKey.SNOW_NIGHT -> CustomThemeRule(
            assetId,
            CustomThemeWeatherKey.SNOW,
            CustomThemeRule.LIGHT_NIGHT,
            CustomThemeRule.NO_TIME,
            CustomThemeRule.NO_TIME,
            80
        )
        CustomThemeWeatherKey.DAWN -> CustomThemeRule(
            assetId,
            CustomThemeWeatherKey.FALLBACK,
            CustomThemeRule.LIGHT_ANY,
            5 * 60,
            8 * 60,
            50
        )
        CustomThemeWeatherKey.DUSK -> CustomThemeRule(
            assetId,
            CustomThemeWeatherKey.FALLBACK,
            CustomThemeRule.LIGHT_ANY,
            17 * 60,
            20 * 60,
            50
        )
        else -> CustomThemeRule(
            assetId,
            weatherKey,
            CustomThemeRule.LIGHT_ANY,
            CustomThemeRule.NO_TIME,
            CustomThemeRule.NO_TIME,
            20
        )
    }
}

private fun customThemeAssetId(weatherKey: String): String {
    return "slot-${CustomThemeWeatherKey.normalize(weatherKey)}"
}

private fun customThemeMediaType(imageUri: String): String {
    return if (imageUri.trim().lowercase().endsWith(".gif")) {
        CustomThemeAsset.MEDIA_GIF
    } else {
        CustomThemeAsset.MEDIA_IMAGE
    }
}
