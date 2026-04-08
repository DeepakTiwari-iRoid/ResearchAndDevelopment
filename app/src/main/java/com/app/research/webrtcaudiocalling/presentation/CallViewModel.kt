package com.app.research.webrtcaudiocalling.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.research.webrtcaudiocalling.data.CallRepositoryImpl
import com.app.research.webrtcaudiocalling.domain.CallRepository
import com.app.research.webrtcaudiocalling.domain.CallState
import com.app.research.webrtcaudiocalling.domain.ConnectToUserUseCase
import com.app.research.webrtcaudiocalling.signaling.SignalingClient
import com.app.research.webrtcaudiocalling.webrtc.WebRTCManager
import kotlinx.coroutines.flow.StateFlow

class CallViewModel(application: Application) : AndroidViewModel(application) {

    private val webRTCManager = WebRTCManager(application)
    private val signalingClient = SignalingClient()

    private val repository: CallRepository = CallRepositoryImpl(
        webRTCManager = webRTCManager,
        signalingClient = signalingClient,
        scope = viewModelScope
    )

    private val connectToUser = ConnectToUserUseCase(repository)

    val callState: StateFlow<CallState> = repository.callState
    val isMicEnabled: StateFlow<Boolean> = repository.isMicEnabled

    fun connect(roomId: String, isInitiator: Boolean) {
        connectToUser(roomId, isInitiator)
    }

    fun setMicEnabled(enabled: Boolean) {
        repository.setMicEnabled(enabled)
    }

    fun disconnect() {
        repository.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        repository.release()
    }
}
