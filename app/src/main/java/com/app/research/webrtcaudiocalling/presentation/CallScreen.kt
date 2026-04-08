package com.app.research.webrtcaudiocalling.presentation

import android.Manifest
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.research.webrtcaudiocalling.domain.CallState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CallScreen(
    modifier: Modifier = Modifier,
    viewModel: CallViewModel = viewModel()
) {
    val callState by viewModel.callState.collectAsState()
    val isMicEnabled by viewModel.isMicEnabled.collectAsState()
    var roomId by remember { mutableStateOf("") }
    val audioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Push-to-Talk",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        StatusChip(callState)

        if (!audioPermission.status.isGranted) {
            Button(onClick = { audioPermission.launchPermissionRequest() }) {
                Text("Grant Audio Permission")
            }
        }

        when (callState) {
            is CallState.Idle, is CallState.Disconnected, is CallState.Error -> {
                ConnectSection(
                    roomId = roomId,
                    onRoomIdChange = { roomId = it },
                    onCreateRoom = { viewModel.connect(roomId, isInitiator = true) },
                    onJoinRoom = { viewModel.connect(roomId, isInitiator = false) },
                    enabled = audioPermission.status.isGranted && roomId.isNotBlank()
                )

                if (callState is CallState.Error) {
                    Text(
                        text = (callState as CallState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            is CallState.Connecting -> {
                Spacer(Modifier.weight(1f))
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text("Connecting...", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.weight(1f))

                OutlinedButton(onClick = { viewModel.disconnect() }) {
                    Icon(Icons.Default.CallEnd, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cancel")
                }
            }

            is CallState.Connected -> {
                Spacer(Modifier.weight(1f))

                PushToTalkButton(
                    isTalking = isMicEnabled,
                    onTalkingChanged = { viewModel.setMicEnabled(it) }
                )

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = { viewModel.disconnect() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("End Call")
                }
            }
        }
    }
}

@Composable
private fun StatusChip(state: CallState) {
    val (text, color) = when (state) {
        is CallState.Idle -> "Ready" to Color.Gray
        is CallState.Connecting -> "Connecting..." to Color(0xFFFFA726)
        is CallState.Connected -> "Connected" to Color(0xFF66BB6A)
        is CallState.Error -> "Error" to Color(0xFFE53935)
        is CallState.Disconnected -> "Disconnected" to Color.Gray
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f),
        contentColor = color
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun ConnectSection(
    roomId: String,
    onRoomIdChange: (String) -> Unit,
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        value = roomId,
        onValueChange = onRoomIdChange,
        label = { Text("Room ID") },
        placeholder = { Text("Enter a shared room name") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onCreateRoom,
            enabled = enabled,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Call, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Create Room")
        }

        OutlinedButton(
            onClick = onJoinRoom,
            enabled = enabled,
            modifier = Modifier.weight(1f)
        ) {
            Text("Join Room")
        }
    }
}

@Composable
private fun PushToTalkButton(
    isTalking: Boolean,
    onTalkingChanged: (Boolean) -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isTalking) Color(0xFFE53935) else Color(0xFF43A047),
        label = "ptt_bg"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onTalkingChanged(true)
                            tryAwaitRelease()
                            onTalkingChanged(false)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Push to Talk",
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = if (isTalking) "Speaking..." else "Hold to Speak",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = if (isTalking) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface
        )
    }
}
