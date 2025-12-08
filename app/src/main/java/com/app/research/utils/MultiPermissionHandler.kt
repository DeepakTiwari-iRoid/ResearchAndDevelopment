package com.app.research.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import timber.log.Timber

//Its for native ui

class MultiPermissionHandler(
    private val activity: Activity,
    private val permissions: List<String>,
    private val requiredPermissions: List<String> = permissions,
    private val description: String = "This app requires permissions to function properly.",
    private val onGranted: (List<String>) -> Unit
) {

    private lateinit var settingsLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    fun registerLaunchers(
        registerForActivityResult: (ActivityResultContracts.RequestMultiplePermissions, (Map<String, Boolean>) -> Unit) -> ActivityResultLauncher<Array<String>>,
        registerForSettings: (ActivityResultContracts.StartActivityForResult) -> ActivityResultLauncher<Intent>
    ) {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                handlePermissionResult(result)
            }

        settingsLauncher = registerForSettings(ActivityResultContracts.StartActivityForResult())
    }

    fun requestPermissions() {
        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun handlePermissionResult(result: Map<String, Boolean>) {
        val isAllGranted = result.all { it.value }
        val areRequiredPermissionsGranted =
            result.filter { requiredPermissions.contains(it.key) }.all { it.value }

        val grantedPermissions = result.filter { it.value }.map { it.key }

        if (isAllGranted || areRequiredPermissionsGranted) {
            onGranted(grantedPermissions)
        } else {
            val shouldShowRationale = result.keys.any {
                ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
            }

            Timber.d("Permissions result: $result, shouldShowRationale: $shouldShowRationale")

            if (shouldShowRationale) {
                showRationaleDialog()
            } else {
                showSettingsDialog()
            }
        }
    }

    private fun showRationaleDialog() {
        val dialog = AlertDialog.Builder(activity)
            .setTitle("Permission Required")
            .setMessage(description)
            .setPositiveButton("Grant") { dialog, _ ->
                dialog.dismiss()
                requestPermissions()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(ContextCompat.getColor(activity, android.R.color.holo_orange_dark))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                ?.setTextColor(ContextCompat.getColor(activity, android.R.color.darker_gray))
        }

        dialog.show()
    }

    private fun showSettingsDialog() {
        val dialog = AlertDialog.Builder(activity)
            .setTitle("Permissions Denied")
            .setMessage("Some permissions are permanently denied. Please grant them in Settings.")
            .setPositiveButton("Open Settings") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", activity.packageName, null)
                )
                settingsLauncher.launch(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(ContextCompat.getColor(activity, android.R.color.holo_orange_dark))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                ?.setTextColor(ContextCompat.getColor(activity, android.R.color.darker_gray))
        }

        dialog.show()
    }
}

/*
UseCase:
define at top :
private lateinit var permissionHandler: MultiPermissionHandler

 In class lifecycle :
handlePermission(this)

 React on callbacks :

@SuppressLint("InlinedApi")
private fun handlePermission(ctx: Context) {
    //generate FCM token
    permissionHandler = MultiPermissionHandler(
        activity = ctx.requireActivity(),
        permissions = listOf(android.Manifest.permission.POST_NOTIFICATIONS),
        requiredPermissions = emptyList()
    ) { grantedList ->
        // Handle granted permissions
        viewModel.generateToken(ctx)
    }

    permissionHandler.registerLaunchers(
        registerForActivityResult = { contract, callback ->
            registerForActivityResult(contract, callback)
        },
        registerForSettings = { contract ->
            registerForActivityResult(contract) { result ->
                // When coming back from settings, recheck permissions
                permissionHandler.requestPermissions()
            }
        }
    )

    // Launch permission request
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionHandler.requestPermissions()
    } else {
        viewModel.generateToken(ctx)
    }
}
 */


