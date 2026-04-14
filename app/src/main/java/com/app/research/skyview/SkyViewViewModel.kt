package com.app.research.skyview

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.research.skyview.data.SkyTag
import com.app.research.skyview.data.SkyTagStore
import com.app.research.skyview.location.GeoUtils
import com.app.research.skyview.location.LocationProvider
import com.app.research.skyview.sensor.Orientation
import com.app.research.skyview.sensor.OrientationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

data class TagScreenPosition(
    val tag: SkyTag,
    val deltaYaw: Float,
    val deltaPitch: Float,
    val distanceMeters: Double,
    val isVisible: Boolean
)

sealed interface CreateTagDialogState {
    data object Hidden : CreateTagDialogState
    data class Visible(val yaw: Float, val pitch: Float) : CreateTagDialogState
}

data class SkyViewUiState(
    val orientation: Orientation = Orientation(),
    val location: Location? = null,
    val tagPositions: List<TagScreenPosition> = emptyList(),
    val dialog: CreateTagDialogState = CreateTagDialogState.Hidden
)

sealed interface SkyViewEvent {
    data object StartSensors : SkyViewEvent
    data object ScreenTapped : SkyViewEvent
    data object DismissCreateDialog : SkyViewEvent
    data class CreateTag(val title: String, val description: String) : SkyViewEvent
    data class DeleteTag(val tagId: String) : SkyViewEvent
}

class SkyViewViewModel(application: Application) : AndroidViewModel(application) {

    private val orientationManager = OrientationManager(application)
    private val locationProvider = LocationProvider(application)
    private val tagStore = SkyTagStore(application)

    companion object {
        const val NEARBY_RADIUS_METERS = 50.0
        const val VISIBILITY_YAW_THRESHOLD = 30f
        const val VISIBILITY_PITCH_THRESHOLD = 25f
    }

    private val _orientation = MutableStateFlow(Orientation())
    private val _location = MutableStateFlow<Location?>(null)
    private val _allTags = MutableStateFlow<List<SkyTag>>(emptyList())
    private val _dialog = MutableStateFlow<CreateTagDialogState>(CreateTagDialogState.Hidden)

    val uiState: StateFlow<SkyViewUiState> = combine(
        _orientation, _location, _allTags, _dialog
    ) { orient, loc, tags, dialog ->
        SkyViewUiState(
            orientation = orient,
            location = loc,
            tagPositions = computeTagPositions(tags, orient, loc),
            dialog = dialog
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SkyViewUiState())

    private var sensorsStarted = false

    fun onEvent(event: SkyViewEvent) {
        when (event) {
            SkyViewEvent.StartSensors -> startSensors()
            SkyViewEvent.ScreenTapped -> openCreateDialog()
            SkyViewEvent.DismissCreateDialog -> _dialog.value = CreateTagDialogState.Hidden
            is SkyViewEvent.CreateTag -> createTag(event.title, event.description)
            is SkyViewEvent.DeleteTag -> deleteTag(event.tagId)
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
                Timber.d("Location updated: $it")
            }
        }
        reloadTags()
    }

    private fun openCreateDialog() {
        val current = _orientation.value
        _dialog.value = CreateTagDialogState.Visible(yaw = current.yaw, pitch = current.pitch)
    }

    private fun createTag(title: String, description: String) {
        val loc = _location.value ?: return
        val snapshot = _dialog.value as? CreateTagDialogState.Visible ?: return
        val tag = SkyTag(
            latitude = loc.latitude,
            longitude = loc.longitude,
            yaw = snapshot.yaw,
            pitch = snapshot.pitch,
            title = title,
            description = description
        )
        viewModelScope.launch {
            tagStore.save(tag)
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
        _allTags.value = tagStore.loadAll()
    }

    private fun computeTagPositions(
        tags: List<SkyTag>,
        orient: Orientation,
        loc: Location?
    ): List<TagScreenPosition> {
        if (loc == null) return emptyList()
        return tags.filter { tag ->
            GeoUtils.distanceMeters(
                loc.latitude, loc.longitude, tag.latitude, tag.longitude
            ) <= NEARBY_RADIUS_METERS
        }.map { tag ->
            val dist = GeoUtils.distanceMeters(
                loc.latitude, loc.longitude, tag.latitude, tag.longitude
            )
            val deltaYaw = angleDelta(orient.yaw, tag.yaw)
            val deltaPitch = tag.pitch - orient.pitch
            val isVisible = kotlin.math.abs(deltaYaw) <= VISIBILITY_YAW_THRESHOLD &&
                    kotlin.math.abs(deltaPitch) <= VISIBILITY_PITCH_THRESHOLD
            TagScreenPosition(
                tag = tag,
                deltaYaw = deltaYaw,
                deltaPitch = deltaPitch,
                distanceMeters = dist,
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
}