package com.litroenade.yunjiweather.ui.compose.theme

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Immutable
data class CustomThemeImageAnalysis(
    val ready: Boolean = false,
    val primaryText: Color = Color.White,
    val secondaryText: Color = Color.White.copy(alpha = 0.78f),
    val darkScrimScale: Float = 1f
)

@Composable
fun rememberCustomThemeImageAnalysis(imageUri: String): State<CustomThemeImageAnalysis> {
    val context = LocalContext.current
    return produceState(initialValue = CustomThemeImageAnalysis(), context, imageUri) {
        value = analyzeCustomThemeImage(context, imageUri) ?: CustomThemeImageAnalysis()
    }
}

private suspend fun analyzeCustomThemeImage(
    context: Context,
    imageUri: String
): CustomThemeImageAnalysis? {
    if (imageUri.isBlank()) {
        return null
    }
    return withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.applicationContext.contentResolver
            val uri = Uri.parse(imageUri)
            val bounds = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            resolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, bounds)
            }
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = calculateAnalysisSampleSize(bounds, 320, 480)
                inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
            }
            val bitmap = resolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return@runCatching null
            val palette = Palette.from(bitmap).maximumColorCount(12).generate()
            val swatch = palette.dominantSwatch
                ?: palette.vibrantSwatch
                ?: palette.mutedSwatch
                ?: palette.lightVibrantSwatch
                ?: palette.darkMutedSwatch
            val luminance = swatch?.rgb?.let(::relativeLuminance) ?: 0.32
            val titleColor = swatch?.titleTextColor?.let { Color(it) }
                ?: if (luminance > 0.58) Color(0xFF11191D) else Color.White
            val bodyColor = swatch?.bodyTextColor?.let { Color(it) }
                ?: titleColor.copy(alpha = 0.78f)
            CustomThemeImageAnalysis(
                ready = true,
                primaryText = titleColor,
                secondaryText = bodyColor.copy(alpha = 0.78f),
                darkScrimScale = if (luminance > 0.58) 0.38f else 1f
            )
        }.getOrNull()
    }
}

private fun calculateAnalysisSampleSize(
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

private fun relativeLuminance(colorInt: Int): Double {
    val red = linearized(android.graphics.Color.red(colorInt) / 255.0)
    val green = linearized(android.graphics.Color.green(colorInt) / 255.0)
    val blue = linearized(android.graphics.Color.blue(colorInt) / 255.0)
    return 0.2126 * red + 0.7152 * green + 0.0722 * blue
}

private fun linearized(channel: Double): Double {
    return if (channel <= 0.03928) {
        channel / 12.92
    } else {
        Math.pow((channel + 0.055) / 1.055, 2.4)
    }
}
