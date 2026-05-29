package com.litroenade.yunjiweather.ui.compose.screens

import android.app.Activity
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
                            Text("取消")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "裁剪静态底图",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "动效由主题引擎叠加，这里只裁剪全屏背景底图",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.68f)
                            )
                        }
                        Button(onClick = {
                            val cropView = cropViewState.value
                            if (cropView == null) {
                                errorText.value = "裁剪视图尚未就绪"
                                return@Button
                            }
                            val outputUri = createOutputUri()
                            cropView.croppedImageAsync(
                                Bitmap.CompressFormat.JPEG,
                                92,
                                1800,
                                2600,
                                CropImageView.RequestSizeOptions.RESIZE_INSIDE,
                                outputUri
                            )
                        }) {
                            Text("完成")
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
                                setImageUriAsync(sourceUri)
                                setOnCropImageCompleteListener { _, result ->
                                    if (result.isSuccessful && result.uriContent != null) {
                                        setResult(
                                            Activity.RESULT_OK,
                                            Intent().putExtra(EXTRA_RESULT_URI, result.uriContent.toString())
                                        )
                                        finish()
                                    } else {
                                        errorText.value = result.error?.message ?: "图片裁剪失败"
                                    }
                                }
                                cropViewState.value = this
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
        }

        fun resultUri(data: Intent?): Uri? {
            return data?.getStringExtra(EXTRA_RESULT_URI)?.let(Uri::parse)
        }
    }
}
