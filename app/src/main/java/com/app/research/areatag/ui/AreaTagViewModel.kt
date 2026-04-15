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

class AreaTagViewModel(application: Application) : AndroidViewModel(application) {


    private val orientationManager = OrientationManager(application)
    private val locationProvider = LocationProvider(application)
    private val tagStore = AreaTagStore(application)
    private var hexZoneId: String = ""
    val h3: H3Core = ResearchApplication.instance.h3
    private var zones = emptyList<Zone>()

    companion object {
        const val NEARBY_RADIUS_METERS = 50.0
        const val VISIBILITY_YAW_THRESHOLD = 30f
        const val VISIBILITY_PITCH_THRESHOLD = 25f
    }


    private val _orientation = MutableStateFlow(Orientation())
    private val _location = MutableStateFlow<Location?>(null)
    private val _allTags = MutableStateFlow<List<Zone.Tag>>(emptyList())
    private val _dialog = MutableStateFlow<CreateTagDialogState>(CreateTagDialogState.Hidden)

    val uiState: StateFlow<AreaTagUiState> = combine(
        _orientation,
        _location,
        _allTags,
        _dialog
    ) { orient, loc, tags, dialog ->
        AreaTagUiState(
            orientation = orient,
            location = loc,
            tagPositions = computeTagPositions(tags, orient, loc),
            dialog = dialog
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AreaTagUiState())

    private var sensorsStarted = false


    init {
        reloadTags()
    }


    fun onEvent(event: AreaTagEvent) {
        when (event) {
            AreaTagEvent.StartSensors -> startSensors()
            AreaTagEvent.ScreenTapped -> openCreateDialog()
            AreaTagEvent.DismissCreateDialog -> _dialog.value = CreateTagDialogState.Hidden
            is AreaTagEvent.CreateTag -> createTag(event.title, event.description)
            is AreaTagEvent.DeleteTag -> deleteTag(event.tagId)
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
                _location.value = it
                hexZoneId = h3.latLngToCellAddress(
                    it.latitude,
                    it.longitude,
                    Constants.H3_RESOLUTION
                )
                updateZoneTags()
                Timber.d("Current Location: hexId = $hexZoneId, location = $it")
            }
            reloadTags()
        }
    }

    private fun openCreateDialog() {
        val current = _orientation.value
        _dialog.value = CreateTagDialogState.Visible(yaw = current.yaw, pitch = current.pitch)
    }

    private fun createTag(title: String, description: String) {
        val loc = _location.value ?: return
        val snapshot = _dialog.value as? CreateTagDialogState.Visible ?: return

        val tag = Zone.Tag(
            latitude = loc.latitude,
            longitude = loc.longitude,
            yaw = snapshot.yaw,
            pitch = snapshot.pitch,
            title = title,
            description = description
        )

        val zone = Zone(
            zoneId = hexZoneId,
            title = "titleTex",
            description = "description",
            tags = listOf(tag)
        )

        Timber.d("Saving Tag zone: $zone")
        viewModelScope.launch {
            tagStore.save(zone)
            updateZoneTags()
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
        zones = tagStore.loadAll()
        // need selection method to navigate to specific zone tags
        updateZoneTags()
    }


    fun updateZoneTags() {
        val selectedZone = zones.find { it.zoneId == hexZoneId }?.tags
        _allTags.value = selectedZone ?: emptyList()
        Timber.d("All Zones: $zones")
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

    /** Returns signed angle difference (-180 to +180) */
    private fun angleDelta(current: Float, saved: Float): Float {
        var delta = saved - current
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        return delta
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

sealed interface CreateTagDialogState {
    data object Hidden : CreateTagDialogState
    data class Visible(val yaw: Float, val pitch: Float) : CreateTagDialogState
}

data class AreaTagUiState(
    val orientation: Orientation = Orientation(),
    val location: Location? = null,
    val tagPositions: List<TagScreenPosition> = emptyList(),
    val dialog: CreateTagDialogState = CreateTagDialogState.Hidden
)

sealed interface AreaTagEvent {
    data object StartSensors : AreaTagEvent
    data object ScreenTapped : AreaTagEvent
    data object DismissCreateDialog : AreaTagEvent
    data class CreateTag(val title: String, val description: String) : AreaTagEvent
    data class DeleteTag(val tagId: String) : AreaTagEvent
}
