package com.app.research.artagging

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.research.artagging.data.ArTag
import com.app.research.artagging.data.ArTagStore
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.google.ar.core.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class ArTaggingUiState(
    val trackingMessage: String = "Scanning environment...",
    val isTracking: Boolean = false,
    val isHostingAnchor: Boolean = false,
    val isResolvingAnchors: Boolean = false,
    val showCreateDialog: Boolean = false,
    val showTagDetail: ArTag? = null,
    val pendingAnchor: Anchor? = null,
    val nearbyTags: List<ArTag> = emptyList(),
    val resolvedAnchors: Map<String, Anchor> = emptyMap(),
    val errorMessage: String? = null,
    val currentLat: Double = 0.0,
    val currentLon: Double = 0.0
)

class ArTaggingViewModel(application: Application) : AndroidViewModel(application) {

    private val tagStore = ArTagStore(application)
    private val fusedClient = LocationServices.getFusedLocationProviderClient(application)

    private val _uiState = MutableStateFlow(ArTaggingUiState())
    val uiState: StateFlow<ArTaggingUiState> = _uiState.asStateFlow()

    companion object {
        private const val NEARBY_RADIUS_METERS = 50.0
        private const val CLOUD_ANCHOR_TTL_DAYS = 365
    }

    init {
        fetchLocation()
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        fusedClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { loc: Location? ->
            loc?.let {
                _uiState.update { state ->
                    state.copy(currentLat = loc.latitude, currentLon = loc.longitude)
                }
                loadNearbyTags(loc.latitude, loc.longitude)
            }
        }
    }

    private fun loadNearbyTags(lat: Double, lon: Double) {
        val allTags = tagStore.loadAll()
        val nearby = allTags.filter { tag ->
            haversineDistance(lat, lon, tag.latitude, tag.longitude) <= NEARBY_RADIUS_METERS
        }
        _uiState.update { it.copy(nearbyTags = nearby) }
    }

    fun onTrackingStateChanged(isTracking: Boolean, message: String) {
        _uiState.update {
            it.copy(isTracking = isTracking, trackingMessage = message)
        }
    }

    // --- Tap-to-place flow ---

    /** Save the pending anchor + compute bearing from camera pose */
    fun onAnchorCreated(anchor: Anchor, cameraPose: Pose?) {
        // Compute bearing: direction from camera to anchor in world space
        var bearing = 0f
        var distance = 2f
        if (cameraPose != null) {
            val anchorPos = anchor.pose.translation  // [x, y, z]
            val cameraPos = cameraPose.translation
            val dx = anchorPos[0] - cameraPos[0]
            val dz = anchorPos[2] - cameraPos[2]
            bearing = (Math.toDegrees(atan2(dx.toDouble(), -dz.toDouble())).toFloat() + 360f) % 360f
            distance = sqrt((dx * dx + dz * dz).toDouble()).toFloat().coerceIn(0.5f, 20f)
        }
        _uiState.update {
            it.copy(pendingAnchor = anchor, showCreateDialog = true)
        }
        // Store temporarily for confirmTag
        pendingBearing = bearing
        pendingDistance = distance
    }

    private var pendingBearing = 0f
    private var pendingDistance = 2f

    fun dismissCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false, pendingAnchor = null) }
    }

    fun confirmTag(title: String, description: String, session: Session) {
        val anchor = _uiState.value.pendingAnchor ?: return
        _uiState.update { it.copy(showCreateDialog = false, isHostingAnchor = true) }

        val bearing = pendingBearing
        val distance = pendingDistance

        viewModelScope.launch {
            hostCloudAnchor(session, anchor, title, description, bearing, distance)
        }
    }

    private fun hostCloudAnchor(
        session: Session,
        anchor: Anchor,
        title: String,
        description: String,
        bearing: Float,
        distance: Float
    ) {
        // Helper to create the tag with bearing/distance
        fun makeTag(cloudId: String = "") = ArTag(
            cloudAnchorId = cloudId,
            latitude = _uiState.value.currentLat,
            longitude = _uiState.value.currentLon,
            title = title,
            description = description,
            bearing = bearing,
            distance = distance
        )

        try {
            session.hostCloudAnchorAsync(anchor, CLOUD_ANCHOR_TTL_DAYS) { cloudAnchorId, cloudState ->
                if (cloudState == Anchor.CloudAnchorState.SUCCESS && cloudAnchorId != null) {
                    val tag = makeTag(cloudId = cloudAnchorId)
                    tagStore.save(tag)
                    _uiState.update {
                        it.copy(
                            isHostingAnchor = false,
                            pendingAnchor = null,
                            nearbyTags = it.nearbyTags + tag,
                            resolvedAnchors = it.resolvedAnchors + (tag.id to anchor)
                        )
                    }
                } else {
                    val tag = makeTag()
                    tagStore.save(tag)
                    _uiState.update {
                        it.copy(
                            isHostingAnchor = false,
                            pendingAnchor = null,
                            nearbyTags = it.nearbyTags + tag,
                            resolvedAnchors = it.resolvedAnchors + (tag.id to anchor),
                            errorMessage = "Cloud anchor failed (${cloudState.name}). Tag saved locally."
                        )
                    }
                }
            }
        } catch (e: Exception) {
            val tag = makeTag()
            tagStore.save(tag)
            _uiState.update {
                it.copy(
                    isHostingAnchor = false,
                    pendingAnchor = null,
                    nearbyTags = it.nearbyTags + tag,
                    resolvedAnchors = it.resolvedAnchors + (tag.id to anchor),
                    errorMessage = "Cloud Anchors not configured. Tag saved locally."
                )
            }
        }
    }

    // --- Revisit: resolve cloud anchors + place local tags ---

    fun resolveNearbyAnchors(session: Session) {
        val alreadyRendered = _uiState.value.resolvedAnchors.keys

        // 1) Cloud anchor tags → resolve via ARCore
        val cloudTags = _uiState.value.nearbyTags
            .filter { it.cloudAnchorId.isNotBlank() && it.id !in alreadyRendered }

        // 2) Local-only tags → place using bearing + distance from camera
        val localTags = _uiState.value.nearbyTags
            .filter { it.cloudAnchorId.isBlank() && it.id !in alreadyRendered }

        val hasWork = cloudTags.isNotEmpty() || localTags.isNotEmpty()
        if (!hasWork) return

        _uiState.update { it.copy(isResolvingAnchors = true) }

        // Place local tags using GPS bearing relative to the camera
        placeLocalTags(session, localTags)

        // Resolve cloud tags
        if (cloudTags.isEmpty()) {
            _uiState.update { it.copy(isResolvingAnchors = false) }
            return
        }
        var remaining = cloudTags.size
        cloudTags.forEach { tag ->
            try {
                session.resolveCloudAnchorAsync(tag.cloudAnchorId) { resolvedAnchor, cloudState ->
                    remaining--
                    if (cloudState == Anchor.CloudAnchorState.SUCCESS && resolvedAnchor != null) {
                        _uiState.update {
                            it.copy(
                                resolvedAnchors = it.resolvedAnchors + (tag.id to resolvedAnchor),
                                isResolvingAnchors = remaining > 0
                            )
                        }
                    } else {
                        // Cloud resolve failed — fall back to local placement
                        placeLocalTags(session, listOf(tag))
                        _uiState.update {
                            it.copy(isResolvingAnchors = remaining > 0)
                        }
                    }
                }
            } catch (e: Exception) {
                remaining--
                placeLocalTags(session, listOf(tag))
                _uiState.update { it.copy(isResolvingAnchors = remaining > 0) }
            }
        }
    }

    /**
     * For tags without cloud anchors: create an anchor in front of the camera
     * offset by the saved bearing and distance.
     *
     * Uses the camera's current pose and rotates the offset by the saved bearing
     * relative to device north (the bearing the user was facing when they placed it).
     */
    private fun placeLocalTags(session: Session, tags: List<ArTag>) {
        val frame = try { session.update() } catch (_: Exception) { return }
        val cameraPose = frame.camera.pose

        tags.forEach { tag ->
            if (tag.id in _uiState.value.resolvedAnchors) return@forEach

            // Convert bearing to radians — bearing is clockwise from north,
            // ARCore Z is negative forward so we negate
            val bearingRad = Math.toRadians(tag.bearing.toDouble())
            val dist = tag.distance.coerceIn(0.5f, 15f)

            // Offset in world-space (X = east, Z = -north in ARCore)
            val offsetX = (sin(bearingRad) * dist).toFloat()
            val offsetZ = (-cos(bearingRad) * dist).toFloat()

            val anchorPose = Pose.makeTranslation(
                cameraPose.tx() + offsetX,
                cameraPose.ty() - 0.5f,  // slightly below eye level
                cameraPose.tz() + offsetZ
            )

            try {
                val anchor = session.createAnchor(anchorPose)
                _uiState.update {
                    it.copy(resolvedAnchors = it.resolvedAnchors + (tag.id to anchor))
                }
            } catch (_: Exception) {
                // Session may not be ready
            }
        }
    }

    // --- Tag detail ---

    fun showTagDetail(tag: ArTag) {
        _uiState.update { it.copy(showTagDetail = tag) }
    }

    fun dismissTagDetail() {
        _uiState.update { it.copy(showTagDetail = null) }
    }

    fun deleteTag(tagId: String) {
        tagStore.delete(tagId)
        _uiState.value.resolvedAnchors[tagId]?.detach()
        _uiState.update {
            it.copy(
                nearbyTags = it.nearbyTags.filter { t -> t.id != tagId },
                resolvedAnchors = it.resolvedAnchors - tagId,
                showTagDetail = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}
