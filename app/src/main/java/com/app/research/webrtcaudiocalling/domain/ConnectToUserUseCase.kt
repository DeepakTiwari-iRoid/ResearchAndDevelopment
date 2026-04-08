package com.app.research.webrtcaudiocalling.domain

class ConnectToUserUseCase(private val repository: CallRepository) {
    operator fun invoke(roomId: String, isInitiator: Boolean) {
        repository.connect(roomId, isInitiator)
    }
}
