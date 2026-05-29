package com.litroenade.yunjiweather.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.litroenade.yunjiweather.R
import com.litroenade.yunjiweather.ui.splash.SplashActivity
import java.util.concurrent.Executors

/**
 * 小组件入口只负责广播生命周期和桌面远程视图渲染。
 * 数据刷新、缓存读取和点击路由分离，便于后续扩展多种桌面样式。
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
        WeatherWidgetRefreshScheduler.cancel(context)
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
            return RemoteViews(context.packageName, mode.layoutResId()).apply {
                setTextViewText(R.id.widget_title, snapshot.cityName)
                setTextViewText(R.id.widget_temperature, snapshot.temperatureText)
                setTextViewText(
                    R.id.widget_summary,
                    if (snapshot.isAvailable) {
                        "${snapshot.conditionText}  ${snapshot.rangeText}"
                    } else {
                        snapshot.rangeText
                    }
                )
                setTextViewText(R.id.widget_update_time, snapshot.updateText)
                setTextViewText(R.id.widget_details, snapshot.detailText())
                setTextViewText(R.id.widget_advice, snapshot.adviceText)
                applyLayoutMode(mode)
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

        private fun RemoteViews.applyLayoutMode(mode: WeatherWidgetLayoutMode) {
            when (mode) {
                WeatherWidgetLayoutMode.COMPACT -> {
                    setTextViewTextSize(R.id.widget_temperature, TypedValue.COMPLEX_UNIT_SP, 20f)
                    setViewVisibility(R.id.widget_update_time, View.GONE)
                    setViewVisibility(R.id.widget_details, View.GONE)
                    setViewVisibility(R.id.widget_advice, View.GONE)
                }
                WeatherWidgetLayoutMode.STANDARD,
                WeatherWidgetLayoutMode.AUTO -> {
                    setTextViewTextSize(R.id.widget_temperature, TypedValue.COMPLEX_UNIT_SP, 22f)
                    setViewVisibility(R.id.widget_update_time, View.VISIBLE)
                    setViewVisibility(R.id.widget_details, View.GONE)
                    setViewVisibility(R.id.widget_advice, View.GONE)
                }
                WeatherWidgetLayoutMode.EXPANDED -> {
                    setTextViewTextSize(R.id.widget_temperature, TypedValue.COMPLEX_UNIT_SP, 24f)
                    setViewVisibility(R.id.widget_update_time, View.VISIBLE)
                    setViewVisibility(R.id.widget_details, View.VISIBLE)
                    setViewVisibility(R.id.widget_advice, View.VISIBLE)
                }
            }
        }

        private fun WeatherWidgetSnapshot.detailText(): String {
            return listOf(humidityText, windText, airQualityText)
                .filter { it.isNotBlank() }
                .joinToString("  ")
        }

        private fun openAppPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            return PendingIntent.getActivity(context, 0, intent, flags)
        }
    }
}

class WeatherCompactAppWidgetProvider : WeatherAppWidgetProvider() {
    override val fixedLayoutMode: WeatherWidgetLayoutMode = WeatherWidgetLayoutMode.COMPACT
}

class WeatherDetailedAppWidgetProvider : WeatherAppWidgetProvider() {
    override val fixedLayoutMode: WeatherWidgetLayoutMode = WeatherWidgetLayoutMode.EXPANDED
}
