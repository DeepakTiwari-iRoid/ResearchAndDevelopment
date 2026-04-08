package com.app.research.artagging.ui

import android.Manifest
import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.research.artagging.ArTaggingViewModel
import com.app.research.artagging.data.ArTag
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.node.CubeNode

private val AccentColor = Color(0xFF00E5FF)
private val DarkBg = Color(0xFF1E1E2E)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ArTaggingScreen(
    viewModel: ArTaggingViewModel = viewModel()
) {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    if (permissionsState.allPermissionsGranted) {
        ArTaggingContent(viewModel = viewModel)
    } else {
        PermissionRequest(onRequest = { permissionsState.launchMultiplePermissionRequest() })
    }
}

@Composable
private fun PermissionRequest(onRequest: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("AR Tagging needs Camera & Location", color = Color.White, fontSize = 18.sp)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
            ) { Text("Grant Permissions", color = Color.Black) }
        }
    }
}

@Composable
private fun ArTaggingContent(viewModel: ArTaggingViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var arSceneViewRef by remember { mutableStateOf<ARSceneView?>(null) }
    // Track which tag IDs already have 3D markers in the scene
    val renderedTagIds = remember { mutableSetOf<String>() }

    Box(Modifier.fillMaxSize()) {

        // --- AR Scene View ---
        AndroidView(
            factory = { context ->
                ARSceneView(context).apply {
                    // Configure the AR session
                    configureSession { session, config ->
                        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                        config.depthMode =
                            if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC))
                                Config.DepthMode.AUTOMATIC
                            else Config.DepthMode.DISABLED
                        config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
                        config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                    }

                    // Track session state changes
                    onSessionUpdated = { _, frame ->
                        handleTrackingState(frame, viewModel)
                    }

                    // Tap to place — use SceneView's onTouchEvent, then do AR hit test
                    onTouchEvent = { motionEvent, _ ->
                        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                            // Defaults are empty — must explicitly enable all surface types
                            val hitResult = hitTestAR(
                                xPx = motionEvent.x,
                                yPx = motionEvent.y,
                                planeTypes = setOf(
                                    Plane.Type.HORIZONTAL_UPWARD_FACING,
                                    Plane.Type.HORIZONTAL_DOWNWARD_FACING,
                                    Plane.Type.VERTICAL
                                ),
                                point = true,
                                depthPoint = true,
                                instantPlacementPoint = false
                            )
                            if (hitResult != null) {
                                onArTap(hitResult, this, viewModel)
                                true
                            } else false
                        } else false
                    }

                    arSceneViewRef = this

                    // Resolve existing nearby anchors after session warm-up
                    postDelayed({
                        session?.let { viewModel.resolveNearbyAnchors(it) }
                    }, 3000)
                }
            },
            // Recompose when resolvedAnchors changes → create markers for newly resolved tags
            update = { arSceneView ->
                val resolved = viewModel.uiState.value.resolvedAnchors
                val tags = viewModel.uiState.value.nearbyTags.associateBy { it.id }
                resolved.forEach { (tagId, anchor) ->
                    if (tagId !in renderedTagIds) {
                        renderedTagIds.add(tagId)
                        createMarkerAtAnchor(arSceneView, anchor, tags[tagId]?.title)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // --- Crosshair ---
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CrosshairIndicator()
        }

        // --- Top status bar ---
        StatusBar(
            message = uiState.trackingMessage,
            isTracking = uiState.isTracking,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)
        )

        // --- Loading overlay for hosting/resolving ---
        AnimatedVisibility(
            visible = uiState.isHostingAnchor || uiState.isResolvingAnchors,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            LoadingOverlay(
                message = if (uiState.isHostingAnchor) "Hosting anchor..." else "Resolving tags..."
            )
        }

        // --- Bottom tag count ---
        if (uiState.nearbyTags.isNotEmpty()) {
            Text(
                "${uiState.nearbyTags.size} tag(s) nearby",
                color = AccentColor,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .background(Color(0x88000000), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        // --- Error snackbar ---
        uiState.errorMessage?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK", color = AccentColor)
                    }
                },
                containerColor = Color(0xFF2D2D3D)
            ) {
                Text(msg, color = Color.White, fontSize = 12.sp)
            }
        }
    }

    // --- Dialogs ---
    if (uiState.showCreateDialog) {
        CreateTagDialog(
            onDismiss = { viewModel.dismissCreateDialog() },
            onCreate = { title, desc ->
                arSceneViewRef?.session?.let { session ->
                    viewModel.confirmTag(title, desc, session)
                }
            }
        )
    }

    uiState.showTagDetail?.let { tag ->
        TagDetailDialog(
            tag = tag,
            onDismiss = { viewModel.dismissTagDetail() },
            onDelete = { viewModel.deleteTag(tag.id) }
        )
    }
}

// --- AR logic ---

private fun handleTrackingState(frame: Frame, viewModel: ArTaggingViewModel) {
    val camera = frame.camera
    when (camera.trackingState) {
        TrackingState.TRACKING -> {
            viewModel.onTrackingStateChanged(true, "Ready — tap a surface to place a tag")
        }

        TrackingState.PAUSED -> {
            val reason = when (camera.trackingFailureReason) {
                TrackingFailureReason.NONE -> "Initializing..."
                TrackingFailureReason.BAD_STATE -> "AR in bad state. Try restarting."
                TrackingFailureReason.INSUFFICIENT_LIGHT -> "Too dark — move to a brighter area"
                TrackingFailureReason.EXCESSIVE_MOTION -> "Moving too fast — slow down"
                TrackingFailureReason.INSUFFICIENT_FEATURES -> "Not enough detail — point at a textured surface"
                TrackingFailureReason.CAMERA_UNAVAILABLE -> "Camera unavailable"
            }
            viewModel.onTrackingStateChanged(false, reason)
        }

        TrackingState.STOPPED -> {
            viewModel.onTrackingStateChanged(false, "Tracking stopped")
        }
    }
}

private fun onArTap(
    hitResult: com.google.ar.core.HitResult,
    arSceneView: ARSceneView,
    viewModel: ArTaggingViewModel
) {
    if (!viewModel.uiState.value.isTracking) return

    val trackable = hitResult.trackable

    // Accept planes, point cloud hits, and depth points
    val isValidHit = when {
        trackable is Plane && trackable.isPoseInPolygon(hitResult.hitPose) -> true
        trackable.trackingState == TrackingState.TRACKING -> true // DepthPoint, Point
        else -> false
    }

    if (!isValidHit) return

    val anchor = hitResult.createAnchor()
    createMarkerAtAnchor(arSceneView, anchor)
    viewModel.onAnchorCreated(anchor, arSceneView.frame?.camera?.pose)
}

/** Creates the 3D marker (cube + pole) at the given anchor and adds it to the scene. */
private fun createMarkerAtAnchor(
    arSceneView: ARSceneView,
    anchor: com.google.ar.core.Anchor,
    @Suppress("unused") label: String? = null
) {
    val engine = arSceneView.engine

    val anchorNode = AnchorNode(engine = engine, anchor = anchor)

    // Marker cube
    val markerNode = CubeNode(
        engine = engine,
        size = Float3(0.04f, 0.04f, 0.04f),
        center = Float3(0f, 0.02f, 0f),
        materialInstance = arSceneView.materialLoader.createColorInstance(
            color = android.graphics.Color.argb(230, 0, 229, 255)
        )
    )

    // Vertical pole for visual grounding
    val poleNode = CubeNode(
        engine = engine,
        size = Float3(0.005f, 0.04f, 0.005f),
        center = Float3(0f, 0f, 0f),
        materialInstance = arSceneView.materialLoader.createColorInstance(
            color = android.graphics.Color.argb(128, 0, 229, 255)
        )
    )

    anchorNode.addChildNode(poleNode)
    anchorNode.addChildNode(markerNode)
    arSceneView.addChildNode(anchorNode)
}

// --- UI Components ---

@Composable
private fun CrosshairIndicator() {
    Box(Modifier.size(40.dp)) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f), CircleShape)
        )
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(AccentColor)
                .align(Alignment.Center)
        )
    }
}

@Composable
private fun StatusBar(
    message: String,
    isTracking: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0xAA000000), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isTracking) Color(0xFF00E676) else Color(0xFFFF5252))
        )
        Spacer(Modifier.width(8.dp))
        Text(message, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
private fun LoadingOverlay(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color(0xCC000000), RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        CircularProgressIndicator(color = AccentColor, strokeWidth = 3.dp)
        Spacer(Modifier.height(12.dp))
        Text(message, color = Color.White, fontSize = 14.sp)
    }
}

@Composable
private fun CreateTagDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, description: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkBg,
        title = { Text("Place AR Tag", color = Color.White) },
        text = {
            Column {
                Text(
                    "Tag will be anchored to this surface",
                    color = Color.Gray, fontSize = 12.sp
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title", color = Color.Gray) },
                    singleLine = true,
                    colors = arTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)", color = Color.Gray) },
                    colors = arTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
                }
                Button(
                    onClick = { if (title.isNotBlank()) onCreate(title.trim(), description.trim()) },
                    enabled = title.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                ) { Text("Place", color = Color.Black) }
            }
        }
    )
}

@Composable
private fun TagDetailDialog(
    tag: ArTag,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkBg,
        title = {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(tag.title, color = Color.White, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.Gray)
                }
            }
        },
        text = {
            Column {
                if (tag.description.isNotBlank()) {
                    Text(tag.description, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    "GPS: ${"%.5f".format(tag.latitude)}, ${"%.5f".format(tag.longitude)}",
                    color = Color.Gray, fontSize = 11.sp
                )
                if (tag.cloudAnchorId.isNotBlank()) {
                    Text(
                        "Cloud Anchor: ${tag.cloudAnchorId.take(16)}...",
                        color = Color.Gray, fontSize = 11.sp
                    )
                } else {
                    Text("Local anchor only", color = Color(0xFFFF9800), fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
            ) {
                Icon(Icons.Default.Delete, "Delete", Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Delete Tag")
            }
        }
    )
}

@Composable
private fun arTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = AccentColor,
    cursorColor = AccentColor
)
