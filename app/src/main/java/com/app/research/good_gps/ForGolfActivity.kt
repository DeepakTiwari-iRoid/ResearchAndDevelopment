package com.app.research.good_gps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController

class ForGolfActivity : ComponentActivity() {

    val viewModel: ForeGolfVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ForeGolfApp(
                navHostController = rememberNavController(),
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel
            )
        }
    }
}