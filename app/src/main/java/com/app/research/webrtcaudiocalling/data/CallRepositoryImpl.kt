package com.app.research.webrtcaudiocalling.data

import com.app.research.webrtcaudiocalling.domain.CallRepository
import com.app.research.webrtcaudiocalling.domain.CallState
import com.app.research.webrtcaudiocalling.signaling.SignalingClient
import com.app.research.webrtcaudiocalling.webrtc.WebRTCManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import timber.log.Timber

class CallRepositoryImpl(
    private val webRTCManager: WebRTCManager,
    private val signalingClient: SignalingClient,
    private val scope: CoroutineScope
) : CallRepository {

    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    override val callState: StateFlow<CallState> = _callState

    private val _isMicEnabled = MutableStateFlow(false)
    override val isMicEnabled: StateFlow<Boolean> = _isMicEnabled

    private var currentRoomId: String? = null
    private var isInitiator = false
    private var cachedLocalOffer: SessionDescription? = null
    private var offerRetryJob: Job? = null

    init {
        observeIceConnectionState()
    }

    private fun observeIceConnectionState() {
        webRTCManager.iceConnectionState
            .onEach { state ->
                _callState.value = when (state) {
                    PeerConnection.IceConnectionState.CONNECTED,
                    PeerConnection.IceConnectionState.COMPLETED -> CallState.Connected

                    PeerConnection.IceConnectionState.DISCONNECTED -> CallState.Disconnected

                    PeerConnection.IceConnectionState.FAILED -> CallState.Error("Connection failed")

                    PeerConnection.IceConnectionState.CLOSED -> CallState.Disconnected

                    else -> return@onEach
                }
            }
            .launchIn(scope)
    }

    override fun connect(roomId: String, isInitiator: Boolean) {
        this.currentRoomId = roomId
        this.isInitiator = isInitiator
        this.cachedLocalOffer = null
        offerRetryJob?.cancel()
        offerRetryJob = null
        _callState.value = CallState.Connecting

        try {
            webRTCManager.initialize()
            webRTCManager.createPeerConnection()
            setupWebRTCCallbacks(roomId)
            setupSignalingCallbacks(roomId, isInitiator)

            // Register handlers BEFORE connect() so a fast socket connect never misses callbacks.
            signalingClient.onError = { error ->
                Timber.e("Signaling error: $error")
                _callState.value = CallState.Error(error)
            }

            signalingClient.onConnected = {
                signalingClient.joinRoom(roomId)
                if (isInitiator) {
                    scope.launch {
                        delay(JOIN_SETTLE_MS)
                        Timber.d("Creating offer for room: $roomId")
                        webRTCManager.createOffer()
                        startOfferRetry(roomId)
                    }
                } else {
                    Timber.d("Waiting for offer in room: $roomId")
                }
            }

            signalingClient.connect()
        } catch (e: Exception) {
            Timber.e(e, "Failed to connect")
            _callState.value = CallState.Error(e.message ?: "Connection failed")
        }
    }

    private fun startOfferRetry(roomId: String) {
        offerRetryJob?.cancel()
        offerRetryJob = scope.launch {
            repeat(OFFER_RESEND_COUNT) {
                delay(OFFER_RESEND_INTERVAL_MS)
                if (!isInitiator) return@launch
                if (_callState.value !is CallState.Connecting) return@launch
                val offer = cachedLocalOffer ?: return@repeat
                Timber.d("Re-sending SDP offer (peer may have joined late)")
                signalingClient.sendOffer(roomId, offer)
            }
        }
    }

    private fun setupWebRTCCallbacks(roomId: String) {
        webRTCManager.onLocalSdp = { sdp ->
            Timber.d("Sending local SDP: ${sdp.type}")
            if (isInitiator && sdp.type == SessionDescription.Type.OFFER) {
                cachedLocalOffer = sdp
            }
            if (isInitiator) {
                signalingClient.sendOffer(roomId, sdp)
            } else {
                signalingClient.sendAnswer(roomId, sdp)
            }
        }

        webRTCManager.onIceCandidate = { candidate ->
            signalingClient.sendIceCandidate(roomId, candidate)
        }
    }

    private fun setupSignalingCallbacks(roomId: String, isInitiator: Boolean) {
        if (isInitiator) {
            signalingClient.onAnswerReceived = { sdp ->
                Timber.d("Received remote answer")
                offerRetryJob?.cancel()
                webRTCManager.setRemoteDescription(sdp)
            }
        } else {
            signalingClient.onOfferReceived = { sdp ->
                Timber.d("Received remote offer")
                webRTCManager.setRemoteDescription(sdp)
                webRTCManager.createAnswer()
            }
        }

        signalingClient.onIceCandidateReceived = { candidate ->
            Timber.d("Received remote ICE candidate")
            webRTCManager.addIceCandidate(candidate)
        }
    }

    override fun setMicEnabled(enabled: Boolean) {
        webRTCManager.setMicEnabled(enabled)
        _isMicEnabled.value = enabled
    }

    override fun disconnect() {
        offerRetryJob?.cancel()
        offerRetryJob = null
        cachedLocalOffer = null
        currentRoomId?.let { signalingClient.leaveRoom(it) }
        signalingClient.disconnect()
        webRTCManager.release()
        _callState.value = CallState.Idle
        _isMicEnabled.value = false
        currentRoomId = null
    }

    override fun release() {
        disconnect()
    }

    companion object {
        private const val JOIN_SETTLE_MS = 450L
        private const val OFFER_RESEND_INTERVAL_MS = 2500L
        private const val OFFER_RESEND_COUNT = 6
    }
}
