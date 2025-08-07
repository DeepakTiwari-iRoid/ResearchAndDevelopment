package com.app.research

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.research.chatpaging.ChatScreen

class ComposeHostActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startDestination = intent.getStringExtra("startDestination") ?: "chat"

        setContent {
            ComposeHost(startDestination = startDestination, modifier = Modifier)
        }
    }
}


@Composable
fun ComposeHost(modifier: Modifier = Modifier, startDestination: String) {

    NavHost(
        navController = rememberNavController(),
        route = "root",
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("chat") {
            ChatScreen(
                modifier = Modifier
            )
        }
    }
}