package com.app.research.good_gps

import com.app.research.good_gps.model.Clubs
import com.google.android.gms.maps.model.LatLng
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


/*

    val polyCoordinates = listOf(
        LatLng(36.5701202, -121.9470146),
        LatLng(36.570013, -121.948721),
    )

    // Usage
    val curvedPoints = getCurvePoints(
        polyCoordinates[0],
        polyCoordinates[1],
        calculateCurve(cameraPositionState.position.zoom)
    )

    val startP = LatLng(36.57020081124087, -121.948783993721)
    val midP = draggableMarker.position //draggable marker position
    val endP = LatLng(36.57068307134521, -121.94711364805698)

    val firstSegmentDistance = getDistanceFromLatLonInKm(startP, midP)
    val secondSegmentDistance = getDistanceFromLatLonInKm(midP, endP)

    val midPoint1 = rememberUpdatedMarkerState(
        LatLng(
            (startP.latitude + midP.latitude) / 2,
            (startP.longitude + midP.longitude) / 2
        )
    )

    val midPoint2 = rememberUpdatedMarkerState(
        LatLng(
            (midP.latitude + endP.latitude) / 2,
            (midP.longitude + endP.longitude) / 2
        )
    )
val outerHull = remember(coordinatesObj) { expandPolygon(hull, 100.0) }
          Polyline(points = polyCoordinates, color = Color.Black.copy(alpha = 0.5f))
          Polyline(points = curvedPoints, color = Color.White)
  */