import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import timber.log.Timber

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MultiPermissionHandler(
    permissions: List<String>,
    requiredPermission: List<String> = permissions,
    description: String = "This app requires permissions to function properly.",
    onGranted: (List<String>) -> Unit,
) {
    val context = LocalContext.current
    val activity = context as android.app.Activity

    var showRationale by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val permissionsState = rememberMultiplePermissionsState(permissions) { pr ->
        val isAllGranted = pr.all { it.value }
        val areRequiredPermissionsGranted = pr.filter { requiredPermission.contains(it.key) }
            .all { it.value }

        val grantedPermissions = pr.filter { it.value }.map { it.key }

        if (isAllGranted || areRequiredPermissionsGranted) {
            onGranted(grantedPermissions)
        } else {
            val shouldShowRational =
                pr.map { shouldShowRequestPermissionRationale(activity, it.key) }.any { it }

            Timber.d("Permissions result: $pr, shouldShowRational: $shouldShowRational")
            if (shouldShowRational) {
                showRationale = true
            } else {
                showSettingsDialog = true
            }
        }
    }

    // Request permissions launcher
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        permissionsState.launchMultiplePermissionRequest()
    }

    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
    }


    // 🔸 Rationale Dialog
    if (showRationale) {
        PermissionDialog(
            title = "Permission Required",
            description = description,
            confirmText = "Grant",
            onConfirm = {
                showRationale = false
                permissionsState.launchMultiplePermissionRequest()
            },
            onDismiss = { showRationale = false }
        )
    }

    // 🔸 Settings Dialog
    if (showSettingsDialog) {
        PermissionDialog(
            title = "Permissions Denied",
            description = "Some permissions are permanently denied. Please grant them in Settings.",
            confirmText = "Open Settings",
            onConfirm = {
                showSettingsDialog = false
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                )
                settingsLauncher.launch(intent)
            },
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
fun PermissionDialog(
    title: String,
    description: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = onConfirm) { Text(confirmText) }
                }
            }
        }
    }
}