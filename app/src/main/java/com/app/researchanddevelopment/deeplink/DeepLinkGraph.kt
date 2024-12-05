package com.app.researchanddevelopment.deeplink

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.app.researchanddevelopment.GraphState
import com.app.researchanddevelopment.MainActivity
import com.app.researchanddevelopment.MainViewModel
import com.app.researchanddevelopment.notification.NotificationUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlin.random.Random


@Composable
fun DeepLinkApp(
    viewModel: MainViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {

    when (viewModel.authState) {

        GraphState.LOADING -> {
            SplashScreen(modifier = modifier)
        }

        is GraphState.SUCCESS -> {
            MainScreen(viewModel, navController, modifier)
        }

    }
}


@Composable
fun MainScreen(
    viewModel: MainViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        DeepLinkNavHost(
            modifier = modifier,
            startDestination = (viewModel.authState as GraphState.SUCCESS).route,
            navController = navController
        )
    }
}

@Composable
fun DeepLinkNavHost(
    modifier: Modifier = Modifier,
    startDestination: String,
    navController: NavHostController
) {

    var shouldPushNotification by remember { mutableStateOf(false) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    CheckPermission {
        shouldPushNotification = it
    }

    Scaffold(
        modifier = modifier.then(Modifier.fillMaxSize()),
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            navItemList.any { it == currentDestination?.route }.let {
                if (it) BottomNavigationBar(modifier = Modifier.fillMaxWidth(), navController, currentDestination)
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            route = "Route",
            startDestination = startDestination,
            modifier = modifier.padding(paddingValues)
        ) {

            authNavGraph(navController)
            mainNavGraph(navController)
        }

    }
}


fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(
        startDestination = "login", route = "auth"
    ) {

        composable(
            "login", deepLinks = listOf(navDeepLink {
                uriPattern = "$BaseDeepLink/login/{$iD}"
            })
        ) {
            ScreenMaker("Login Screen") {
                navController.navigate("main") {
                    popUpTo("auth")
                }
            }
        }

        composable(
            "register",
            deepLinks = listOf(navDeepLink {
                uriPattern = "$BaseDeepLink/register/{$iD}"
            })
        ) {
            ScreenMaker("Register Screen") {
                navController.navigate("main") {
                    popUpTo("auth")
                }
            }
        }
    }
}


fun NavGraphBuilder.mainNavGraph(navController: NavHostController) {

    navigation(
        startDestination = "home",
        route = "main",
    ) {

        composable(
            "home",
            deepLinks = listOf(navDeepLink {
                uriPattern = "$BaseDeepLink/main/{$iD}"
            })
        ) { backStackEntry ->

            val id = backStackEntry.arguments?.getString(iD)
            val ctx = LocalContext.current

            HomeScreen(
                id = id,
                onClick = { pushNotification(context = ctx, route = "login") },
                shouldPushNotification = true
            )
        }

        composable(
            "profile",
            deepLinks = listOf(navDeepLink {
                uriPattern = "$BaseDeepLink/profile/{$iD}"
            })
        ) {

            ScreenMaker(name = "Profile") {
                navController.navigate("auth") {
                    popUpTo("main")
                }
            }
        }

        composable(
            "search",
            deepLinks = listOf(navDeepLink {
                uriPattern = "$BaseDeepLink/setting/{$iD}"
            })
        ) {
            ScreenMaker(name = "Search") { }
        }

    }

}


//-----------------


@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
) {


    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.error)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "SPLASH\nSCREEN",
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                lineHeight = 48.sp,
                fontSize = 48.sp,
                fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                color = MaterialTheme.colorScheme.background
            )

            Text(
                text = "DEEPAK TIWARI",
                fontWeight = FontWeight.Bold,
                letterSpacing = 9.sp,
                fontSize = 10.sp,
                fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}


@Composable
fun HomeScreen(modifier: Modifier = Modifier, id: String?, onClick: () -> Unit = {}, shouldPushNotification: Boolean = false) {

    Column(
        modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = id ?: "Main Screen")
        Button(
            modifier = Modifier, enabled = shouldPushNotification, onClick = onClick
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
fun CheckPermission(isGranted: (Boolean) -> Unit) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

        val notificationPermissionState = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

        val requestPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            isGranted(it)
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


@Composable
fun BottomNavigationBar(modifier: Modifier = Modifier, navController: NavHostController, currentDestination: NavDestination?) {

    BottomAppBar(modifier = modifier) {
        navItemList.forEach {
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { (it.route ?: "").equals(it) } == true,
                onClick = {
                    navigate(navController, it)
                },
                icon = {
                    Icon(Icons.Filled.Home, "home")
                },
                label = {
                    Text(it)
                }
            )
        }
    }
}

private fun navigate(navController: NavHostController, screen: String) {
    navController.navigate(screen) {
        popUpTo("home") {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}


@Composable
fun ScreenMaker(name: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface),
    ) {
        Text(name,
            Modifier
                .align(Alignment.Center)
                .clickable { onClick() })
    }

}

const
val BaseDeepLink = "https://www.researchanddevelopment.com"
val navItemList = listOf("home", "search", "profile")
val iD = "exampleId ${Random.nextInt(1, 100)}"


@Preview
@Composable
private fun SplashPreview() {
    SplashScreen()
}