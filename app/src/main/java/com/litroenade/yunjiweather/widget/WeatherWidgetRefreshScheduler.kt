package com.litroenade.yunjiweather.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.litroenade.yunjiweather.worker.WeatherWidgetRefreshWorker
import com.litroenade.yunjiweather.worker.WorkerScopeUtils
import java.util.concurrent.TimeUnit

object WeatherWidgetRefreshScheduler {
    private const val REFRESH_INTERVAL_MINUTES = 30L

    @JvmStatic
    fun sync(context: Context) {
        val applicationContext = context.applicationContext
        if (hasWeatherWidgets(applicationContext)) {
            schedulePeriodic(applicationContext)
            enqueueImmediate(applicationContext)
        } else {
            cancel(applicationContext)
        }
    }

    @JvmStatic
    fun schedulePeriodic(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequest.Builder(
            WeatherWidgetRefreshWorker::class.java,
            REFRESH_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            WorkerScopeUtils.weatherWidgetRefreshWorkName(),
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    @JvmStatic
    fun enqueueImmediate(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequest.Builder(WeatherWidgetRefreshWorker::class.java)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            WorkerScopeUtils.weatherWidgetImmediateRefreshWorkName(),
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    @JvmStatic
    fun cancel(context: Context) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.cancelUniqueWork(WorkerScopeUtils.weatherWidgetRefreshWorkName())
        workManager.cancelUniqueWork(WorkerScopeUtils.weatherWidgetImmediateRefreshWorkName())
    }

    @JvmStatic
    fun hasWeatherWidgets(context: Context): Boolean {
        val appWidgetManager = AppWidgetManager.getInstance(context.applicationContext)
        return WeatherAppWidgetProvider.providerClasses().any { providerClass ->
            val componentName = ComponentName(context.applicationContext, providerClass)
            appWidgetManager.getAppWidgetIds(componentName).isNotEmpty()
        }
    }
}
