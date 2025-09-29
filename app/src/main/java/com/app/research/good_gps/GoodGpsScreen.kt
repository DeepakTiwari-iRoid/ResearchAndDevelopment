package com.app.research.good_gps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.app.research.R
import com.app.research.utils.AppUtils.calculateCurve
import com.app.research.utils.AppUtils.getCurvePoints
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


@Composable
fun GoodGpsScreen(
    modifier: Modifier = Modifier
) {

    MapWithMarkers(
        coordinates = fromGson, modifier = modifier
    )

}


@Composable
fun MapWithMarkers(coordinates: Coordinates, modifier: Modifier = Modifier) {


    val coordinate = coordinates.coordinates.take(1).map { latLng ->
        LatLng(latLng.latitude, latLng.longitude)
    }


    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(36.5700666, -121.9478678), 21f
        )
    }

    val draggableMarker = rememberUpdatedMarkerState(position = LatLng(36.5700666, -121.9478678))

    println("Zoom: ${cameraPositionState.position.zoom}")

    GoogleMap(
        properties = MapProperties(
            mapType = MapType.SATELLITE,
        ), modifier = modifier.fillMaxSize(), cameraPositionState = cameraPositionState
    ) {

        /*coordinates.coordinates.forEach { coordinate ->
            *//*MarkerComposable(
                state = MarkerState(position = LatLng(coordinate.latitude, coordinate.longitude)),
                title = "Coordinate Values",
            ) {
                Text(
                    text = "Lat: ${coordinate.latitude}, Lng: ${coordinate.longitude}\n" + "POI : ${
                        POI_NAME.fromId(
                            coordinate.poi.toInt()
                        )
                    },\n" + "sideFW : ${SIDE_OF_FAIRWAY.fromId(coordinate.sideFW.toInt())}\n" + "Hole : ${coordinate.hole}",
                    style = TextStyle(fontSize = 10.sp),
                    modifier = Modifier
                        .background(
                            Color.LightGray, shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 0.dp)
                        )
                        .padding(8.dp)
                )
            }*//*
            Marker(
                state = MarkerState(
                    position = LatLng(
                        coordinate.latitude,
                        coordinate.longitude
                    )
                )
            )
        }*/

        MarkerComposable(
            state = draggableMarker,
            draggable = true,
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
        }

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
        Polyline(points = polyCoordinates, color = Color.Black.copy(alpha = 0.5f))
        Polyline(points = curvedPoints, color = Color.White)


        val startP = LatLng(36.57020081124087, -121.948783993721)
        val midP = draggableMarker.position //draggable marker position
        val endP = LatLng(36.57068307134521, -121.94711364805698)

        val firstSegmentDistance = getDistanceFromLatLonInKm(startP, midP)
        val secondSegmentDistance = getDistanceFromLatLonInKm(midP, endP)

        Polyline(
            tag = {
                getDistanceFromLatLonInKm(
                    p0 = LatLng(36.57020081124087, -121.948783993721),
                    p1 = draggableMarker.position
                )
            },
            color = Color.Blue,
            points = listOf(
                LatLng(36.57020081124087, -121.948783993721),
                draggableMarker.position,
                LatLng(36.57068307134521, -121.94711364805698)
            )
        )

        val state1 = rememberUpdatedMarkerState(
            LatLng(
                (startP.latitude + midP.latitude) / 2,
                (startP.longitude + midP.longitude) / 2
            )
        )

        MarkerInfoWindow(
            state = state1,
            title = "Mid Point1 Marker",
        ) {
            Text(
                text = (firstSegmentDistance * 1000).toString() + " m",
                color = Color.White,
                modifier = Modifier.background(Color.Black)
            )
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