package com.litroenade.yunjiweather.ui.location

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat

/**
 * 系统定位的最小封装。
 * 只给粗略权限时不读取精确定位源，避免系统权限降级后触发安全异常。
 */
class AndroidLocationClient(private val context: Context) {

    @SuppressLint("MissingPermission")
    fun requestCurrentLocation(
        onSuccess: (Double, Double) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!LocationPermissionResult.hasUsablePermission(context)) {
            onError("未授予定位权限，可手动搜索并添加城市。")
            return
        }
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (manager == null) {
            onError("系统定位服务不可用，可手动搜索并添加城市。")
            return
        }
        val fineLocationGranted = hasFineLocationPermission()
        val provider = bestProvider(manager, fineLocationGranted)
        if (provider == null) {
            onError("未开启系统定位服务，可打开定位后重试，或手动搜索城市。")
            return
        }
        val cached = newestLastKnownLocation(manager, provider, fineLocationGranted)
        if (cached != null && System.currentTimeMillis() - cached.time <= MAX_LAST_KNOWN_AGE_MILLIS) {
            onSuccess(cached.latitude, cached.longitude)
            return
        }
        requestFreshLocation(manager, provider, onSuccess, onError)
    }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    private fun requestFreshLocation(
        manager: LocationManager,
        provider: String,
        onSuccess: (Double, Double) -> Unit,
        onError: (String) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            manager.getCurrentLocation(
                provider,
                null,
                ContextCompat.getMainExecutor(context)
            ) { location ->
                if (location == null) {
                    onError("暂未获取到定位结果，可稍后重试或手动搜索城市。")
                } else {
                    onSuccess(location.latitude, location.longitude)
                }
            }
            return
        }

        val handler = Handler(Looper.getMainLooper())
        var completed = false
        lateinit var listener: LocationListener
        lateinit var timeoutRunnable: Runnable
        listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (completed) {
                    return
                }
                completed = true
                handler.removeCallbacks(timeoutRunnable)
                manager.removeUpdates(this)
                onSuccess(location.latitude, location.longitude)
            }

            @Deprecated("Deprecated in Android framework")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            override fun onProviderEnabled(provider: String) {
            }

            override fun onProviderDisabled(provider: String) {
            }
        }
        timeoutRunnable = Runnable {
            if (!completed) {
                completed = true
                manager.removeUpdates(listener)
                onError("定位等待超时，可手动搜索并添加城市。")
            }
        }
        manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
        handler.postDelayed(timeoutRunnable, LOCATION_TIMEOUT_MILLIS)
    }

    @SuppressLint("MissingPermission")
    private fun newestLastKnownLocation(
        manager: LocationManager,
        provider: String,
        fineLocationGranted: Boolean
    ): Location? {
        val candidates = buildList {
            add(manager.getLastKnownLocation(provider))
            add(manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))
            if (fineLocationGranted) {
                add(manager.getLastKnownLocation(LocationManager.GPS_PROVIDER))
            }
        }
        return candidates.filterNotNull().maxByOrNull { it.time }
    }

    private fun bestProvider(manager: LocationManager, fineLocationGranted: Boolean): String? {
        return when {
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            fineLocationGranted && manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            else -> null
        }
    }

    private fun hasFineLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    }

    private companion object {
        private const val MAX_LAST_KNOWN_AGE_MILLIS = 30L * 60L * 1000L
        private const val LOCATION_TIMEOUT_MILLIS = 12L * 1000L
    }
}
