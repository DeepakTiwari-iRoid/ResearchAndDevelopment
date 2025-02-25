package com.app.researchanddevelopment.wearables

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.app.researchanddevelopment.R
import com.app.researchanddevelopment.ui.theme.white
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private val not_install = HealthConnectBanner(
    title = "Sync with Health Connect",
    description = "Keep Run Track updated with the latest information from your other apps, like your calories, heart rate etc..",
    textButton = "Get Started"
)

private val not_given_permission = HealthConnectBanner(
    title = "Get more detailed Workouts",
    description = "Keep Run Track updated with the latest information from your other apps, like your calories, heart rate etc..",
    textButton = "Set up Health Connect"
)


private val banner_selector = Pair(not_install, not_given_permission)


@Preview(showBackground = true)
@Composable
fun SyncHealthConnectPreview(modifier: Modifier = Modifier) {
    HealthConnectBanner(
        modifier = modifier,
        healthConnectBanner = HealthConnectBanner(
            title = "Sync with Health Connect",
            description = "Keep Run Track updated with the latest information from your other apps, like your calories, heart rate etc..",
            textButton = "Get Started"
        ),
        onClick = {},
        onClose = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun WearableScreenPreview() {

    WearableScreen(
        modifier = Modifier.fillMaxSize(),
        healthConnectManager = HealthConnectManager(LocalContext.current),
        viewModel = WearableViewModel(
            healthConnectManager = HealthConnectManager(LocalContext.current)
        )
    )
}


@Composable
fun WearableScreen(
    modifier: Modifier = Modifier,
    healthConnectManager: HealthConnectManager,
    viewModel: WearableViewModel
) {

    WearableContent(
        steps = "",
        maxBmx = "",
        minBmx = "",
        modifier = modifier.statusBarsPadding(),
        healthConnectManager = healthConnectManager,
        viewModel = viewModel,
        permissions = viewModel.permissions
    )
}


@Composable
fun WearableContent(
    steps: String,
    minBmx: String,
    maxBmx: String,
    modifier: Modifier = Modifier,
    healthConnectManager: HealthConnectManager,
    permissions: Set<String>,
    viewModel: WearableViewModel
) {


    val stepsFlow by viewModel.steps

    val context = LocalContext.current

    var allPermissionGranted by remember { mutableStateOf(false) }
    var isBannerVisible by remember { mutableStateOf(Pair(true, banner_selector.first)) }
    val availability by healthConnectManager.availability

    val scope = rememberCoroutineScope()

    // Build the URL to allow the user to install the Health Connect package
    val url = Uri.parse(stringResource(id = R.string.market_url))
        .buildUpon()
        .appendQueryParameter("id", stringResource(id = R.string.health_connect_package))
        // Additional parameter to execute the onboarding flow.
        .appendQueryParameter("url", stringResource(id = R.string.onboarding_url))
        .build()


    val permissionsLauncher =
        rememberLauncherForActivityResult(viewModel.permissionsLauncher) {
            Toast.makeText(context, "All Permission Granted!", Toast.LENGTH_SHORT).show()
        }

    LifecycleEventEffect(
        event = Lifecycle.Event.ON_RESUME,
    ) {
        healthConnectManager.checkAvailability()

        scope.launch(Dispatchers.Main) {
            healthConnectManager.hasAllPermissions(permissions).also { isGranted ->
                if (isGranted) {
                    allPermissionGranted = true
                } else {
                    permissionsLauncher.launch(viewModel.permissions)
                }
            }
        }.invokeOnCompletion {
            isBannerVisible = when (availability) {
                HealthConnectAvailability.INSTALLED -> Pair(
                    !allPermissionGranted,
                    banner_selector.second
                )

                HealthConnectAvailability.NOT_INSTALLED -> Pair(true, banner_selector.first)

                HealthConnectAvailability.NOT_SUPPORTED -> Pair(false, banner_selector.first)
            }
        }
    }

    Box(
        modifier = modifier.padding(12.dp),
        contentAlignment = Alignment.Center
    ) {

        if (isBannerVisible.first) {
            HealthConnectBanner(
                modifier = Modifier.align(Alignment.TopCenter),
                healthConnectBanner = isBannerVisible.second,
                onClick = {
                    if (availability == HealthConnectAvailability.INSTALLED) {
                        val settingsIntent = Intent()
                        settingsIntent.action = HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS
                        context.startActivity(settingsIntent)
                    } else {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, url)
                        )
                    }
                },
                onClose = { isBannerVisible = isBannerVisible.copy(false) }
            )
        }

        Column(
            modifier = modifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(text = "Steps: ${stepsFlow.map { it.count }}")
            Text(text = "BMX: min $minBmx max $maxBmx")

            Spacer(modifier = Modifier.padding(18.dp))

            Button(
                onClick = {},
                shape = RoundedCornerShape(12)
            ) {
                Text("Fetch From Wearable")
            }

            Button(
                onClick = {
                    if (availability == HealthConnectAvailability.INSTALLED) {
                        val settingsIntent = Intent()
                        settingsIntent.action = HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS
                        context.startActivity(settingsIntent)
                    }
                },
                shape = RoundedCornerShape(12)
            ) {
                Text("Open Setting")
            }

            Button(
                onClick = {
                    if (availability == HealthConnectAvailability.INSTALLED) {
                        viewModel.startStopSession()
                    }
                },
                shape = RoundedCornerShape(12)
            ) {
                Text("Start/Stop Session")
            }
        }
    }
}


@Composable
fun HealthConnectBanner(
    modifier: Modifier = Modifier,
    healthConnectBanner: HealthConnectBanner,
    onClose: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .clip(RoundedCornerShape(25))
            .background(color = Color.Black.copy(8f))
            .border(1.dp, Color.Cyan, shape = RoundedCornerShape(25))
            .padding(horizontal = 18.dp, vertical = 22.dp),
    ) {

        Column {

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_health_connect),
                    contentDescription = null,
                    tint = white.copy(0.8f),
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    healthConnectBanner.title, Modifier.weight(1f), style = TextStyle(
                        color = white.copy(0.8f),
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        fontWeight = FontWeight.Medium
                    )
                )

                IconButton(
                    onClick = onClose
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "close",
                        tint = white.copy(.8f)
                    )
                }
            }

            Spacer(Modifier.padding(8.dp))

            Text(
                text = healthConnectBanner.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = white.copy(.8f),
                    fontWeight = FontWeight.SemiBold
                )
            )

            TextButton(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = Color.Transparent, contentColor = Color.Cyan)
            ) {
                Text(healthConnectBanner.textButton)
            }
        }
    }
}

data class HealthConnectBanner(
    val title: String,
    val description: String,
    val textButton: String
)


