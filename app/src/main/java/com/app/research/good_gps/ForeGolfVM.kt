package com.app.research.good_gps

import androidx.lifecycle.ViewModel
import com.app.research.good_gps.model.Coordinates
import com.app.research.good_gps.model.ForeGolfTemp
import com.app.research.good_gps.model.LOCATION
import com.app.research.good_gps.model.POI_NAME
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

class ForeGolfVM : ViewModel() {

    private val _uiState = MutableStateFlow(ForeGolfUiState())
    val uiState = _uiState.asStateFlow()

    val coursesCords = ForeGolfTemp.coordinates
    var currentHole = 1

    private val grounds = coursesCords.coordinates.groupBy { it.hole }


    init {
        updateGround()
    }

    fun event(event: ForeGolfEvent) {
        when (event) {
            is ForeGolfEvent.OnNextHole -> {
                // Handle next hole event
                currentHole += 1
                updateGround()
                Timber.d("Next hole: $currentHole -:::::- ${grounds[currentHole]}")
            }

            is ForeGolfEvent.OnPreviousHole -> {
                // Handle previous hole event
                currentHole -= 1
                updateGround()
                Timber.d("Previous hole: $currentHole -:::::- ${grounds[currentHole]}")
            }

            is ForeGolfEvent.ChangeDistMarkPos -> {
                updateDistMarkPos(event.latLng)
            }
        }
    }

    private fun updateDistMarkPos(latLng: LatLng) {
        _uiState.update { cs ->
            cs.copy(dragMarkPos = latLng)
        }
    }

    fun updateGround() {
        val maxNumHole = uiState.value.maxHole
        val toFindHole: Int = (currentHole % maxNumHole).coerceIn(1, maxNumHole)
        val ground = grounds[toFindHole] ?: emptyList()
        val hull = convexHull(ground.map { co -> LatLng(co.latitude, co.longitude) })

        _uiState.update { currentState ->
            currentState.copy(
                selectedGround = ground,
                hull = hull,
                bounds = hull.toLatLngBounds().build(),
                dragMarkPos = ground.first { it.poi == POI_NAME.GREEN && it.location == LOCATION.MIDDLE }.latLng,
                currLoc = ground.first { it.poi == POI_NAME.BACK_TEE }.latLng,
            )
        }
    }
}


sealed interface ForeGolfEvent {
    object OnNextHole : ForeGolfEvent
    object OnPreviousHole : ForeGolfEvent

    data class ChangeDistMarkPos(val latLng: LatLng) : ForeGolfEvent
}


data class ForeGolfUiState(
    val hole: Int = 1,
    val maxHole: Int = ForeGolfTemp.course.numHoles,
    val selectedGround: List<Coordinates.Coordinate> = emptyList(),
    val isLoading: Boolean = false,
    val hull: List<LatLng> = emptyList(),
    val bounds: LatLngBounds = LatLngBounds(LatLng(0.0, 0.0), LatLng(0.0, 0.0)),
    val dragMarkPos: LatLng = LatLng(0.0, 0.0),
    val currLoc: LatLng = LatLng(0.0, 0.0)
)