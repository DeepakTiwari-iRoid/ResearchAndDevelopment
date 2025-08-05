package com.app.research.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object AppUtils {

    fun getCurvePoints(start: LatLng, end: LatLng, curvature: Double = 0.2): List<LatLng> {
        val numPoints = 100
        val result = mutableListOf<LatLng>()

        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)

        // Compute the distance and heading
        val d = 2 * asin(
            sqrt(
                sin((lat2 - lat1) / 2).pow(2) +
                        cos(lat1) * cos(lat2) * sin((lon2 - lon1) / 2).pow(2)
            )
        )

        val heading = atan2(
            sin(lon2 - lon1) * cos(lat2),
            cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(lon2 - lon1)
        )

        for (i in 0..numPoints) {
            val t = i.toDouble() / numPoints
            val delta = d * t

            // Curve offset using a sine wave
            val offset = sin(Math.PI * t) * curvature

            // Interpolate latitude and longitude on the great circle
            val A = sin((1 - t) * d) / sin(d)
            val B = sin(t * d) / sin(d)

            val x = A * cos(lat1) * cos(lon1) + B * cos(lat2) * cos(lon2)
            val y = A * cos(lat1) * sin(lon1) + B * cos(lat2) * sin(lon2)
            val z = A * sin(lat1) + B * sin(lat2)

            var interpolatedLat = atan2(z, sqrt(x * x + y * y))
            var interpolatedLng = atan2(y, x)

            // Apply perpendicular offset for curvature
            interpolatedLat += offset * cos(heading + Math.PI / 2)
            interpolatedLng += offset * sin(heading + Math.PI / 2)

            result.add(LatLng(Math.toDegrees(interpolatedLat), Math.toDegrees(interpolatedLng)))
        }

        return result
    }

    fun calculateCurve(zoom: Float): Double {
        val minZoom = 19f
        val maxZoom = 21f
        val maxCurve = 0.0000005

        val progress = ((zoom - minZoom) / (maxZoom - minZoom)).coerceIn(0f, 1f)
        return maxCurve * progress
    }

}