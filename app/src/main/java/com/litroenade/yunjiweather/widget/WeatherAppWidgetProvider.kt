package com.litroenade.yunjiweather.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.litroenade.yunjiweather.R
import com.litroenade.yunjiweather.ui.splash.SplashActivity
import java.util.concurrent.Executors

/**
 * AppWidgetProvider stays thin: it loads a snapshot off the main thread, renders
 * RemoteViews, and routes widget taps back into the normal SplashActivity entry.
 */
class WeatherAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        val finisher = WeatherWidgetBroadcastFinisher.create(appWidgetIds.size) {
            pendingResult.finish()
        }
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId, finisher::markFinished)
        }
    }

    companion object {
        private val executor = Executors.newSingleThreadExecutor()

        @JvmStatic
        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context.applicationContext)
            val componentName = ComponentName(context.applicationContext, WeatherAppWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
            widgetIds.forEach { appWidgetId ->
                updateWidget(context.applicationContext, appWidgetManager, appWidgetId)
            }
        }

        @JvmStatic
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            updateWidget(context, appWidgetManager, appWidgetId) {
            }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            onFinished: () -> Unit
        ) {
            executor.execute {
                try {
                    val snapshot = WeatherWidgetSnapshotLoader.fromContext(context).load()
                    val views = createRemoteViews(context, snapshot)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } finally {
                    onFinished()
                }
            }
        }

        @JvmStatic
        fun createRemoteViews(context: Context, snapshot: WeatherWidgetSnapshot): RemoteViews {
            return RemoteViews(context.packageName, R.layout.widget_weather).apply {
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
                setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent(context))
            }
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
