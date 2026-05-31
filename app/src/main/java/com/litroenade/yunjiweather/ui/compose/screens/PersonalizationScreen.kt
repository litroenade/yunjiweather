package com.litroenade.yunjiweather.ui.compose.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.litroenade.yunjiweather.data.model.HomeWeatherData
import com.litroenade.yunjiweather.ui.compose.theme.LocalYunJiVisualTheme
import com.litroenade.yunjiweather.ui.mine.MineViewModel
import com.litroenade.yunjiweather.utils.VisualThemeUtils
import com.litroenade.yunjiweather.utils.DateTimeUtils
import com.litroenade.yunjiweather.utils.DefaultCityUtils
import com.litroenade.yunjiweather.widget.WeatherWidgetSnapshotFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PersonalizationScreen(
    modifier: Modifier = Modifier,
    viewModel: MineViewModel = viewModel(),
    onOpenHomeBlockEditor: () -> Unit = {},
    backRequestVersion: Int = 0,
    homeWeatherData: HomeWeatherData? = null,
    homeWeatherUpdateTime: Long = 0L,
    temperatureUnit: String = "\u0043",
    onExitRequested: () -> Unit = {}
) {
    val context = LocalContext.current
    val selectedTheme by viewModel.visualTheme.observeAsState(viewModel.currentVisualTheme)
    val customThemeImageUri by viewModel.customThemeImageUri.observeAsState("")
    val customThemeCropAnchor by viewModel.customThemeCropAnchor.observeAsState("center")
    val customThemeImageUris by viewModel.customThemeImageUris.observeAsState(emptyMap())
    val customThemeCropAnchors by viewModel.customThemeCropAnchors.observeAsState(emptyMap())
    val customThemeProfile by viewModel.customThemeProfile.observeAsState(CustomThemeProfile.empty())
    val themes = remember(viewModel) { viewModel.visualThemes }
    val visualTheme = LocalYunJiVisualTheme.current
    val widgetSnapshot = remember(homeWeatherData, homeWeatherUpdateTime, temperatureUnit) {
        homeWeatherData?.let { data ->
            WeatherWidgetSnapshotFactory.fromHomeWeather(
                data,
                DateTimeUtils.formatMinuteTime(if (homeWeatherUpdateTime > 0L) homeWeatherUpdateTime else data.updateTime),
                CustomThemeAsset.empty(),
                temperatureUnit
            )
        } ?: WeatherWidgetSnapshotFactory.unavailable(DefaultCityUtils.DEFAULT_CITY_NAME)
    }
    val scope = rememberCoroutineScope()
    val draftCustomThemeImageUris = remember { mutableStateMapOf<String, String>() }
    val draftCustomThemeCropAnchors = remember { mutableStateMapOf<String, String>() }
    var pendingCustomThemeWeatherKey by remember { mutableStateOf(CustomThemeWeatherKey.FALLBACK) }
    var customThemeEditorMessage by remember { mutableStateOf("") }
    var customThemeImporting by remember { mutableStateOf(false) }
    var showingCustomThemeEditor by rememberSaveable { mutableStateOf(false) }
    var panelBackRequestVersion by remember { mutableStateOf(0) }

    fun importDraftImage(sourceUri: Uri, weatherKey: String, deleteCacheAfterImport: Boolean) {
        customThemeImporting = true
        customThemeEditorMessage = "\u6b63\u5728\u5bfc\u5165${CustomThemeWeatherKey.displayName(weatherKey)}..."
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
                customThemeEditorMessage =
                    "${CustomThemeWeatherKey.displayName(weatherKey)}\u5df2\u5bfc\u5165\uff0c\u786e\u8ba4\u540e\u70b9\u51fb\u4fdd\u5b58\u3002"
                if (!previousDraftUri.isNullOrBlank() && previousDraftUri != importedUri) {
                    scope.launch(Dispatchers.IO) {
                        CustomThemeImageStore.deleteImportedImage(context, previousDraftUri)
                    }
                }
            }.onFailure { throwable ->
                customThemeEditorMessage =
                    "\u5e95\u56fe\u5bfc\u5165\u5931\u8d25\uff1a${throwable.message ?: "\u65e0\u6cd5\u8bfb\u53d6\u56fe\u7247"}"
            }
        }
    }

    fun importDraftImages(sourceUris: List<Uri>) {
        if (sourceUris.isEmpty()) {
            customThemeEditorMessage = "\u5e95\u56fe\u9009\u62e9\u5df2\u53d6\u6d88"
            return
        }
        val targetKeys = CustomThemeWeatherKey.orderedKeys()
            .filterNot { weatherKey ->
                draftCustomThemeImageUris[weatherKey].orEmpty().isNotBlank() ||
                    customThemeImageUris[weatherKey].orEmpty().isNotBlank() ||
                    (weatherKey == CustomThemeWeatherKey.FALLBACK && customThemeImageUri.isNotBlank())
            }
            .take(sourceUris.size)
        if (targetKeys.isEmpty()) {
            customThemeEditorMessage =
                "\u6240\u6709\u573a\u666f\u5df2\u6709\u7d20\u6750\uff0c\u8bf7\u5728\u5bf9\u5e94\u69fd\u4f4d\u70b9\u51fb\u66ff\u6362\u3002"
            return
        }
        customThemeImporting = true
        customThemeEditorMessage =
            "\u6b63\u5728\u6279\u91cf\u5bfc\u5165 ${targetKeys.size} \u5f20\u81ea\u5b9a\u4e49\u4e3b\u9898\u7d20\u6750..."
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
                val targetNames = imported.joinToString("\u3001") { (weatherKey, _) ->
                    CustomThemeWeatherKey.displayName(weatherKey)
                }
                customThemeEditorMessage = if (skippedCount > 0) {
                    "\u5df2\u6279\u91cf\u5bfc\u5165\u5230\uff1a$targetNames\uff1b\u53e6\u6709 $skippedCount \u5f20\u56e0\u65e0\u7a7a\u69fd\u672a\u5206\u914d\u3002"
                } else {
                    "\u5df2\u6279\u91cf\u5bfc\u5165\u5230\uff1a$targetNames\uff0c\u786e\u8ba4\u540e\u4fdd\u5b58\u5e76\u5e94\u7528\u3002"
                }
            }.onFailure { throwable ->
                customThemeEditorMessage =
                    "\u6279\u91cf\u5bfc\u5165\u5931\u8d25\uff1a${throwable.message ?: "\u65e0\u6cd5\u8bfb\u53d6\u56fe\u7247"}"
            }
        }
    }

    val cropImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val croppedUri = CustomThemeCropActivity.resultUri(result.data)
            if (croppedUri == null) {
                customThemeEditorMessage = "\u5e95\u56fe\u88c1\u526a\u5931\u8d25\uff1a\u6ca1\u6709\u8fd4\u56de\u88c1\u526a\u7ed3\u679c"
                return@rememberLauncherForActivityResult
            }
            importDraftImage(croppedUri, pendingCustomThemeWeatherKey, deleteCacheAfterImport = true)
        } else {
            customThemeEditorMessage = "\u5e95\u56fe\u88c1\u526a\u5df2\u53d6\u6d88"
        }
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            customThemeEditorMessage = "\u5e95\u56fe\u9009\u62e9\u5df2\u53d6\u6d88"
            return@rememberLauncherForActivityResult
        }
        persistReadPermission(context, uri)
        val mediaType = runCatching {
            CustomThemeImageStore.mediaTypeForUri(context, uri)
        }.getOrDefault(CustomThemeAsset.MEDIA_IMAGE)
        if (mediaType == CustomThemeAsset.MEDIA_GIF) {
            importDraftImage(uri, pendingCustomThemeWeatherKey, deleteCacheAfterImport = false)
        } else {
            runCatching {
                cropImageLauncher.launch(CustomThemeCropActivity.createIntent(context, uri))
            }.onFailure {
                customThemeEditorMessage = "\u88c1\u526a\u5668\u6253\u5f00\u5931\u8d25\uff0c\u5df2\u6539\u4e3a\u76f4\u63a5\u5bfc\u5165\u539f\u56fe\u3002"
                importDraftImage(uri, pendingCustomThemeWeatherKey, deleteCacheAfterImport = false)
            }
        }
    }
    val multiImagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri -> persistReadPermission(context, uri) }
        importDraftImages(uris.take(CustomThemeWeatherKey.orderedKeys().size))
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }
    BackHandler(enabled = showingCustomThemeEditor) {
        showingCustomThemeEditor = false
    }
    LaunchedEffect(backRequestVersion) {
        if (backRequestVersion == 0) {
            return@LaunchedEffect
        }
        if (showingCustomThemeEditor) {
            showingCustomThemeEditor = false
        } else {
            panelBackRequestVersion = backRequestVersion
        }
    }

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
            modifier = modifier.background(visualTheme.background),
            customThemeImageUri = customThemeImageUri,
            customThemeCropAnchor = customThemeCropAnchor,
            customThemeImageUris = customThemeImageUris,
            customThemeCropAnchors = customThemeCropAnchors,
            customThemeProfile = customThemeProfile,
            widgetSnapshot = widgetSnapshot,
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
        LazyColumn(
            modifier = modifier.background(visualTheme.background),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                PersonalizationPanel(
                    themes = themes,
                    selectedTheme = selectedTheme,
                    customThemeImageUri = customThemeImageUri,
                    customThemeCropAnchor = customThemeCropAnchor,
                    customThemeImageUris = customThemeImageUris,
                    customThemeCropAnchors = customThemeCropAnchors,
                    customThemeProfile = customThemeProfile,
                    widgetSnapshot = widgetSnapshot,
                    draftCustomThemeImageUris = draftCustomThemeImageUris,
                    draftCustomThemeCropAnchors = draftCustomThemeCropAnchors,
                    onThemeSelected = viewModel::setVisualTheme,
                    backRequestVersion = panelBackRequestVersion,
                    onBackRequestConsumed = { consumed ->
                        if (!consumed) {
                            onExitRequested()
                        }
                    },
                    onOpenCustomThemeEditor = {
                        viewModel.setVisualTheme(VisualThemeUtils.THEME_CUSTOM_1)
                        showingCustomThemeEditor = true
                    }
                )
            }
        }
    }
}

private fun persistReadPermission(context: Context, uri: Uri) {
    runCatching {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
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
