package com.app.research.skyview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.research.skyview.data.SkyTag
import com.app.research.skyview.data.SkyTagStore
import com.app.research.skyview.location.GeoUtils
import com.app.research.skyview.location.GpsLocation
import com.app.research.skyview.location.LocationProvider
import com.app.research.skyview.sensor.Orientation
import com.app.research.skyview.sensor.OrientationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TagScreenPosition(
    val tag: SkyTag,
    val deltaYaw: Float,
    val deltaPitch: Float,
    val distanceMeters: Double,
    val isVisible: Boolean
)

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
    val orientation: StateFlow<Orientation> = _orientation.asStateFlow()

    private val _location = MutableStateFlow<GpsLocation?>(null)
    val location: StateFlow<GpsLocation?> = _location.asStateFlow()

    private val _allTags = MutableStateFlow<List<SkyTag>>(emptyList())

    val nearbyTagPositions: StateFlow<List<TagScreenPosition>> = combine(
        _allTags, _orientation, _location
    ) { tags, orient, loc ->
        if (loc == null) return@combine emptyList()

        tags.filter { tag ->
            GeoUtils.distanceMeters(loc.latitude, loc.longitude, tag.latitude, tag.longitude) <= NEARBY_RADIUS_METERS
        }.map { tag ->
            val dist = GeoUtils.distanceMeters(loc.latitude, loc.longitude, tag.latitude, tag.longitude)
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()

    private val _tapYaw = MutableStateFlow(0f)
    val tapYaw: StateFlow<Float> = _tapYaw.asStateFlow()

    private val _tapPitch = MutableStateFlow(0f)
    val tapPitch: StateFlow<Float> = _tapPitch.asStateFlow()

    fun startSensors() {
        viewModelScope.launch {
            orientationManager.observeOrientation().collect { _orientation.value = it }
        }
        viewModelScope.launch {
            locationProvider.observeLocation().collect { _location.value = it }
        }
        reloadTags()
    }

    fun onScreenTap() {
        _tapYaw.value = _orientation.value.yaw
        _tapPitch.value = _orientation.value.pitch
        _showCreateDialog.value = true
    }

    fun dismissCreateDialog() {
        _showCreateDialog.value = false
    }

    fun createTag(title: String, description: String = "") {
        val loc = _location.value ?: return
        val tag = SkyTag(
            latitude = loc.latitude,
            longitude = loc.longitude,
            yaw = _tapYaw.value,
            pitch = _tapPitch.value,
            title = title,
            description = description
        )
        viewModelScope.launch {
            tagStore.save(tag)
            reloadTags()
        }
        _showCreateDialog.value = false
    }

    fun deleteTag(tagId: String) {
        viewModelScope.launch {
            tagStore.delete(tagId)
            reloadTags()
        }
    }

    private fun reloadTags() {
        _allTags.value = tagStore.loadAll()
    }

    /** Returns signed angle difference (-180 to +180) */
    private fun angleDelta(current: Float, saved: Float): Float {
        var delta = saved - current
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        return delta
    }
}
