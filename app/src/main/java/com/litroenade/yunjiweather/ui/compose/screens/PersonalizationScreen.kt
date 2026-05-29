package com.litroenade.yunjiweather.ui.compose.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.litroenade.yunjiweather.data.local.prefs.CustomThemeImageStore
import com.litroenade.yunjiweather.data.model.CustomThemeCropAnchor
import com.litroenade.yunjiweather.data.model.CustomThemeWeatherKey
import com.litroenade.yunjiweather.ui.compose.InfoCard
import com.litroenade.yunjiweather.ui.mine.MineViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PersonalizationScreen(
    modifier: Modifier = Modifier,
    viewModel: MineViewModel = viewModel()
) {
    val context = LocalContext.current
    val selectedTheme by viewModel.getVisualTheme().observeAsState(viewModel.getCurrentVisualTheme())
    val customThemeImageUri by viewModel.getCustomThemeImageUri().observeAsState("")
    val customThemeCropAnchor by viewModel.getCustomThemeCropAnchor().observeAsState("center")
    val customThemeImageUris by viewModel.getCustomThemeImageUris().observeAsState(emptyMap())
    val customThemeCropAnchors by viewModel.getCustomThemeCropAnchors().observeAsState(emptyMap())
    val message by viewModel.getMessage().observeAsState("")
    val themes = remember(viewModel) { viewModel.getVisualThemes() }
    val scope = rememberCoroutineScope()
    val draftCustomThemeImageUris = remember { mutableStateMapOf<String, String>() }
    val draftCustomThemeCropAnchors = remember { mutableStateMapOf<String, String>() }
    var pendingCustomThemeWeatherKey by remember { mutableStateOf(CustomThemeWeatherKey.FALLBACK) }
    var customThemeEditorMessage by remember { mutableStateOf("") }
    var customThemeImporting by remember { mutableStateOf(false) }
    val cropImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val croppedUri = CustomThemeCropActivity.resultUri(result.data)
            if (croppedUri == null) {
                customThemeEditorMessage = "图片裁剪失败：没有返回裁剪结果"
                return@rememberLauncherForActivityResult
            }
            val weatherKey = pendingCustomThemeWeatherKey
            customThemeImporting = true
            customThemeEditorMessage = "正在导入${CustomThemeWeatherKey.displayName(weatherKey)}图片..."
            scope.launch {
                val importResult = withContext(Dispatchers.IO) {
                    runCatching {
                        CustomThemeImageStore.importImage(context, croppedUri)
                    }.also {
                        CustomThemeImageStore.deleteCacheImage(context, croppedUri.toString())
                    }
                }
                customThemeImporting = false
                importResult.onSuccess { importedUri ->
                    draftCustomThemeImageUris[weatherKey] = importedUri
                    draftCustomThemeCropAnchors[weatherKey] = customThemeCropAnchors[weatherKey]
                        ?: customThemeCropAnchor
                    customThemeEditorMessage = "${CustomThemeWeatherKey.displayName(weatherKey)}图片已导入，确认后点击保存。"
                }.onFailure { throwable ->
                    customThemeEditorMessage = "图片导入失败：${throwable.message ?: "无法读取图片"}"
                }
            }
        } else {
            customThemeEditorMessage = "图片裁剪已取消"
        }
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            customThemeEditorMessage = "图片选择已取消"
            return@rememberLauncherForActivityResult
        }
        cropImageLauncher.launch(CustomThemeCropActivity.createIntent(context, uri))
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (message.isNotBlank() && !message.startsWith("主题/个性化")) {
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
        item {
            PersonalizationPanel(
                themes = themes,
                selectedTheme = selectedTheme,
                customThemeImageUri = customThemeImageUri,
                customThemeCropAnchor = customThemeCropAnchor,
                customThemeImageUris = customThemeImageUris,
                customThemeCropAnchors = customThemeCropAnchors,
                draftCustomThemeImageUris = draftCustomThemeImageUris,
                draftCustomThemeCropAnchors = draftCustomThemeCropAnchors,
                customThemeEditorMessage = customThemeEditorMessage,
                customThemeImporting = customThemeImporting,
                onThemeSelected = viewModel::setVisualTheme,
                onPickCustomThemeImage = { weatherKey ->
                    pendingCustomThemeWeatherKey = weatherKey
                    imagePickerLauncher.launch(arrayOf("image/*"))
                },
                onCustomThemeCropAnchorChanged = { weatherKey, cropAnchor ->
                    val imageUri = customThemeImageUris[weatherKey].orEmpty()
                    if (imageUri.isNotBlank()) {
                        viewModel.setCustomThemeImage(weatherKey, imageUri, cropAnchor)
                    }
                },
                onDraftCustomThemeCropAnchorChanged = { weatherKey, anchor ->
                    draftCustomThemeCropAnchors[weatherKey] = anchor
                },
                onApplyCustomThemeDraft = { imageUris, cropAnchors ->
                    if (imageUris.isNotEmpty()) {
                        val savedImageUris = customThemeImageUris.toMutableMap()
                        val savedCropAnchors = customThemeCropAnchors.toMutableMap()
                        savedImageUris.putAll(imageUris)
                        savedCropAnchors.putAll(cropAnchors)
                        viewModel.setCustomThemeImages(savedImageUris, savedCropAnchors)
                        draftCustomThemeImageUris.clear()
                        draftCustomThemeCropAnchors.clear()
                        customThemeEditorMessage = ""
                        scope.launch(Dispatchers.IO) {
                            CustomThemeImageStore.pruneImportedImages(context, savedImageUris.values)
                        }
                    }
                },
                onDiscardCustomThemeDraft = {
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
                },
                onClearCustomThemeImage = {
                    viewModel.clearCustomThemeImage()
                    draftCustomThemeImageUris.clear()
                    draftCustomThemeCropAnchors.clear()
                    customThemeEditorMessage = ""
                    scope.launch(Dispatchers.IO) {
                        CustomThemeImageStore.deleteAllImportedImages(context)
                    }
                }
            )
        }
    }
}
