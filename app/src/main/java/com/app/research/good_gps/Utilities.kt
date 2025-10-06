package com.app.research.good_gps

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import com.app.research.good_gps.model.Clubs
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PatternItem
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


fun Clubs.filterClubs(query: String): Clubs {
    if (query.isEmpty()) return this.copy(clubs = clubs)
    val clubs = this.clubs.filter { club ->
        club.clubName.contains(query, ignoreCase = true) ||
                club.city.contains(query, ignoreCase = true) ||
                club.state.contains(query, ignoreCase = true) ||
                club.courses.any { course ->
                    course.courseName.contains(query, ignoreCase = true)
                }
    }
    return this.copy(clubs = clubs)
}


fun getDistanceFromLatLonInKm(p0: LatLng, p1: LatLng): Double {
    val R = 6371.0 // Radius of the earth in km
    val dLat = deg2rad(p1.latitude - p0.latitude)
    val dLon = deg2rad(p1.longitude - p0.longitude)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(deg2rad(p0.latitude)) * cos(deg2rad(p1.latitude)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val d = R * c // Distance in km
    return d
}

fun deg2rad(deg: Double): Double {
    return deg * (Math.PI / 180)
}


/**
 * Compute convex hull (Monotone Chain algorithm) for a list of LatLng points
 * @param points list of LatLng coordinates
 * @return list of LatLng forming the convex hull in counter-clockwise order
 */
fun convexHull(points: List<LatLng>): List<LatLng> {
    if (points.size <= 1) return points

    // Sort points by latitude first, then longitude
    val sorted = points.sortedWith(compareBy({ it.latitude }, { it.longitude }))

    val lower = mutableListOf<LatLng>()
    for (p in sorted) {
        while (lower.size >= 2 && cross(lower[lower.size - 2], lower.last(), p) <= 0) {
            lower.removeAt(lower.size - 1)
        }
        lower.add(p)
    }

    val upper = mutableListOf<LatLng>()
    for (p in sorted.asReversed()) {
        while (upper.size >= 2 && cross(upper[upper.size - 2], upper.last(), p) <= 0) {
            upper.removeAt(upper.size - 1)
        }
        upper.add(p)
    }

    // Remove last element of each list (it's the starting point repeated)
    lower.removeAt(lower.size - 1)
    upper.removeAt(upper.size - 1)

    return lower + upper
}

// Cross product of OA x OB
private fun cross(o: LatLng, a: LatLng, b: LatLng): Double {
    return (a.latitude - o.latitude) * (b.longitude - o.longitude) -
            (a.longitude - o.longitude) * (b.latitude - o.latitude)
}


fun expandPolygon(points: List<LatLng>, meters: Double): List<LatLng> {
    if (points.isEmpty()) return points

    val centerLat = points.map { it.latitude }.average()
    val centerLng = points.map { it.longitude }.average()
    val center = LatLng(centerLat, centerLng)

    return points.map { p ->
        val bearing = bearingBetween(center, p)
        movePoint(p, bearing, meters)
    } + listOf(points.first()) // close polygon
}

// Haversine move: shift point by distance along bearing
fun movePoint(point: LatLng, bearing: Double, distanceMeters: Double): LatLng {
    val R = 6371000.0 // Earth radius in meters
    val latRad = Math.toRadians(point.latitude)
    val lngRad = Math.toRadians(point.longitude)
    val brng = Math.toRadians(bearing)

    val newLat = asin(
        sin(latRad) * cos(distanceMeters / R) +
                cos(latRad) * sin(distanceMeters / R) * cos(brng)
    )
    val newLng = lngRad + atan2(
        sin(brng) * sin(distanceMeters / R) * cos(latRad),
        cos(distanceMeters / R) - sin(latRad) * sin(newLat)
    )

    return LatLng(Math.toDegrees(newLat), Math.toDegrees(newLng))
}

// Bearing from one point to another
fun bearingBetween(from: LatLng, to: LatLng): Double {
    val fromLat = Math.toRadians(from.latitude)
    val fromLng = Math.toRadians(from.longitude)
    val toLat = Math.toRadians(to.latitude)
    val toLng = Math.toRadians(to.longitude)


    val dLng = toLng - fromLng
    val y = sin(dLng) * cos(toLat)
    val x = cos(fromLat) * sin(toLat) - sin(fromLat) * cos(toLat) * cos(dLng)
    return (Math.toDegrees(atan2(y, x)) + 360) % 360
}

fun List<LatLng>.toLatLngBounds(): LatLngBounds.Builder {
    val builder = LatLngBounds.builder()
    this.forEach { builder.include(it) }
    return builder
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    /*@IntRange(from = 0)*/
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: SliderColors = SliderDefaults.colors()
) {

    Slider(
        colors = colors,
        interactionSource = interactionSource,
        onValueChangeFinished = onValueChangeFinished,
        steps = steps,
        valueRange = valueRange,
        enabled = enabled,
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .graphicsLayer {
                rotationZ = 270f
                transformOrigin = TransformOrigin(0f, 0f)
            }
            .layout { measurable, constraints ->
                val placeable = measurable.measure(
                    Constraints(
                        minWidth = constraints.minHeight,
                        maxWidth = constraints.maxHeight,
                        minHeight = constraints.minWidth,
                        maxHeight = constraints.maxHeight,
                    )
                )
                layout(placeable.height, placeable.width) {
                    placeable.place(-placeable.width, 0)
                }
            }
            .then(modifier)
    )
}


val MAX_ZOOM_OUT = 15.98f

class PolyLinePattern {

    private var PATTERN_DASH_LENGTH_PX = 20f

    fun setDashLength(length: Float): PolyLinePattern {
        PATTERN_DASH_LENGTH_PX = length
        return this
    }

    val GAP: Gap
        get() = Gap(PATTERN_DASH_LENGTH_PX)
    val DASH: Dash
        get() = Dash(PATTERN_DASH_LENGTH_PX)
    val DOT: Dot
        get() = Dot()

    val DASH_PATTERN: List<PatternItem>
        get() = listOf(GAP, DASH)

    val DOT_PATTERN: List<PatternItem>
        get() = listOf(GAP, DOT)
}