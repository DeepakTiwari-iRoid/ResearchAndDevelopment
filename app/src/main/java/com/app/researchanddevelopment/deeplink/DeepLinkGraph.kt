package com.app.researchanddevelopment.deeplink

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.app.researchanddevelopment.MainActivity
import com.app.researchanddevelopment.notification.NotificationUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay
import kotlin.random.Random


@Composable
fun DeepLinkNavHost(modifier: Modifier = Modifier, startDestination: String, navController: NavHostController) {

    var shouldPushNotification by remember { mutableStateOf(false) }
    val ctx = LocalContext.current

    CheckPermission {
        shouldPushNotification = it
    }

    NavHost(
        navController = navController, route = "Route", startDestination = startDestination, modifier = modifier
    ) {

        composable("auth", deepLinks = listOf(navDeepLink {
            uriPattern = "$BaseDeepLink/auth/{$iD}"
        })) {
            Login()
        }

        composable(
            "main", deepLinks = listOf(navDeepLink {
                uriPattern = "$BaseDeepLink/main/{$iD}"
            })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString(iD)
            MainScreen(
                id = id,
                onClick = { pushNotification(context = ctx, route = "auth") },
                shouldPushNotification = shouldPushNotification
            )
        }
    }
}


@Composable
fun SplashScreen(modifier: Modifier = Modifier, duration: Long = 1000, shouldShowSplash: Uri?, showOtherScreen: () -> Unit) {

    val updatedCall by rememberUpdatedState(showOtherScreen)

    if (shouldShowSplash != null) {
        updatedCall()
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.error)
    ) {
        Text(text = "Splash Screen", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
    }

    LaunchedEffect(Unit) {
        delay(duration)
        updatedCall()

    }
}

@Composable
fun Login(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface)
    ) {
        Text(text = "Login Screen", modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, id: String?, onClick: () -> Unit = {}, shouldPushNotification: Boolean = false) {

    Column(
        modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = id ?: "Main Screen")
        Button(
            modifier = Modifier,
            enabled = shouldPushNotification,
            onClick = onClick
        ) {
            Text(text = "Push Notification")
        }
    }
}


fun pushNotification(context: Context, route: String) {

    val deepLinkIntent = Intent(
        Intent.ACTION_VIEW, "$BaseDeepLink/$route/$iD".toUri(), context, MainActivity::class.java
    )

    val deepLinkPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
        addNextIntentWithParentStack(deepLinkIntent)
        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    NotificationUtils().sendNotification(
        context = context, title = "DeepLink", message = "testing Deep Link navigation", pendingIntent = deepLinkPendingIntent, id = Random.nextInt()
    )
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CheckPermission(modifier: Modifier = Modifier, isGranted: (Boolean) -> Unit) {


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

        val notificationPermissionState = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

        val requestPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            isGranted(isGranted)
        }

        LaunchedEffect(notificationPermissionState) {
            if (!notificationPermissionState.status.isGranted && notificationPermissionState.status.shouldShowRationale) {
                // Show rationale if needed
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

const val BaseDeepLink = "https://www.researchanddevelopment.com"
val iD = "exampleId ${Random.nextInt(1, 100)}"

