package com.app.research.good_gps

import android.location.Location
import androidx.lifecycle.ViewModel
import com.app.research.good_gps.model.Coordinates
import com.app.research.good_gps.model.EventLocReceive
import com.app.research.good_gps.model.ForeGolfTemp
import com.app.research.good_gps.model.LOCATION
import com.app.research.good_gps.model.POI_NAME
import com.app.research.good_gps.model.Payload
import com.app.research.good_gps.model.User
import com.app.research.good_gps.utils.RequestLocationUpdate
import com.app.research.good_gps.utils.SocketHelper
import com.app.research.utils.AppUtils.fromJsonString
import com.app.research.utils.AppUtils.toJsonString
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.util.Random

class ForeGolfVM : ViewModel() {

    private val _uiState = MutableStateFlow(ForeGolfUiState())
    val uiState = _uiState.asStateFlow()

    val coursesCords = ForeGolfTemp.coordinates
    var currentHole = 1

    var tempUserId = 1

    private val grounds = coursesCords.coordinates.groupBy { it.hole }

    private val mapPlayerLoc = HashMap<Int, EventLocReceive>()

     val requestLocationUpdate = RequestLocationUpdate { loc ->
         Timber.d("Location: ${loc.latitude} , ${loc.longitude}, :: $loc")
         event(ForeGolfEvent.UpdateGPSLoc(loc))
     }

    private val socketHelper = SocketHelper(
        endPoint = SocketHelper.END_POINT,
        token = SocketHelper.TOKEN,
        builder = {
            on(SocketHelper.UPDATE_USER_LOC) { data ->
                Timber.d("Location update ack: $data")

                data.fromJsonString<EventLocReceive>()?.let { er ->
                    if (er.user.id == tempUserId) return@let

                    val userId = er.user.id
                    val newLatLng = er.payload.latLng
                    val existing = mapPlayerLoc[userId]

                    if (existing == null || existing.payload.latLng != newLatLng) {
                        mapPlayerLoc[userId] = er
                        _uiState.update { cs ->
                            cs.copy(playerLoc = mapPlayerLoc.values.toList())
                        }
                    }
                }
            }
        }
    )

    init {
        tempUserId = Random().nextInt()
        updateGround()
//        socketHelper.socket.connect()
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

            is ForeGolfEvent.UpdateGPSLoc -> {
                updateGpsLoc(event.location)
            }
        }
    }

    private fun updateGpsLoc(location: Location) {

        _uiState.update { cs ->
            cs.copy(currLoc = LatLng(location.latitude, location.longitude))
        }

        if (socketHelper.socket.isConnected()) {
            val payload = Payload(
                lat = location.latitude.toString(),
                lng = location.longitude.toString(),
                timestamp = "",
                accuracy = location.accuracy,
                speed = location.speed
            )
            val eventR = EventLocReceive(
                user = User(id = tempUserId),
                payload = payload
            )

            socketHelper.socket.emit(
                SocketHelper.UPDATE_USER_LOC,
                eventR.toJsonString() ?: return
            )
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

    override fun onCleared() {
        socketHelper.socket.disconnect()
//        requestLocationUpdate.stopLocationUpdates()
        super.onCleared()
        Timber.d("CLEAR: Stopping location updates")
    }
}


sealed interface ForeGolfEvent {
    object OnNextHole : ForeGolfEvent
    object OnPreviousHole : ForeGolfEvent

    data class ChangeDistMarkPos(val latLng: LatLng) : ForeGolfEvent

    data class UpdateGPSLoc(val location: Location) : ForeGolfEvent
}


data class ForeGolfUiState(
    val hole: Int = 1,
    val maxHole: Int = ForeGolfTemp.course.numHoles,
    val selectedGround: List<Coordinates.Coordinate> = emptyList(),
    val isLoading: Boolean = false,
    val hull: List<LatLng> = emptyList(),
    val bounds: LatLngBounds = LatLngBounds(LatLng(0.0, 0.0), LatLng(0.0, 0.0)),
    val dragMarkPos: LatLng = LatLng(0.0, 0.0),
    val currLoc: LatLng = LatLng(0.0, 0.0),
    val playerLoc: List<EventLocReceive> = emptyList()
)