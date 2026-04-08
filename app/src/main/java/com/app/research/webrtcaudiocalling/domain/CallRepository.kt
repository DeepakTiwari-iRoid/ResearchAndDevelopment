package com.app.research.webrtcaudiocalling.domain

import kotlinx.coroutines.flow.StateFlow

interface CallRepository {
    val callState: StateFlow<CallState>
    val isMicEnabled: StateFlow<Boolean>
    fun connect(roomId: String, isInitiator: Boolean)
    fun setMicEnabled(enabled: Boolean)
    fun disconnect()
    fun release()
}
