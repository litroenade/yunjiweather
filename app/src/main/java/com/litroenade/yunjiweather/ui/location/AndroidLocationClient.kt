package com.litroenade.yunjiweather.ui.location

import android.Manifest
import android.annotation.SuppressLint
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
 * Minimal system-location wrapper used by city selection flows.
 */
class AndroidLocationClient(private val context: Context) {

    @SuppressLint("MissingPermission")
    fun requestCurrentLocation(
        onSuccess: (Double, Double) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!LocationPermissionResult.hasUsablePermission(context)) {
            onError("\u672a\u6388\u4e88\u5b9a\u4f4d\u6743\u9650\uff0c\u53ef\u624b\u52a8\u641c\u7d22\u5e76\u6dfb\u52a0\u57ce\u5e02\u3002")
            return
        }
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (manager == null) {
            onError("\u7cfb\u7edf\u5b9a\u4f4d\u670d\u52a1\u4e0d\u53ef\u7528\uff0c\u53ef\u624b\u52a8\u641c\u7d22\u5e76\u6dfb\u52a0\u57ce\u5e02\u3002")
            return
        }
        val fineLocationGranted = hasFineLocationPermission()
        val provider = bestProvider(manager, fineLocationGranted)
        if (provider == null) {
            onError("\u672a\u5f00\u542f\u7cfb\u7edf\u5b9a\u4f4d\u670d\u52a1\uff0c\u53ef\u6253\u5f00\u5b9a\u4f4d\u540e\u91cd\u8bd5\uff0c\u6216\u624b\u52a8\u641c\u7d22\u57ce\u5e02\u3002")
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
                    onError("\u6682\u672a\u83b7\u53d6\u5230\u5b9a\u4f4d\u7ed3\u679c\uff0c\u53ef\u7a0d\u540e\u91cd\u8bd5\u6216\u624b\u52a8\u641c\u7d22\u57ce\u5e02\u3002")
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
                onError("\u5b9a\u4f4d\u7b49\u5f85\u8d85\u65f6\uff0c\u53ef\u624b\u52a8\u641c\u7d22\u5e76\u6dfb\u52a0\u57ce\u5e02\u3002")
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
