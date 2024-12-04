package com.app.researchanddevelopment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.app.researchanddevelopment.deeplink.DeepLinkNavHost
import com.app.researchanddevelopment.deeplink.SplashScreen
import com.app.researchanddevelopment.ui.theme.ResearchAndDevelopmentTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ResearchAndDevelopmentTheme {
                var startDestination by remember { mutableStateOf(false) }
                val navController = rememberNavController()

                SplashScreen(shouldShowSplash = intent?.data) {
                    startDestination = true
                }

                if (startDestination) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        DeepLinkNavHost(modifier = Modifier, startDestination = "main", navController = navController)
                    }
                }
            }
        }
    }
}