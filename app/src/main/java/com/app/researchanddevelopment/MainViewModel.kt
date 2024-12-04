package com.app.researchanddevelopment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    var startDestination by mutableStateOf("home")


    init {
        getDestination()
    }

    private fun getDestination() {
        startDestination = "home"
    }

}