package com.litroenade.yunjiweather.ui.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import java.util.Map;

public final class LocationPermissionResult {

    public static final String[] RUNTIME_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private LocationPermissionResult() {
    }

    public static boolean hasUsablePermission(Map<String, Boolean> grantResults) {
        if (grantResults == null || grantResults.isEmpty()) {
            return false;
        }
        return Boolean.TRUE.equals(grantResults.get(Manifest.permission.ACCESS_FINE_LOCATION))
                || Boolean.TRUE.equals(grantResults.get(Manifest.permission.ACCESS_COARSE_LOCATION));
    }

    public static boolean hasUsablePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
}
