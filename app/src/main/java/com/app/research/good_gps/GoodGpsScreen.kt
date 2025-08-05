package com.app.research.good_gps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.research.utils.AppUtils.calculateCurve
import com.app.research.utils.AppUtils.getCurvePoints
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState


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
            coordinate.firstOrNull() ?: LatLng(0.0, 0.0), 14f
        )
    }

    println("Zoom: ${cameraPositionState.position.zoom}")



    GoogleMap(
        properties = MapProperties(
            mapType = MapType.SATELLITE,
        ), modifier = modifier.fillMaxSize(), cameraPositionState = cameraPositionState
    ) {
        coordinates.coordinates.forEach { coordinate ->
            MarkerComposable(
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
            }
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
    }
}