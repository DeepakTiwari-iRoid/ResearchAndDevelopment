package com.app.research.good_gps

import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.app.research.good_gps.model.POI_NAME
import com.app.research.singlescreen_r_d.skaifitness.HStack
import com.app.research.singlescreen_r_d.skaifitness.VStack
import com.app.research.ui.pxToDp
import com.app.research.ui.theme.white
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import timber.log.Timber
import kotlin.math.cos
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    navHostController: NavHostController,
    viewModel: ForeGolfVM,
    modifier: Modifier = Modifier,
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
            uiState = uiState,
            event = viewModel::event,
            modifier = Modifier.padding(innerPadding)
        )
    }

}


@SuppressLint("PotentialBehaviorOverride")
@OptIn(MapsComposeExperimentalApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapWithMarkers(
    uiState: ForeGolfUiState,
    event: (ForeGolfEvent) -> Unit,
    modifier: Modifier = Modifier
) {


    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val green = uiState.selectedGround.find { it.poi == POI_NAME.GREEN }

    val draggableMarker = remember { MarkerState(uiState.dragMarkPos) }
    var sliderState by remember { mutableFloatStateOf(20f) }


    val cameraPositionState = rememberCameraPositionState()
    println("Zoom: ${cameraPositionState.position.zoom}")
    var mapLoaded by remember { mutableStateOf(false) }

    var mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                mapToolbarEnabled = false,
                compassEnabled = false,
                rotationGesturesEnabled = true,
                tiltGesturesEnabled = false,
                zoomGesturesEnabled = true,
                zoomControlsEnabled = false
            )
        )
    }

    LaunchedEffect(uiState.dragMarkPos) {
        draggableMarker.position = uiState.dragMarkPos
    }

    val whiteWidth = 0.5f
    val restWidth = 1f - (whiteWidth - 0.2f)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        CourseMatrix(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .fillMaxWidth(whiteWidth)
                .zIndex(1f)
        )

        HStack(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .fillMaxWidth()
                .zIndex(1f),
            verticalAlignment = Alignment.Bottom
        ) {

            Spacer(modifier.weight(.5f))

            GroundChanging(
                modifier = Modifier.weight(.5f),
                onPrev = { event(ForeGolfEvent.OnPreviousHole) },
                onNext = {
                    event(ForeGolfEvent.OnNextHole)
                }
            )


            VerticalSlider(
                value = sliderState,
                onValueChange = { sliderState = it },
                colors = SliderDefaults.colors().copy(
                    thumbColor = Color.White,
                    activeTrackColor = Color.Blue,
                    inactiveTrackColor = Color.White,
                ),
                valueRange = 5f..20f,
                modifier = Modifier
                    .padding(vertical = 18.dp)
                    .size(150.dp, 10.dp)
                    .zIndex(1f)
            )
        }

        GoogleMap(
            properties = MapProperties(
                mapType = MapType.SATELLITE,
                latLngBoundsForCameraTarget = uiState.bounds,
            ),
            uiSettings = mapUiSettings,
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .fillMaxWidth(restWidth),
            cameraPositionState = cameraPositionState,
            onMapLoaded = {
                mapLoaded = true
            }
        ) {

            uiState.selectedGround.forEach { latLng ->
                MarkerComposable(
                    state = MarkerState(position = LatLng(latLng.latitude, latLng.longitude)),
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
                                shape = CircleShape
                            )
                    )
                }
            }

            // Draw circle
            com.google.maps.android.compose.Circle(
                center = draggableMarker.position,
                radius = sliderState.toDouble(), // radius in meters
                strokeWidth = 5f,
                zIndex = 1f,
                strokeColor = Color.Blue,
                fillColor = Color.Blue.copy(alpha = 0.1f)
            )

            // Center Marker (draggable)
            MarkerComposable(
                state = draggableMarker,
                draggable = true,
                tag = "center",
                anchor = Offset(0.5f, 0.5f),
                zIndex = 1f,
                flat = true,
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Color.White, shape = CircleShape)
                        .border(1.dp, Color.Black, shape = CircleShape)
                )
            }

            Polyline(
                color = Color.White,
                points = listOf(
                    uiState.dragMarkPos,
                    draggableMarker.position,
                    uiState.currLoc
                )
            )

            MapEffect(Unit) { map ->


                val markerDragListener = object : GoogleMap.OnMarkerDragListener {
                    override fun onMarkerDrag(marker: Marker) {
                        draggableMarker.position = marker.position
                    }

                    override fun onMarkerDragEnd(marker: Marker) {
                        draggableMarker.position = marker.position
                        Timber.d("onMarkerDragEnd: ${marker.position} ")
                    }

                    override fun onMarkerDragStart(marker: Marker) {
                        draggableMarker.position = marker.position
                        Timber.d("onMarkerDragStart: ${marker.position} ")
                    }
                }

                map.setOnMarkerDragListener(markerDragListener)

                map.setOnMarkerClickListener { p0 ->
                    Timber.d("onMarkerClick: ${p0.position}")
                    draggableMarker.position = p0.position
                    false
                }

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

    if (mapLoaded)
        LaunchedEffect(uiState.hull) {

            val update = CameraUpdateFactory.newLatLngBounds(uiState.bounds, 100)

            // Move to bounds with padding
            cameraPositionState.animate(update = update)

            val fittedZoom = cameraPositionState.position.zoom

            /**
             * @param first param is Tee Box (Lat, Lng), Green (Lat, Lng)
             * */
            val green = uiState.dragMarkPos

            val teeBox = uiState.currLoc

            // Step 2: Calculate bearing (first vs last point for orientation)
            val bearing = bearingBetween(teeBox, green)

            // Then apply rotation (bearing) while keeping zoom
            val cameraPosition = CameraPosition.Builder()
                .target(uiState.bounds.center) // Sets the center of the map to the location of the marker
                .zoom(fittedZoom) // optional offset
                .bearing(bearing.toFloat()) // Sets the orientation of the camera to east
                .build() // Creates a CameraPosition from the builder


            cameraPositionState.animate(
                update = CameraUpdateFactory.newCameraPosition(
                    cameraPosition
                )
            )
            // Step 6: Update your minZoom to match
//            minZoomPreference = fittedZoom
        }
}


@Composable
private fun CourseMatrix(
    modifier: Modifier = Modifier,
    matrix: List<POI_NAME> = POI_NAME.entries,
) {

    var maxSize by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0.0f to Color.White,
                        0.6f to Color.White,
                        8.0f to Color.Transparent    // Green fills the rest
                    ),
                    tileMode = TileMode.Clamp
                )
            ),
        contentAlignment = Alignment.CenterStart
    ) {

        LazyColumn(
            modifier = Modifier
                .wrapContentWidth()
                .fillMaxHeight()
                .graphicsLayer {
                    maxSize = if (size.width > maxSize) size.width else maxSize
                },
            horizontalAlignment = Alignment.End
        ) {
            items(items = matrix) { item ->
                StatBadge(
                    numberText = item.id.toString(),
                    labelText = item.title,
                    dividerWidth = maxSize,
                    color = if (item == POI_NAME.GREEN) Color(0xFF00982A) else Color(0xFF111111),
                    modifier = Modifier
                        .padding(vertical = 8.dp),
                )
            }
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


@Composable
fun GroundChanging(
    modifier: Modifier = Modifier,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {

    HStack(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onPrev,
            modifier = Modifier
                .background(Color.Black, shape = RoundedCornerShape(12.dp))
                .size(48.dp)
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = white)
        }

        IconButton(
            onClick = onNext,
            modifier = Modifier
                .background(Color.Black, shape = RoundedCornerShape(12.dp))
                .size(48.dp)
        ) {
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = white)
        }
    }
}


/**
 * Extension function to find the distance from this to another LatLng object
 */
private fun LatLng.distanceFrom(other: LatLng): Double {
    val result = FloatArray(1)
    Location.distanceBetween(latitude, longitude, other.latitude, other.longitude, result)
    return result[0].toDouble()
}

private fun LatLng.getPointAtDistance(distance: Double): LatLng {
    val radiusOfEarth = 6371009.0
    val radiusAngle = (Math.toDegrees(distance / radiusOfEarth)
            / cos(Math.toRadians(latitude)))
    return LatLng(latitude, longitude + radiusAngle)
}
