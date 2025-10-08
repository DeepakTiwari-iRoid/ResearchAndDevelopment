package com.app.research.good_gps.utils

import android.Manifest
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.app.research.ResearchApplication
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority

class RequestLocationUpdate(
    private val listener: LocationListener,
) {
    private val locationClient = ResearchApplication.instance.locationClient


    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    fun startLocationUpdates(usePreciseLocation: Boolean = false) {
        locationClient.requestLocationUpdates(
            LocationRequest.Builder(
                if (usePreciseLocation) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                3000L
            ).build(),
            listener,
            Looper.getMainLooper(),
        )
    }

    fun stopLocationUpdates() {
        locationClient.removeLocationUpdates(listener)
    }
}