package com.app.researchanddevelopment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.app.researchanddevelopment.AuthState.SUCCESS
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    var authState by mutableStateOf<AuthState>(AuthState.LOADING)
    val exceptionHandler = CoroutineExceptionHandler { _, throwable -> println(throwable) }
    val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    init {
        getDestination()
    }

    private fun getDestination() {
        viewModelScope.launch {
            delay(3000)
            authState = SUCCESS(route = "auth")
        }
    }
}


sealed interface AuthState {
    object LOADING : AuthState
    data class SUCCESS(val route: String) : AuthState
}