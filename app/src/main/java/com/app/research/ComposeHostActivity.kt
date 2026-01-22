package com.app.research

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.research.camoverlaypointsmapping.CamPointsMappingScreen
import com.app.research.chatpaging.ChatScreen
import com.app.research.data.Constants
import com.app.research.tensorflow.TensorFlow
import org.opencv.android.OpenCVLoader
import timber.log.Timber

class ComposeHostActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = Color.Transparent.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = Color.Transparent.toArgb(),
            )
        )

        OpenCVLoader.initDebug()

        Timber.d("ComposeHostActivity started with startDestination: ${intent.extras?.getString("start_destination")}")
        val startDestination =
            intent.extras?.getString(Constants.KEYS.START_DEST) ?: return finish()

        setContent {
            ComposeHost(startDestination = startDestination, modifier = Modifier)
        }
    }
}


@Composable
fun ComposeHost(
    modifier: Modifier = Modifier,
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {

    NavHost(
        navController = navController,
        route = "root",
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screens.ChatCustomPaging.name) {
            ChatScreen(
                modifier = Modifier
            )
        }

        composable(Screens.TensorFlow.name) {
            TensorFlow()
        }

        composable(route = Screens.CamPointsMappingOverlay.name) {
            CamPointsMappingScreen()
        }
    }
}