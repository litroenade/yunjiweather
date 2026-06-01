package com.litroenade.yunjiweather.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.litroenade.yunjiweather.R
import com.litroenade.yunjiweather.data.model.CustomThemeCropAnchor
import com.litroenade.yunjiweather.ui.splash.SplashActivity
import com.litroenade.yunjiweather.utils.VisualThemeUtils
import com.litroenade.yunjiweather.utils.WeatherIconUtils
import java.io.File
import java.util.concurrent.Executors

/**
 * The widget provider only handles lifecycle broadcasts and RemoteViews rendering.
 */
open class WeatherAppWidgetProvider : AppWidgetProvider() {

    protected open val fixedLayoutMode: WeatherWidgetLayoutMode = WeatherWidgetLayoutMode.STANDARD

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        WeatherWidgetRefreshScheduler.sync(context)
        val pendingResult = goAsync()
        val finisher = WeatherWidgetBroadcastFinisher.create(appWidgetIds.size) {
            pendingResult.finish()
        }
        appWidgetIds.forEach { appWidgetId ->
            refreshWidget(context, appWidgetManager, appWidgetId, fixedLayoutMode, finisher::markFinished)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        refreshWidget(context, appWidgetManager, appWidgetId, fixedLayoutMode)
    }

    override fun onEnabled(context: Context) {
        WeatherWidgetRefreshScheduler.sync(context)
    }

    override fun onDisabled(context: Context) {
        WeatherWidgetRefreshScheduler.sync(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            WeatherWidgetRefreshScheduler.sync(context)
        }
    }

    companion object {
        private val executor = Executors.newSingleThreadExecutor()

        @JvmStatic
        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context.applicationContext)
            providerClasses().forEach { providerClass ->
                val componentName = ComponentName(context.applicationContext, providerClass)
                val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
                widgetIds.forEach { appWidgetId ->
                    refreshWidget(
                        context.applicationContext,
                        appWidgetManager,
                        appWidgetId,
                        modeForProvider(providerClass)
                    )
                }
            }
        }

        private fun refreshWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            fixedLayoutMode: WeatherWidgetLayoutMode,
            onFinished: () -> Unit = {}
        ) {
            executor.execute {
                try {
                    val snapshot = WeatherWidgetSnapshotLoader.fromContext(context).load()
                    val mode = fixedLayoutMode.takeUnless { it == WeatherWidgetLayoutMode.AUTO }
                        ?: appWidgetManager.getLayoutMode(appWidgetId)
                    val views = createRemoteViews(context, snapshot, mode)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (exception: Exception) {
                    Log.e(TAG, "Failed to update weather widget $appWidgetId", exception)
                } finally {
                    onFinished()
                }
            }
        }

        @JvmStatic
        fun createRemoteViews(context: Context, snapshot: WeatherWidgetSnapshot): RemoteViews {
            return createRemoteViews(context, snapshot, WeatherWidgetLayoutMode.STANDARD)
        }

        @JvmStatic
        fun createRemoteViews(
            context: Context,
            snapshot: WeatherWidgetSnapshot,
            mode: WeatherWidgetLayoutMode
        ): RemoteViews {
            val spec = WidgetStyleSpec.forMode(mode)
            return RemoteViews(context.packageName, mode.layoutResId()).apply {
                applyWidgetBackground(context.applicationContext, snapshot, spec)
                setTextViewText(R.id.widget_title, snapshot.cityName)
                setTextViewText(R.id.widget_temperature, snapshot.temperatureText)
                setTextViewText(R.id.widget_summary, snapshot.conditionText)
                setTextViewText(R.id.widget_details, snapshot.rangeText)
                setTextViewText(R.id.widget_update_time, snapshot.updateText)
                setTextViewText(R.id.widget_advice, snapshot.adviceText)
                setImageViewResource(R.id.widget_icon, snapshot.iconResId())
                if (mode == WeatherWidgetLayoutMode.EXPANDED) {
                    setTextViewText(R.id.widget_life_clothing_value, snapshot.clothingValue)
                    setTextViewText(R.id.widget_life_fishing_value, snapshot.fishingValue)
                    setTextViewText(R.id.widget_life_sunset_value, snapshot.sunsetValue)
                    setTextViewText(R.id.widget_life_cold_value, snapshot.coldValue)
                }
                applyLayoutMode(spec)
                if (!snapshot.isAvailable) {
                    setTextViewTextSize(R.id.widget_temperature, TypedValue.COMPLEX_UNIT_SP, 14f)
                }
                setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent(context))
            }
        }

        @JvmStatic
        fun providerClasses(): Array<Class<out AppWidgetProvider>> {
            return arrayOf(
                WeatherAppWidgetProvider::class.java,
                WeatherCompactAppWidgetProvider::class.java,
                WeatherDetailedAppWidgetProvider::class.java
            )
        }

        @JvmStatic
        fun providerClassFor(mode: WeatherWidgetLayoutMode): Class<out AppWidgetProvider> {
            return when (mode) {
                WeatherWidgetLayoutMode.COMPACT -> WeatherCompactAppWidgetProvider::class.java
                WeatherWidgetLayoutMode.EXPANDED -> WeatherDetailedAppWidgetProvider::class.java
                else -> WeatherAppWidgetProvider::class.java
            }
        }

        private fun modeForProvider(providerClass: Class<out AppWidgetProvider>): WeatherWidgetLayoutMode {
            return when (providerClass) {
                WeatherCompactAppWidgetProvider::class.java -> WeatherWidgetLayoutMode.COMPACT
                WeatherDetailedAppWidgetProvider::class.java -> WeatherWidgetLayoutMode.EXPANDED
                else -> WeatherWidgetLayoutMode.STANDARD
            }
        }

        private fun WeatherWidgetLayoutMode.layoutResId(): Int {
            return when (this) {
                WeatherWidgetLayoutMode.COMPACT -> R.layout.widget_weather_compact
                WeatherWidgetLayoutMode.EXPANDED -> R.layout.widget_weather_expanded
                else -> R.layout.widget_weather
            }
        }

        private fun AppWidgetManager.getLayoutMode(appWidgetId: Int): WeatherWidgetLayoutMode {
            val options = getAppWidgetOptions(appWidgetId)
            return WeatherWidgetLayoutMode.fromSize(
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0),
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
            )
        }

        private fun RemoteViews.applyLayoutMode(spec: WidgetStyleSpec) {
            setTextViewTextSize(
                R.id.widget_temperature,
                TypedValue.COMPLEX_UNIT_SP,
                spec.temperatureTextSizeSp.toFloat()
            )
            setViewVisibility(R.id.widget_update_time, if (spec.isUpdateTimeVisible) View.VISIBLE else View.GONE)
            setViewVisibility(R.id.widget_details, if (spec.isDetailsVisible) View.VISIBLE else View.GONE)
            setViewVisibility(R.id.widget_advice, if (spec.isAdviceVisible) View.VISIBLE else View.GONE)
        }

        private fun RemoteViews.applyWidgetBackground(
            context: Context,
            snapshot: WeatherWidgetSnapshot,
            spec: WidgetStyleSpec
        ) {
            val customBitmap = bitmapFromUri(
                context = context,
                snapshot.customBackgroundUri,
                snapshot.customBackgroundCropAnchor,
                spec
            )
            if (customBitmap == null) {
                setImageViewResource(R.id.widget_background_image, snapshot.widgetBackgroundResId())
            } else {
                setImageViewBitmap(R.id.widget_background_image, customBitmap)
            }
        }

        private fun WeatherWidgetSnapshot.widgetBackgroundResId(): Int {
            if (visualThemeKey != VisualThemeUtils.THEME_PANORAMA) {
                return R.drawable.widget_weather_background
            }
            return when (WeatherIconUtils.getWeatherCategory(iconCode)) {
                WeatherIconUtils.WeatherCategory.RAIN -> R.drawable.theme_panorama_rain
                WeatherIconUtils.WeatherCategory.SNOW -> R.drawable.theme_panorama_snow
                WeatherIconUtils.WeatherCategory.NIGHT -> R.drawable.theme_panorama_night
                else -> R.drawable.theme_panorama_day
            }
        }

        private fun bitmapFromUri(
            context: Context,
            imageUri: String,
            cropAnchor: String,
            spec: WidgetStyleSpec
        ): Bitmap? {
            if (imageUri.isBlank()) {
                return null
            }
            val uri = runCatching { Uri.parse(imageUri) }.getOrNull() ?: return null
            return runCatching {
                val (targetWidth, targetHeight) = widgetBitmapSizePx(context, spec)
                val bitmap = decodeSampledBitmap(context, uri, targetWidth, targetHeight)
                bitmap?.let { source ->
                    val cropped = cropBitmapToWidgetAspect(source, cropAnchor, spec)
                    if (cropped.width == targetWidth && cropped.height == targetHeight) {
                        cropped
                    } else {
                        Bitmap.createScaledBitmap(cropped, targetWidth, targetHeight, true)
                    }
                }
            }.getOrNull()
        }

        private fun decodeSampledBitmap(
            context: Context,
            uri: Uri,
            targetWidth: Int,
            targetHeight: Int
        ): Bitmap? {
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            openBitmapInputStream(context, uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOptions)
            } ?: return null
            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                return null
            }
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = calculateWidgetInSampleSize(boundsOptions, targetWidth, targetHeight)
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            return openBitmapInputStream(context, uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            }
        }

        private fun openBitmapInputStream(context: Context, uri: Uri) = when (uri.scheme) {
            "file" -> {
                val path = uri.path ?: return null
                val file = File(path)
                if (!file.isFile) null else file.inputStream()
            }
            "content" -> context.contentResolver.openInputStream(uri)
            else -> null
        }

        private fun calculateWidgetInSampleSize(
            options: BitmapFactory.Options,
            targetWidth: Int,
            targetHeight: Int
        ): Int {
            var inSampleSize = 1
            var halfHeight = options.outHeight / 2
            var halfWidth = options.outWidth / 2
            while (halfHeight / inSampleSize >= targetHeight && halfWidth / inSampleSize >= targetWidth) {
                inSampleSize *= 2
            }
            return inSampleSize.coerceAtLeast(1)
        }

        private fun widgetBitmapSizePx(context: Context, spec: WidgetStyleSpec): Pair<Int, Int> {
            val density = context.resources.displayMetrics.density
            val rawWidth = (spec.previewWidthDp * density).toInt().coerceAtLeast(1)
            val rawHeight = (spec.previewHeightDp * density).toInt().coerceAtLeast(1)
            val scale = minOf(1f, 512f / maxOf(rawWidth, rawHeight).toFloat())
            return (rawWidth * scale).toInt().coerceAtLeast(120) to
                    (rawHeight * scale).toInt().coerceAtLeast(120)
        }

        private fun cropBitmapToWidgetAspect(
            bitmap: Bitmap,
            cropAnchor: String,
            spec: WidgetStyleSpec
        ): Bitmap {
            if (bitmap.width <= 0 || bitmap.height <= 0 || spec.previewWidthDp <= 0 || spec.previewHeightDp <= 0) {
                return bitmap
            }
            val targetRatio = spec.previewWidthDp.toFloat() / spec.previewHeightDp.toFloat()
            val bitmapRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val cropWidth: Int
            val cropHeight: Int
            if (bitmapRatio > targetRatio) {
                cropHeight = bitmap.height
                cropWidth = (bitmap.height * targetRatio).toInt().coerceIn(1, bitmap.width)
            } else {
                cropWidth = bitmap.width
                cropHeight = (bitmap.width / targetRatio).toInt().coerceIn(1, bitmap.height)
            }
            if (cropWidth == bitmap.width && cropHeight == bitmap.height) {
                return bitmap
            }
            val cropLeft = ((bitmap.width - cropWidth) / 2).coerceAtLeast(0)
            val cropTop = when (cropAnchor) {
                CustomThemeCropAnchor.TOP -> 0
                CustomThemeCropAnchor.BOTTOM -> bitmap.height - cropHeight
                else -> (bitmap.height - cropHeight) / 2
            }.coerceIn(0, bitmap.height - cropHeight)
            return Bitmap.createBitmap(bitmap, cropLeft, cropTop, cropWidth, cropHeight)
        }

        private fun WeatherWidgetSnapshot.iconResId(): Int {
            return when {
                iconCode.startsWith("4") || conditionText.contains("\u96ea") -> R.drawable.ic_weather_snow
                iconCode.startsWith("3") ||
                        conditionText.contains("\u96e8") ||
                        conditionText.contains("\u96f7") -> R.drawable.ic_weather_rain
                iconCode.startsWith("15") ||
                        conditionText.contains("\u4e91") ||
                        conditionText.contains("\u9634") -> R.drawable.ic_weather_cloudy
                else -> R.drawable.ic_weather_sunny
            }
        }

        private fun openAppPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            return PendingIntent.getActivity(context, 0, intent, flags)
        }

        private const val TAG = "WeatherWidgetProvider"
    }
}

class WeatherCompactAppWidgetProvider : WeatherAppWidgetProvider() {
    override val fixedLayoutMode: WeatherWidgetLayoutMode = WeatherWidgetLayoutMode.COMPACT
}

class WeatherDetailedAppWidgetProvider : WeatherAppWidgetProvider() {
    override val fixedLayoutMode: WeatherWidgetLayoutMode = WeatherWidgetLayoutMode.EXPANDED
}
