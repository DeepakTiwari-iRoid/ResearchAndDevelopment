package com.app.researchanddevelopment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.app.researchanddevelopment.deeplink.DeepLinkApp
import com.app.researchanddevelopment.ui.theme.ResearchAndDevelopmentTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ResearchAndDevelopmentTheme {
                DeepLinkApp(viewModel = viewModel, navController = rememberNavController())
            }
        }
    }
}