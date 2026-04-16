package com.app.research.areatag.ui

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.research.R
import com.app.research.areatag.data.Zone
import com.app.research.data.TempDataSource
import com.app.research.singlescreen_r_d.skaifitness.VStack
import com.app.research.ui.isPreviewMode
import com.app.research.ui.theme.black
import com.app.research.ui.theme.blackA25
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlin.math.roundToInt

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AreaTagScreen(
    viewModel: AreaTagViewModel = viewModel()
) {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    if (permissionsState.allPermissionsGranted) {
        val uiState by viewModel.uiState.collectAsState()
        AreaTagContent(
            uiState = uiState, onEvent = viewModel::onEvent
        )
    } else {
        PermissionRequest(
            onRequestPermissions = { permissionsState.launchMultiplePermissionRequest() })
    }
}

@Composable
private fun PermissionRequest(onRequestPermissions: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "AreaTag needs Camera & Location",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onRequestPermissions,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Text("Grant Permissions", color = Color.Black)
            }
        }
    }
}

@Composable
private fun AreaTagContent(
    uiState: AreaTagUiState,
    onEvent: (AreaTagEvent) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val orientation = uiState.orientation
    val location = uiState.location
    val tagPositions = uiState.tagPositions

    DisposableEffect(Unit) {
        onEvent(AreaTagEvent.StartSensors)
        onDispose { }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        if (!isPreviewMode) {

            // CameraX Preview
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview
                            )
                        } catch (_: Exception) {
                        }
                    }, androidx.core.content.ContextCompat.getMainExecutor(context))

                    previewView
                }, modifier = Modifier.fillMaxSize()
            )
        }


        // AR Overlay — tags, crosshair, arrows
        AROverlay(
            tagPositions = tagPositions,
            zoneArrow = uiState.zoneArrow,
            modifier = Modifier.fillMaxSize()
        )

        // Tap target (invisible, captures taps)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) {
                    onEvent(AreaTagEvent.ScreenTapped)
                })

        VStack(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
        ) {

            // HUD: sensor info
            HUD(
                yaw = orientation.yaw,
                pitch = orientation.pitch,
                lat = location?.latitude,
                lon = location?.longitude,
                modifier = Modifier
            )

            StabilityIndicator(
                stability = uiState.stability,
                modifier = Modifier
            )
        }
        // Location accuracy indicator

        VStack(
            spaceBy = 8.dp,
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 16.dp)
                .statusBarsPadding()
        ) {
            AccuracyIndicator(
                accuracyMeters = if (location?.hasAccuracy() == true) location.accuracy else null,
                modifier = Modifier

            )

            ZoneDropDown(
                zones = uiState.zones,
                selectedZoneId = uiState.selectedZoneId,
                onSelect = { onEvent(AreaTagEvent.SelectZone(it)) },
                modifier = Modifier
            )
        }
        // Tag count badge
        if (tagPositions.isNotEmpty()) {
            Text(
                text = pluralStringResource(
                    R.plurals.tag_count_nearby,
                    tagPositions.size,
                    tagPositions.size
                ),
                color = Color(0xFF00E5FF),
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .background(Color(0x88000000), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }

    // Create tag dialog
    val dialog = uiState.dialog
    if (dialog is CreateTagDialogState.Visible) {
        CreateTagDialog(
            isNewZone = dialog.isNewZone,
            onDismiss = { onEvent(AreaTagEvent.DismissCreateDialog) },
            onCreate = { tagTitle, tagDesc, zoneTitle, zoneDesc ->
                onEvent(AreaTagEvent.CreateTag(tagTitle, tagDesc, zoneTitle, zoneDesc))
            }
        )
    }
}

@Composable
private fun HUD(
    yaw: Float, pitch: Float, lat: Double?, lon: Double?, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = blackA25, shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Text(
            "Yaw: ${yaw.roundToInt()}°  Pitch: ${pitch.roundToInt()}°",
            color = Color.White,
            fontSize = 11.sp
        )
        if (lat != null && lon != null) {
            Text(
                "GPS: ${"%.5f".format(lat)}, ${"%.5f".format(lon)}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        } else {
            Text("Acquiring GPS...", color = Color.Yellow, fontSize = 10.sp)
        }
    }
}

private enum class AccuracyLevel(val label: String, val color: Color) {
    VeryHigh("Very High", Color(0xFF00FF81)), High("High", Color(0xFF4CAF50)), Medium(
        "Medium",
        Color(0xFFFFC107)
    ),
    Low("Low", Color(0xB3FF5252)), VeryLow("Very Low", Color(0xFFFF5252)), Unknown(
        "--",
        Color(0xFFBDBDBD)
    );

    companion object {
        fun from(accuracyMeters: Float?): AccuracyLevel = when {
            accuracyMeters == null -> Unknown
            accuracyMeters < 5f -> VeryHigh
            accuracyMeters < 10f -> High
            accuracyMeters < 20f -> Medium
            accuracyMeters < 30f -> Low
            else -> VeryLow
        }
    }
}

@Composable
private fun StabilityIndicator(
    stability: ZoneStabilityState,
    modifier: Modifier = Modifier
) {
    val isStable = stability.isStable
    val color = if (isStable) Color(0xFF00FF81) else Color(0xFFFFC107)
    val label = if (isStable) {
        "Stable"
    } else {
        "Unstable ${stability.consecutiveCount}/${stability.threshold}"
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color = blackA25, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AccuracyIndicator(
    accuracyMeters: Float?, modifier: Modifier = Modifier
) {
    val level = AccuracyLevel.from(accuracyMeters)
    val valueText = if (accuracyMeters == null) level.label
    else "${"%.1f".format(accuracyMeters)}m | ${level.label}"

    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = modifier
            .background(
                color = blackA25, shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$valueText Accuracy",
            color = level.color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneDropDown(
    zones: List<Zone>,
    selectedZoneId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val selectedZone = zones.firstOrNull { it.zoneId == selectedZoneId }
    val label = when {
        zones.isEmpty() -> "No Zones"
        selectedZone != null -> selectedZone.title.ifBlank { shortZoneId(selectedZone.zoneId) }
        else -> "Select Zone"
    }

    Box(modifier = modifier) {
        VStack(8.dp) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .background(color = blackA25, shape = RoundedCornerShape(8.dp))
                    .clickable(enabled = zones.isNotEmpty()) { isExpanded = !isExpanded }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                if (selectedZone != null) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(selectedZone.color), CircleShape)
                    )
                }
                Text(text = label, color = Color.White, fontSize = 12.sp)
            }

            DropdownMenu(
                shape = RoundedCornerShape(8.dp),
                containerColor = black,
                expanded = isExpanded && zones.isNotEmpty(),
                onDismissRequest = { isExpanded = false }
            ) {
                zones.forEach { zone ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(zone.color), CircleShape)
                                )

                                Column {
                                    Text(
                                        text = zone.title.ifBlank { shortZoneId(zone.zoneId) },
                                        color = Color.White,
                                        lineHeight = 1.em,
                                        fontSize = 13.sp,
                                        fontWeight = if (zone.zoneId == selectedZoneId) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = pluralStringResource(
                                            R.plurals.tag_count,
                                            zone.tags.size,
                                            zone.tags.size
                                        ),
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        },
                        onClick = {
                            onSelect(zone.zoneId)
                            isExpanded = false
                        }
                    )
                }
            }
        }
    }
}

private fun shortZoneId(id: String): String =
    if (id.length <= 8) id else "${id.take(4)}…${id.takeLast(4)}"

@Composable
private fun CreateTagDialog(
    isNewZone: Boolean,
    onDismiss: () -> Unit,
    onCreate: (title: String, description: String, zoneTitle: String?, zoneDescription: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var zoneTitle by remember { mutableStateOf("") }
    var zoneDescription by remember { mutableStateOf("") }

    val canSubmit = title.isNotBlank() && (!isNewZone || zoneTitle.isNotBlank())

    AlertDialog(onDismissRequest = onDismiss, containerColor = Color(0xFF1E1E2E), title = {
        Text(
            if (isNewZone) "New Zone & First Tag" else "Place Tag Here",
            color = Color.White
        )
    }, text = {
        Column {
            if (isNewZone) {
                Text(
                    "You're in a new zone. Name it before adding tags.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = zoneTitle,
                    onValueChange = { zoneTitle = it },
                    label = { Text("Zone Name", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        cursorColor = Color(0xFF00E5FF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = zoneDescription,
                    onValueChange = { zoneDescription = it },
                    label = { Text("Zone Description (optional)", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        cursorColor = Color(0xFF00E5FF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tag Title", color = Color.Gray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF00E5FF),
                    cursorColor = Color(0xFF00E5FF)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Tag Description (optional)", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF00E5FF),
                    cursorColor = Color(0xFF00E5FF)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }, confirmButton = {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
            Button(
                onClick = {
                    if (canSubmit) onCreate(
                        title.trim(),
                        description.trim(),
                        if (isNewZone) zoneTitle.trim() else null,
                        if (isNewZone) zoneDescription.trim() else null
                    )
                },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Text(if (isNewZone) "Create Zone" else "Place", color = Color.Black)
            }
        }
    })
}


@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun AreaTagPreview() {
    AreaTagContent(
        uiState = TempDataSource.sampleAreaTagUiState, onEvent = {})
}