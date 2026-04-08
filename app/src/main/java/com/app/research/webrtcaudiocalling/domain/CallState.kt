package com.app.research.webrtcaudiocalling.domain

sealed interface CallState {
    data object Idle : CallState
    data object Connecting : CallState
    data object Connected : CallState
    data class Error(val message: String) : CallState
    data object Disconnected : CallState
}
