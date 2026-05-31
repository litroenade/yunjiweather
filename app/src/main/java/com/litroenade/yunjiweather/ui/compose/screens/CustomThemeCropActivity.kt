package com.litroenade.yunjiweather.ui.compose.screens

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.canhub.cropper.CropImageView
import com.litroenade.yunjiweather.ui.compose.theme.YunJiTheme
import java.io.File

class CustomThemeCropActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sourceUri = intent.getStringExtra(EXTRA_SOURCE_URI)?.let(Uri::parse)
        if (sourceUri == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }
        setContent {
            YunJiTheme(darkTheme = true) {
                val errorText = remember { mutableStateOf("") }
                val cropViewState = remember { mutableStateOf<CropImageView?>(null) }
                val cropImageReady = remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF070B0E))
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        }) {
                            Text("\u53d6\u6d88")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "\u88c1\u526a\u9759\u6001\u5e95\u56fe",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "\u52a8\u6548\u7531\u4e3b\u9898\u5f15\u64ce\u9a71\u52a8\uff0c\u8fd9\u91cc\u53ea\u88c1\u526a\u5168\u5c4f\u80cc\u666f\u5e95\u56fe\u3002",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.68f)
                            )
                        }
                        Button(
                            enabled = cropViewState.value != null && cropImageReady.value,
                            onClick = {
                                val cropView = cropViewState.value
                                if (cropView == null) {
                                    errorText.value = "\u88c1\u526a\u89c6\u56fe\u5c1a\u672a\u5c31\u7eea"
                                    return@Button
                                }
                                val outputUri = runCatching { createOutputUri() }.getOrElse { throwable ->
                                    errorText.value = throwable.message ?: "\u65e0\u6cd5\u521b\u5efa\u88c1\u526a\u7f13\u5b58\u6587\u4ef6"
                                    return@Button
                                }
                                runCatching {
                                    cropView.croppedImageAsync(
                                        Bitmap.CompressFormat.JPEG,
                                        92,
                                        1800,
                                        2600,
                                        CropImageView.RequestSizeOptions.RESIZE_INSIDE,
                                        outputUri
                                    )
                                }.onFailure { throwable ->
                                    errorText.value = throwable.message ?: "\u56fe\u7247\u88c1\u526a\u542f\u52a8\u5931\u8d25"
                                }
                            }
                        ) {
                            Text("\u5b8c\u6210")
                        }
                    }
                    if (errorText.value.isNotBlank()) {
                        Text(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
                            text = errorText.value,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        factory = { context ->
                            CropImageView(context).apply {
                                guidelines = CropImageView.Guidelines.ON
                                setFixedAspectRatio(false)
                                setOnSetImageUriCompleteListener { _, _, error ->
                                    if (error == null) {
                                        cropImageReady.value = true
                                        errorText.value = ""
                                    } else {
                                        cropImageReady.value = false
                                        cropViewState.value = null
                                        errorText.value = error.message ?: "\u65e0\u6cd5\u6253\u5f00\u9009\u62e9\u7684\u56fe\u7247"
                                    }
                                }
                                cropImageReady.value = false
                                runCatching { setImageUriAsync(sourceUri) }.onSuccess {
                                    cropViewState.value = this
                                }.onFailure { throwable ->
                                    cropImageReady.value = false
                                    cropViewState.value = null
                                    errorText.value = throwable.message ?: "\u65e0\u6cd5\u6253\u5f00\u9009\u62e9\u7684\u56fe\u7247"
                                }
                                setOnCropImageCompleteListener { _, result ->
                                    if (result.isSuccessful && result.uriContent != null) {
                                        setResult(
                                            Activity.RESULT_OK,
                                            Intent().putExtra(EXTRA_RESULT_URI, result.uriContent.toString())
                                        )
                                        finish()
                                    } else {
                                        errorText.value = result.error?.message ?: "\u56fe\u7247\u88c1\u526a\u5931\u8d25"
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun createOutputUri(): Uri {
        val directory = File(cacheDir, "theme_crop")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return Uri.fromFile(File.createTempFile("custom_theme_crop_", ".jpg", directory))
    }

    companion object {
        private const val EXTRA_SOURCE_URI = "source_uri"
        private const val EXTRA_RESULT_URI = "result_uri"

        fun createIntent(context: Context, sourceUri: Uri): Intent {
            return Intent(context, CustomThemeCropActivity::class.java)
                .putExtra(EXTRA_SOURCE_URI, sourceUri.toString())
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .apply {
                    clipData = ClipData.newUri(
                        context.contentResolver,
                        "custom_theme_source",
                        sourceUri
                    )
                }
        }

        fun resultUri(data: Intent?): Uri? {
            return data?.getStringExtra(EXTRA_RESULT_URI)?.let(Uri::parse)
        }
    }
}
