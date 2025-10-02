package com.app.research.good_gps

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.app.research.utils.AppUtils.calculateCurve
import com.app.research.utils.AppUtils.getCurvePoints
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.launch
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


@Composable
fun ForeGolfScreen(
    modifier: Modifier = Modifier
) {

    MapWithMarkers(
        coordinatesObj = fromGson, modifier = modifier
    )

}


@SuppressLint("PotentialBehaviorOverride")
@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MapWithMarkers(coordinatesObj: Coordinates, modifier: Modifier = Modifier) {


    val coordinate = coordinatesObj.coordinates.take(1).map { latLng ->
        LatLng(latLng.latitude, latLng.longitude)
    }

    val coordinates = coordinatesObj.coordinates.filter { it.hole == 3 }.map { latLng ->
        LatLng(latLng.latitude, latLng.longitude)
    }


    val cameraPositionState = rememberCameraPositionState()

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    println("Zoom: ${cameraPositionState.position.zoom}")

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

    val draggableMarker = remember { MarkerState(position = LatLng(0.0, 0.0)) }


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

    val hull = remember(coordinatesObj) { convexHull(coordinates) }
    val outerHull = remember(coordinatesObj) { expandPolygon(hull, 100.0) }

    val bounds = remember(hull) {
        LatLngBounds.builder().apply {
            hull.forEach { include(it) }
        }.build()
    }

    var minZoomPreference by remember { mutableFloatStateOf(cameraPositionState.position.zoom) }

    LaunchedEffect(hull) {

        val update = CameraUpdateFactory.newLatLngBounds(bounds, 100)

        // Move to bounds with padding
        cameraPositionState.animate(update = update)

        // Step 2: Calculate bearing (first vs last point for orientation)
//        val bearing = bearingBetween(hull.first(), hull.last())
        /**
         * @param first param is Tee Box (Lat, Lng), Green (Lat, Lng)
         * */
        val bearing =
            bearingBetween(LatLng(36.5691802, -121.9412614), LatLng(36.5673836, -121.9445853))

        // Then apply rotation (bearing) while keeping zoom
        val cameraPosition = CameraPosition.Builder()
            .target(bounds.center) // Sets the center of the map to the location of the marker
            .zoom(cameraPositionState.position.zoom) // Sets the zoom
            .bearing(bearing.toFloat()) // Sets the orientation of the camera to east
            .build() // Creates a CameraPosition from the builder

        cameraPositionState.animate(update = CameraUpdateFactory.newCameraPosition(cameraPosition))

        minZoomPreference = cameraPositionState.position.zoom
    }



    Log.d("Marker", "Hull: $hull")
    GoogleMap(
        properties = MapProperties(
            mapType = MapType.SATELLITE,
            latLngBoundsForCameraTarget = bounds,
            minZoomPreference = minZoomPreference,
        ),
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
    ) {

        coordinates.forEach { latLng ->
            Marker(
                state = MarkerState(position = latLng),
                title = "Point",
                snippet = "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}"
            )
        }

        /*        MarkerComposable(
                    state = draggableMarker,
                    draggable = true,
                    tag = "Circular Dragger",
                    flat = true,
                    anchor = Offset(0.5f, 0.5f),
                    onClick = {
                        println("Marker Dragged to ${it.position}")
                        false
                    }
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_foregolf_drag_marker),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )

                    *//* Circle(
                 center = draggableMarker.position,
                 radius = 5.0,
                 strokeColor = Color.White.copy(alpha = 0.5f),
                 fillColor = Color.Transparent,
                 strokeWidth = 2f
             )*//*

        }*/


        /*
                Polyline(points = polyCoordinates, color = Color.Black.copy(alpha = 0.5f))
                Polyline(points = curvedPoints, color = Color.White)
        */

        /*        Polyline(
                    color = Color.Blue,
                    points = listOf(
                        LatLng(36.57020081124087, -121.948783993721),
                        draggableMarker.position,
                        LatLng(36.57068307134521, -121.94711364805698)
                    )
                )*/

        /*        MarkerComposable(
                    state = midPoint1,
                    title = "Start - Mid",
                    anchor = Offset(0.5f, 0.5f),
                    infoWindowAnchor = Offset(0.5f, 0.5f),
                    snippet = "${(firstSegmentDistance * 1000).toInt()} m"
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(color = Color.Red)
                    )
                }*/

        /*        MarkerComposable(
                    state = midPoint2,
                    title = "Mid - End",
                    infoWindowAnchor = Offset(0.5f, 0.5f),
                    anchor = Offset(0.5f, 0.5f),
                    snippet = "${(secondSegmentDistance * 1000).toInt()} m",
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(color = Color.Green)
                    )
                }*/

        Polyline(
            points = hull,
            color = Color.Green,
            jointType = JointType.ROUND,
        )

        /*
                Polygon(
                    points = outerHull,
                    strokeColor = Color.Green,
                    fillColor = Color.Transparent.copy(alpha = 0.2f),
                    strokeWidth = 4f,
                    strokeJointType = JointType.ROUND
                )
        */


        MapEffect(Unit) { map ->

            val markerDragListener = object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDrag(p0: Marker) {
                    Log.d("TAG", "onMarkerDrag: ${p0.position}")
                    draggableMarker.position = p0.position
                    Log.d("TAG", "Draggable Marker: ${draggableMarker.position}")
                }

                override fun onMarkerDragEnd(p0: Marker) {
                    draggableMarker.position = p0.position
                    Log.d("TAG", "onMarkerDragEnd: ${p0.position}")
                }

                override fun onMarkerDragStart(p0: Marker) {
                    draggableMarker.position = p0.position
                    Log.d("TAG", "onMarkerDragStart: ${p0.position}")
                }

            }


            /*       val layer = KmlLayer(map, R.raw.swiss_map_cantons, ctx)
                   layer.addLayerToMap()

                   val markerManager = MarkerManager(map)
                   val singleMarkerCollection = markerManager.newCollection()
                   singleMarkerCollection.addMarker(
                       MarkerOptions()
                           .position(draggableMarker.position)
                           .draggable(true)
                           .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                           .title("Normal marker")
                   )

                   singleMarkerCollection.setOnMarkerDragListener(markerDragListener)
       */

            map.setOnMarkerDragListener(markerDragListener)
            map.setOnMapLongClickListener { coordinate ->
                scope.launch {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(
                            coordinate,
                            20f
                        )
                    )
                }
            }


        }
    }
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
