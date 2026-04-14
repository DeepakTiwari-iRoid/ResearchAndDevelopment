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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.research.data.TempDataSource
import com.app.research.ui.isPreviewMode
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
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    if (permissionsState.allPermissionsGranted) {
        val uiState by viewModel.uiState.collectAsState()
        AreaTagContent(
            uiState = uiState,
            onEvent = viewModel::onEvent
        )
    } else {
        PermissionRequest(
            onRequestPermissions = { permissionsState.launchMultiplePermissionRequest() }
        )
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
            .background(Color.Black)
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
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview
                            )
                        } catch (_: Exception) {
                        }
                    }, androidx.core.content.ContextCompat.getMainExecutor(context))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }


        // AR Overlay — tags, crosshair, arrows
        AROverlay(
            tagPositions = tagPositions,
            modifier = Modifier.fillMaxSize()
        )

        // Tap target (invisible, captures taps)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onEvent(AreaTagEvent.ScreenTapped)
                }
        )

        // HUD: sensor info
        HUD(
            yaw = orientation.yaw,
            pitch = orientation.pitch,
            lat = location?.latitude,
            lon = location?.longitude,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
        )

        // Location accuracy indicator
        AccuracyIndicator(
            accuracyMeters = if (location?.hasAccuracy() == true) location.accuracy else null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
        )

        // Tag count badge
        if (tagPositions.isNotEmpty()) {
            Text(
                text = "${tagPositions.size} tag(s) nearby",
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
    if (uiState.dialog is CreateTagDialogState.Visible) {
        CreateTagDialog(
            onDismiss = { onEvent(AreaTagEvent.DismissCreateDialog) },
            onCreate = { title, desc -> onEvent(AreaTagEvent.CreateTag(title, desc)) }
        )
    }
}

@Composable
private fun HUD(
    yaw: Float,
    pitch: Float,
    lat: Double?,
    lon: Double?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = Color(0x40000000),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Text(
            "Yaw: ${yaw.roundToInt()}°  Pitch: ${pitch.roundToInt()}°",
            color = Color.White, fontSize = 11.sp
        )
        if (lat != null && lon != null) {
            Text(
                "GPS: ${"%.5f".format(lat)}, ${"%.5f".format(lon)}",
                color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp
            )
        } else {
            Text("Acquiring GPS...", color = Color.Yellow, fontSize = 10.sp)
        }
    }
}

private enum class AccuracyLevel(val label: String, val color: Color) {
    VeryHigh("Very High", Color(0xFF69F0AE)),
    High("High", Color(0xFF4CAF50)),
    Medium("Medium", Color(0xFFFFC107)),
    Low("Low", Color(0xB3FF5252)),
    VeryLow("Very Low", Color(0xFFFF5252)),
    Unknown("--", Color(0xFFBDBDBD));

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
private fun AccuracyIndicator(
    accuracyMeters: Float?,
    modifier: Modifier = Modifier
) {
    val level = AccuracyLevel.from(accuracyMeters)
    val valueText = if (accuracyMeters == null) level.label
    else "${"%.1f".format(accuracyMeters)}m | ${level.label}"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(Color(0x88000000), RoundedCornerShape(8.dp))
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

@Composable
private fun CreateTagDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, description: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2E),
        title = {
            Text("Place Tag Here", color = Color.White)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title", color = Color.Gray) },
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
                    label = { Text("Description (optional)", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        cursorColor = Color(0xFF00E5FF)
                    ),
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
                    onClick = {
                        if (title.isNotBlank()) onCreate(title.trim(), description.trim())
                    },
                    enabled = title.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Text("Place", color = Color.Black)
                }
            }
        }
    )
}


@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun AreaTagPreview() {
    AreaTagContent(
        uiState = TempDataSource.sampleAreaTagUiState,
        onEvent = {}
    )
}
