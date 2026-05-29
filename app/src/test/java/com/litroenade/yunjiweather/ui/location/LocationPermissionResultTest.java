package com.litroenade.yunjiweather.ui.location;

import android.Manifest;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocationPermissionResultTest {

    @Test
    public void hasUsablePermissionReturnsTrueWhenFineLocationGranted() {
        Map<String, Boolean> grants = new HashMap<>();
        grants.put(Manifest.permission.ACCESS_FINE_LOCATION, true);
        grants.put(Manifest.permission.ACCESS_COARSE_LOCATION, false);

        assertTrue(LocationPermissionResult.hasUsablePermission(grants));
    }

    @Test
    public void hasUsablePermissionReturnsTrueWhenCoarseLocationGranted() {
        Map<String, Boolean> grants = new HashMap<>();
        grants.put(Manifest.permission.ACCESS_FINE_LOCATION, false);
        grants.put(Manifest.permission.ACCESS_COARSE_LOCATION, true);

        assertTrue(LocationPermissionResult.hasUsablePermission(grants));
    }

    @Test
    public void hasUsablePermissionReturnsFalseWhenAllLocationPermissionsDenied() {
        assertFalse(LocationPermissionResult.hasUsablePermission(Collections.emptyMap()));
    }
}
