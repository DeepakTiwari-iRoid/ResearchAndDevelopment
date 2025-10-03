package com.app.research.good_gps

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.app.research.good_gps.model.Coordinates
import com.app.research.good_gps.model.ForeGolfTemp.coordinates
import com.app.research.good_gps.model.POI_NAME
import com.app.research.singlescreen_r_d.skaifitness.VStack
import com.app.research.ui.pxToDp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    navHostController: NavHostController,
    viewModel: ForeGolfVM,
    modifier: Modifier = Modifier,
) {

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .background(Color(0xFFE4E4E6))
                    .statusBarsPadding()
                    .height(48.dp)
                    .fillMaxWidth()
            ) {
                IconButton(onClick = { navHostController.popBackStack() }) {
                    Image(
                        imageVector = Icons.Filled.KeyboardArrowLeft,
                        contentDescription = null
                    )
                }
            }
        }
    ) { innerPadding ->
        MapWithMarkers(
            coordinatesObj = coordinates,
            modifier = Modifier.padding(innerPadding)
        )
    }

}


@SuppressLint("PotentialBehaviorOverride")
@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MapWithMarkers(coordinatesObj: Coordinates, modifier: Modifier = Modifier) {


    val hole = coordinatesObj.coordinates.groupBy { it.hole }


    val cameraPositionState = rememberCameraPositionState()

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    println("Zoom: ${cameraPositionState.position.zoom}")


    val draggableMarker = remember { MarkerState(position = LatLng(0.0, 0.0)) }

    val hull =
        remember(coordinatesObj) { convexHull(emptyList()) } //TODO: change to coordinatesObj.coordinates.map { LatLng(it.latitude, it.longitude) }

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

        /**
         * @param first param is Tee Box (Lat, Lng), Green (Lat, Lng)
         * */
        val green = LatLng(36.5703434, -121.9471006)
        val teeBox = LatLng(36.5694545, -121.9418075)
        // Step 2: Calculate bearing (first vs last point for orientation)
        val bearing = bearingBetween(green, teeBox)

        // Then apply rotation (bearing) while keeping zoom
        val cameraPosition = CameraPosition.Builder()
            .target(bounds.center) // Sets the center of the map to the location of the marker
            .zoom(cameraPositionState.position.zoom + 1.5f) // Sets the zoom
            .bearing(bearing.toFloat()) // Sets the orientation of the camera to east
            .build() // Creates a CameraPosition from the builder


        cameraPositionState.animate(update = CameraUpdateFactory.newCameraPosition(cameraPosition))
        minZoomPreference = cameraPositionState.position.zoom
    }


    val whiteWidth = 0.5f

    Log.d("Marker", "Hull: $hull")
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(whiteWidth)
                .zIndex(1f)
                .align(Alignment.CenterStart)
                .background(
                    brush = Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.White,
                            0.6f to Color.White,
                            1.0f to Color.Transparent    // Green fills the rest
                        ),
                        tileMode = TileMode.Clamp
                    )
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            CourseMatrix(
                modifier = Modifier
                    .wrapContentWidth()
                    .fillMaxHeight()
            )
        }

        GoogleMap(
            properties = MapProperties(
                mapType = MapType.SATELLITE,
                latLngBoundsForCameraTarget = bounds,
                minZoomPreference = minZoomPreference,
            ),
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .fillMaxWidth(1f - (whiteWidth - 0.2f)),
            cameraPositionState = cameraPositionState,
        ) {

            hole[1]?.forEach { latLng ->
                MarkerComposable(
                    state = MarkerState(position = LatLng(latLng.longitude, latLng.latitude)),
                    title = "Point",
                    snippet = "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}"
                ) {
                    val rnd = Random
                    val red = rnd.nextInt(0, 255)
                    val green = rnd.nextInt(0, 255)
                    val blue = rnd.nextInt(0, 255)

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = Color(red, green, blue, 255),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
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


            /*        Polyline(
                        color = Color.Blue,
                        points = listOf(
                            LatLng(36.57020081124087, -121.948783993721),
                            draggableMarker.position,
                            LatLng(36.57068307134521, -121.94711364805698)
                        )
                    )*/



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

                map.setOnMarkerDragListener(markerDragListener)
                map.setOnMapLongClickListener { coordinate ->
                    /*scope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(
                                coordinate,
                                20f
                            )
                        )
                    }*/
                }

            }
        }
    }
}


@Composable
private fun CourseMatrix(
    modifier: Modifier = Modifier,
    matrix: List<POI_NAME> = POI_NAME.entries,
) {

    var maxSize by remember { mutableFloatStateOf(0f) }

    LazyColumn(
        modifier = modifier.graphicsLayer {
            maxSize = if (size.width > maxSize) size.width else maxSize
        },
        horizontalAlignment = Alignment.End
    ) {
        items(items = matrix) { item ->
            StatBadge(
                numberText = item.id.toString(),
                labelText = item.title,
                dividerWidth = maxSize,
                modifier = Modifier
                    .padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
fun StatBadge(
    numberText: String,
    labelText: String,
    modifier: Modifier = Modifier,
    dividerWidth: Float = 0f,
    color: Color = Color(0xFF111111),
) {


    VStack(
        modifier = modifier, // Fill full width
        horizontalAlignment = Alignment.End,
        spaceBy = 0.dp
    ) {
        // small label on top
        Text(
            text = labelText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier // Fill width for proper alignment
        )

        // big number below
        Text(
            text = numberText,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            textAlign = TextAlign.End,
            modifier = Modifier // Fill width for proper alignment
        )

        // Divider between item
        HorizontalDivider(modifier = Modifier.width(dividerWidth.pxToDp()))
    }
}


@Preview(showBackground = true)
@Composable
fun StatBadgePreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Example that looks like your image
        StatBadge(
            numberText = "1",
            labelText = "150 Layup",
            color = Color.Black // transparent
        )
    }
}