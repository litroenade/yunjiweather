package com.litroenade.yunjiweather.ui.compose

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.litroenade.yunjiweather.ui.compose.theme.customThemeCropAlignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun UriImage(
    uriString: String,
    cropAnchor: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val bitmapState = rememberUriImageBitmap(uriString)
    val bitmap = bitmapState.value ?: return
    Image(
        bitmap = bitmap,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        alignment = customThemeCropAlignment(cropAnchor)
    )
}

@Composable
internal fun rememberUriImageBitmap(uriString: String): State<ImageBitmap?> {
    val context = LocalContext.current
    return produceState<ImageBitmap?>(initialValue = null, context, uriString) {
        value = loadUriImageBitmap(context, uriString)
    }
}

private suspend fun loadUriImageBitmap(context: Context, uriString: String): ImageBitmap? {
    if (uriString.isBlank()) {
        return null
    }
    return withContext(Dispatchers.IO) {
        runCatching {
            val uri = Uri.parse(uriString)
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            resolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, bounds)
            }
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(bounds, 1600, 2400)
                inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
            }
            resolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, decodeOptions)?.asImageBitmap()
            }
        }.getOrNull()
    }
}

private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        var halfHeight = height / 2
        var halfWidth = width / 2
        while ((halfHeight / inSampleSize) >= reqHeight &&
            (halfWidth / inSampleSize) >= reqWidth
        ) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}
