package com.app.researchanddevelopment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import com.app.researchanddevelopment.ui.theme.ResearchAndDevelopmentTheme
import com.app.researchanddevelopment.wearables.WearableScreen
import com.app.researchanddevelopment.wearables.WearableViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val healthConnectManager =
            (application as ResearchAndDevelopmentApplication).healthConnectManager

        setContent {
            ResearchAndDevelopmentTheme {
                WearableScreen(
                    modifier = Modifier
                        .fillMaxSize(),
                    healthConnectManager = healthConnectManager,
                    viewModel = WearableViewModel(healthConnectManager)
                )
            }
        }
    }
}