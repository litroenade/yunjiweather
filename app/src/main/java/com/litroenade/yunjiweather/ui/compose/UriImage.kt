package com.litroenade.yunjiweather.ui.compose

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Movie
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.litroenade.yunjiweather.data.model.CustomThemeAsset
import com.litroenade.yunjiweather.data.model.CustomThemeCropAnchor
import com.litroenade.yunjiweather.ui.compose.theme.customThemeCropAlignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@Composable
internal fun UriImage(
    uriString: String,
    cropAnchor: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    mediaType: String = CustomThemeAsset.MEDIA_IMAGE
) {
    if (isGifMedia(uriString, mediaType)) {
        GifUriImage(uriString = uriString, cropAnchor = cropAnchor, modifier = modifier)
        return
    }
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
private fun GifUriImage(
    uriString: String,
    cropAnchor: String,
    modifier: Modifier = Modifier
) {
    val movieState = rememberUriMovie(uriString)
    val movie = movieState.value ?: return
    var frameTimeMillis by remember(movie) { mutableStateOf(0L) }
    LaunchedEffect(movie) {
        while (isActive) {
            androidx.compose.runtime.withFrameMillis { frameTimeMillis = it }
        }
    }
    Canvas(modifier = modifier) {
        val movieWidth = movie.width().takeIf { it > 0 } ?: return@Canvas
        val movieHeight = movie.height().takeIf { it > 0 } ?: return@Canvas
        val duration = movie.duration().takeIf { it > 0 } ?: 1000
        movie.setTime((frameTimeMillis % duration).toInt())
        val scale = maxOf(size.width / movieWidth, size.height / movieHeight)
        val scaledWidth = movieWidth * scale
        val scaledHeight = movieHeight * scale
        val dx = (size.width - scaledWidth) / 2f
        val dy = when (cropAnchor) {
            CustomThemeCropAnchor.TOP -> 0f
            CustomThemeCropAnchor.BOTTOM -> size.height - scaledHeight
            else -> (size.height - scaledHeight) / 2f
        }
        drawIntoCanvas { canvas ->
            val nativeCanvas = canvas.nativeCanvas
            nativeCanvas.save()
            nativeCanvas.translate(dx, dy)
            nativeCanvas.scale(scale, scale)
            movie.draw(nativeCanvas, 0f, 0f)
            nativeCanvas.restore()
        }
    }
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

@Composable
private fun rememberUriMovie(uriString: String): State<Movie?> {
    val context = LocalContext.current
    return produceState<Movie?>(initialValue = null, context, uriString) {
        value = loadUriMovie(context, uriString)
    }
}

private suspend fun loadUriMovie(context: Context, uriString: String): Movie? {
    if (uriString.isBlank()) {
        return null
    }
    return withContext(Dispatchers.IO) {
        runCatching {
            val uri = Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                Movie.decodeStream(inputStream)
            }
        }.getOrNull()
    }
}

private fun isGifMedia(uriString: String, mediaType: String): Boolean {
    return CustomThemeAsset.MEDIA_GIF == mediaType || uriString.trim().lowercase().endsWith(".gif")
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
