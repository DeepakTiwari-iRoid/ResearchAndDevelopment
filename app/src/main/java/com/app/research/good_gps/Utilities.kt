package com.app.research.good_gps

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PointF
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import com.app.research.good_gps.model.Clubs
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PatternItem
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
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


fun maxDistanceOnHull(points: List<LatLng>): Pair<LatLng, LatLng> {
    if (points.size < 2) return Pair(points[0], points[0])

    var maxDist = 0.0
    var farthestPair = Pair(points[0], points[0])
    val n = points.size

    var j = 1
    for (i in points.indices) {
        val nextI = (i + 1) % n
        while (area2(points[i], points[nextI], points[(j + 1) % n]) >
            area2(points[i], points[nextI], points[j])
        ) {
            j = (j + 1) % n
        }

        val dist = haversineDistance(points[i], points[j])
        if (dist > maxDist) {
            maxDist = dist
            farthestPair = Pair(points[i], points[j])
        }
    }
    return farthestPair
}

private fun area2(a: LatLng, b: LatLng, c: LatLng): Double {
    return abs(
        (b.longitude - a.longitude) * (c.latitude - a.latitude) -
                (b.latitude - a.latitude) * (c.longitude - a.longitude)
    )
}

private fun haversineDistance(a: LatLng, b: LatLng): Double {
    val R = 6371000.0 // Earth's radius in meters
    val lat1 = Math.toRadians(a.latitude)
    val lat2 = Math.toRadians(b.latitude)
    val dLat = Math.toRadians(b.latitude - a.latitude)
    val dLon = Math.toRadians(b.longitude - a.longitude)

    val h = sin(dLat / 2).pow(2) +
            cos(lat1) * cos(lat2) *
            sin(dLon / 2).pow(2)
    return 2 * R * asin(sqrt(h)) // distance in meters
}

fun createExpandedPolygonBitmap(
    points: List<Point>,
    expandBy: Float = 1f,
    layers: Int = 100,
    strokeWidth: Float = 2f,
    color: Color = Color.Magenta,
    width:Int,
    height:Int
): Bitmap? {
    if (points.isEmpty()) return null

    val bounds = calculateBounds(points)

    // Add margin for expansion
    val margin = expandBy * layers + strokeWidth
    val width = (width + margin * 2).toInt()
    val height = (height + margin * 2).toInt()

    if (width <= 0 || height <= 0) return null

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        this.strokeWidth = strokeWidth
    }

    // Draw all outward layers
    for (i in 1..layers) {
        val expansion = expandBy * i
        val expandedPoints = points.map { p ->
            var newX = p.x.toFloat() - bounds.left + margin
            var newY = p.y.toFloat() - bounds.top + margin

            when {
                p.x.toFloat() == bounds.left -> newX -= expansion
                p.x.toFloat() == bounds.right -> newX += expansion
            }

            when {
                p.y.toFloat() == bounds.top -> newY -= expansion
                p.y.toFloat() == bounds.bottom -> newY += expansion
            }

            PointF(newX, newY)
        }

        val path = android.graphics.Path().apply {
            expandedPoints.forEachIndexed { index, point ->
                if (index == 0) moveTo(point.x, point.y)
                else lineTo(point.x, point.y)
            }
            close()
        }

        paint.color = color.copy(alpha = 1f - (i.toFloat() / layers.toFloat())).toArgb()
        canvas.drawPath(path, paint)
    }

    return bitmap
}

fun calculateBounds(points: List<Point>): Bounds {
    val left = points.minOf { it.x }
    val right = points.maxOf { it.x }
    val top = points.minOf { it.y }
    val bottom = points.maxOf { it.y }

    return Bounds(left.toFloat(), right.toFloat(), top.toFloat(), bottom.toFloat())
}

data class Bounds(
    val left: Float,
    val right: Float,
    val top: Float,
    val bottom: Float
)

/*
    val bitmap = createBitmap(mapViewWidth, mapViewHeight)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Step 1: Dark overlay
    paint.color = Color.argb(200, 0, 0, 0)
    canvas.drawRect(0f, 0f, mapViewWidth.toFloat(), mapViewHeight.toFloat(), paint)

    // Step 2: Create a path for the polygon (in screen pixel coordinates)
    val path = Path()
    polygonPoints.forEachIndexed { index, latLng ->
        val screenPoint = projection.toScreenLocation(latLng)
        if (index == 0) {
            path.moveTo(screenPoint.x.toFloat(), screenPoint.y.toFloat())
        } else {
            path.lineTo(screenPoint.x.toFloat(), screenPoint.y.toFloat())
        }
    }
    path.close()

    // Step 3: Cut out the polygon hole
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    canvas.drawPath(path, paint)

    // Step 4: Add a cloudy/blurred edge (optional)
    val blurPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(150, 0, 0, 0)
        maskFilter = BlurMaskFilter(60f, BlurMaskFilter.Blur.NORMAL)
    }
    canvas.drawPath(path, blurPaint)

* */