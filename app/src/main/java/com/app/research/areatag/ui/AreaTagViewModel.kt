package com.app.research.areatag.ui

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.research.ResearchApplication
import com.app.research.areatag.data.AreaTagStore
import com.app.research.areatag.data.Zone
import com.app.research.areatag.location.LocationProvider
import com.app.research.areatag.sensor.Orientation
import com.app.research.areatag.sensor.OrientationManager
import com.app.research.data.Constants
import com.uber.h3core.H3Core
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class AreaTagViewModel(application: Application) : AndroidViewModel(application) {


    private val orientationManager = OrientationManager(application)
    private val locationProvider = LocationProvider(application)
    private val tagStore = AreaTagStore(application)
    val h3: H3Core = ResearchApplication.instance.h3

    companion object {
        const val NEARBY_RADIUS_METERS = 50.0
        const val VISIBILITY_YAW_THRESHOLD = 30f
        const val VISIBILITY_PITCH_THRESHOLD = 25f
        const val STABILITY_THRESHOLD = 10
        private const val EARTH_RADIUS_METERS = 6_371_000.0
    }


    private val _orientation = MutableStateFlow(Orientation())
    private val _location = MutableStateFlow<Location?>(null)
    private val _dialog = MutableStateFlow<CreateTagDialogState>(CreateTagDialogState.Hidden)
    private val _zones = MutableStateFlow<List<Zone>>(emptyList())
    private val _selectedZoneId = MutableStateFlow<String?>(null)
    private val _currentHexZoneId = MutableStateFlow("")
    private val _stability = MutableStateFlow(ZoneStabilityState())

    val uiState: StateFlow<AreaTagUiState> = combine(
        _orientation,
        _location,
        _dialog,
        combine(_zones, _selectedZoneId, _currentHexZoneId) { z, s, h -> Triple(z, s, h) },
        _stability
    ) { orient, loc, dialog, (zones, selectedId, currentHexId), stability ->
        val currentZone = zones.find { it.zoneId == currentHexId }
        val resolvedZone = currentZone
            ?: if (stability.isStable && currentHexId.isNotEmpty()) {
                findNearbyZoneViaParent(currentHexId, zones)
            } else null

        val effectiveZoneId = resolvedZone?.zoneId ?: currentHexId
        val isInTargetZone = selectedId == null || selectedId == effectiveZoneId
        val rawTags = if (isInTargetZone && resolvedZone != null) {
            resolvedZone.tags
        } else emptyList()
        val arrow = if (!isInTargetZone) {
            computeZoneArrow(zones, selectedId, loc, orient)
        } else null

        AreaTagUiState(
            orientation = orient,
            location = loc,
            tagPositions = computeTagPositions(rawTags, orient, loc),
            dialog = dialog,
            zones = zones,
            selectedZoneId = selectedId,
            currentHexZoneId = currentHexId.takeIf { it.isNotEmpty() },
            zoneArrow = arrow,
            stability = stability
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AreaTagUiState())

    private var sensorsStarted = false


    init {
        reloadTags()
        viewModelScope.launch {
            combine(_currentHexZoneId, _zones, _stability) { hex, zones, stability ->
                Triple(hex, zones, stability)
            }.collect { (hex, zones, stability) ->
                if (hex.isNotEmpty() && _selectedZoneId.value == null) {
                    val directMatch = zones.any { it.zoneId == hex }
                    if (directMatch) {
                        _selectedZoneId.value = hex
                    } else if (stability.isStable) {
                        findNearbyZoneViaParent(hex, zones)?.let {
                            _selectedZoneId.value = it.zoneId
                        }
                    }
                }
            }
        }
    }


    fun onEvent(event: AreaTagEvent) {
        when (event) {
            AreaTagEvent.StartSensors -> startSensors()
            AreaTagEvent.ScreenTapped -> openCreateDialog()
            AreaTagEvent.DismissCreateDialog -> _dialog.value = CreateTagDialogState.Hidden
            is AreaTagEvent.CreateTag -> createTag(
                event.title,
                event.description,
                event.zoneTitle,
                event.zoneDescription
            )

            is AreaTagEvent.DeleteTag -> deleteTag(event.tagId)
            is AreaTagEvent.SelectZone -> _selectedZoneId.value = event.zoneId
        }
    }

    private fun startSensors() {
        if (sensorsStarted) return
        sensorsStarted = true
        viewModelScope.launch {
            orientationManager.observeOrientation().collect { _orientation.value = it }
        }
        viewModelScope.launch {
            locationProvider.observeLocation().collect {
                val newHex = h3.latLngToCellAddress(
                    it.latitude,
                    it.longitude,
                    Constants.H3_RESOLUTION
                )
                val prevHex = _currentHexZoneId.value
                val nextCount = if (newHex == prevHex && prevHex.isNotEmpty()) {
                    _stability.value.consecutiveCount + 1
                } else {
                    1
                }
                _stability.value = ZoneStabilityState(
                    consecutiveCount = nextCount,
                    threshold = STABILITY_THRESHOLD
                )
                _currentHexZoneId.value = newHex
                _location.value = it
                Timber.d("Location: hexId=$newHex, stability=$nextCount/$STABILITY_THRESHOLD")
            }
        }
    }

    private fun openCreateDialog() {
        val current = _orientation.value
        val currentHex = _currentHexZoneId.value
        if (currentHex.isEmpty()) return // no GPS yet
        val isNewZone = _zones.value.none { it.zoneId == currentHex }
        _dialog.value = CreateTagDialogState.Visible(
            yaw = current.yaw,
            pitch = current.pitch,
            isNewZone = isNewZone
        )
    }

    private fun createTag(
        title: String,
        description: String,
        zoneTitle: String?,
        zoneDescription: String?
    ) {
        val loc = _location.value ?: return
        val currentHex = _currentHexZoneId.value.ifEmpty { return }
        val snapshot = _dialog.value as? CreateTagDialogState.Visible ?: return

        val tag = Zone.Tag(
            latitude = loc.latitude,
            longitude = loc.longitude,
            yaw = snapshot.yaw,
            pitch = snapshot.pitch,
            title = title,
            description = description
        )

        val zone = if (snapshot.isNewZone) {
            Zone(
                zoneId = currentHex,
                title = zoneTitle.orEmpty(),
                description = zoneDescription.orEmpty(),
                createdAt = System.currentTimeMillis(),
                tags = listOf(tag)
            )
        } else {
            // Existing zone: carry only the new tag; store merges it.
            Zone(zoneId = currentHex, tags = listOf(tag))
        }

        Timber.d("Saving Tag zone: $zone")
        viewModelScope.launch {
            tagStore.save(zone)
            reloadTags()
        }
        _dialog.value = CreateTagDialogState.Hidden
    }

    private fun deleteTag(tagId: String) {
        viewModelScope.launch {
            tagStore.delete(tagId)
            reloadTags()
        }
    }

    private fun reloadTags() {
        _zones.value = tagStore.loadAll()
    }

    private fun findNearbyZoneViaParent(currentHexId: String, zones: List<Zone>): Zone? {
        return runCatching {
            val parentRes = Constants.H3_RESOLUTION - 1
            val parentCell = h3.cellToParentAddress(currentHexId, parentRes)
            val siblingCells = h3.cellToChildren(parentCell, Constants.H3_RESOLUTION)
            val zoneMap = zones.associateBy { it.zoneId }
            val match = siblingCells.firstNotNullOfOrNull { sibling -> zoneMap[sibling] }
            if (match != null) {
                Timber.d("Parent-cell fallback: resolved ${match.zoneId} from parent $parentCell")
            }
            match
        }.onFailure {
            Timber.e(it, "Parent-cell fallback failed for $currentHexId")
        }.getOrNull()
    }

    private fun computeTagPositions(
        tags: List<Zone.Tag>,
        orient: Orientation,
        loc: Location?
    ): List<TagScreenPosition> {
        if (loc == null) return emptyList()
        return tags.map { tag ->
            val deltaYaw = angleDelta(orient.yaw, tag.yaw)
            val deltaPitch = tag.pitch - orient.pitch
            val isVisible = abs(deltaYaw) <= VISIBILITY_YAW_THRESHOLD &&
                    abs(deltaPitch) <= VISIBILITY_PITCH_THRESHOLD
            TagScreenPosition(
                tag = tag,
                deltaYaw = deltaYaw,
                deltaPitch = deltaPitch,
                distanceMeters = 0.0,
                isVisible = isVisible
            )
        }
    }

    private fun computeZoneArrow(
        zones: List<Zone>,
        selectedId: String?,
        loc: Location?,
        orient: Orientation
    ): ZoneArrowState? {
        val id = selectedId ?: return null
        if (loc == null) return null
        val zone = zones.find { it.zoneId == id } ?: return null
        val center = runCatching { h3.cellToLatLng(id) }.getOrNull() ?: return null
        val bearing = bearingDegrees(loc.latitude, loc.longitude, center.lat, center.lng)
        val distance = haversineMeters(loc.latitude, loc.longitude, center.lat, center.lng)
        return ZoneArrowState(
            zoneId = id,
            colorArgb = zone.color,
            title = zone.title.ifBlank { "Zone" },
            deltaYaw = angleDelta(orient.yaw, bearing),
            distanceMeters = distance
        )
    }

    /** Returns signed angle difference (-180 to +180) */
    private fun angleDelta(current: Float, saved: Float): Float {
        var delta = saved - current
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        return delta
    }

    /** Initial bearing from (lat1,lon1) to (lat2,lon2), degrees 0..360 (0 = north, clockwise). */
    private fun bearingDegrees(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val dLon = Math.toRadians(lon2 - lon1)
        val y = sin(dLon) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(dLon)
        val deg = Math.toDegrees(atan2(y, x))
        return ((deg + 360.0) % 360.0).toFloat()
    }

    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val dPhi = Math.toRadians(lat2 - lat1)
        val dLam = Math.toRadians(lon2 - lon1)
        val a = sin(dPhi / 2).let { it * it } +
                cos(phi1) * cos(phi2) * sin(dLam / 2).let { it * it }
        return 2 * EARTH_RADIUS_METERS * atan2(sqrt(a), sqrt(1 - a))
    }

    override fun onCleared() {
        super.onCleared()
        sensorsStarted = false
    }
}


data class TagScreenPosition(
    val tag: Zone.Tag,
    val deltaYaw: Float,
    val deltaPitch: Float,
    val distanceMeters: Double,
    val isVisible: Boolean
)

data class ZoneArrowState(
    val zoneId: String,
    val colorArgb: Int,
    val title: String,
    /** Signed horizontal angle from current yaw to zone bearing (-180..180). 0 = straight ahead. */
    val deltaYaw: Float,
    val distanceMeters: Double
)

sealed interface CreateTagDialogState {
    data object Hidden : CreateTagDialogState
    data class Visible(
        val yaw: Float,
        val pitch: Float,
        val isNewZone: Boolean
    ) : CreateTagDialogState
}

data class AreaTagUiState(
    val orientation: Orientation = Orientation(),
    val location: Location? = null,
    val tagPositions: List<TagScreenPosition> = emptyList(),
    val dialog: CreateTagDialogState = CreateTagDialogState.Hidden,
    val zones: List<Zone> = emptyList(),
    val selectedZoneId: String? = null,
    val currentHexZoneId: String? = null,
    val zoneArrow: ZoneArrowState? = null,
    val stability: ZoneStabilityState = ZoneStabilityState()
)

data class ZoneStabilityState(
    val consecutiveCount: Int = 0,
    val threshold: Int = 10
) {
    val isStable: Boolean get() = consecutiveCount >= threshold
}

sealed interface AreaTagEvent {
    data object StartSensors : AreaTagEvent
    data object ScreenTapped : AreaTagEvent
    data object DismissCreateDialog : AreaTagEvent
    data class CreateTag(
        val title: String,
        val description: String,
        val zoneTitle: String? = null,
        val zoneDescription: String? = null
    ) : AreaTagEvent

    data class DeleteTag(val tagId: String) : AreaTagEvent
    data class SelectZone(val zoneId: String) : AreaTagEvent
}
