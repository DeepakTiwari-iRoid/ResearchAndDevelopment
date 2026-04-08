package com.app.research.webrtcaudiocalling.webrtc

import android.content.Context
import android.media.AudioManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.audio.JavaAudioDeviceModule
import timber.log.Timber

class WebRTCManager(private val context: Context) {

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null
    private var audioSource: AudioSource? = null

    private val _iceConnectionState = MutableStateFlow(PeerConnection.IceConnectionState.NEW)
    val iceConnectionState: StateFlow<PeerConnection.IceConnectionState> = _iceConnectionState

    var onIceCandidate: ((IceCandidate) -> Unit)? = null
    var onLocalSdp: ((SessionDescription) -> Unit)? = null

    fun initialize() {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
        )

        val audioDeviceModule = JavaAudioDeviceModule.builder(context)
            .createAudioDeviceModule()

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setAudioDeviceModule(audioDeviceModule)
            .createPeerConnectionFactory()

        configureAudio()
    }

    @Suppress("DEPRECATION")
    private fun configureAudio() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = true
    }

    fun createPeerConnection() {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )

        val config = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        peerConnection = peerConnectionFactory?.createPeerConnection(
            config,
            object : PeerConnection.Observer {
                override fun onSignalingChange(state: PeerConnection.SignalingState) {
                    Timber.d("Signaling state: $state")
                }

                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                    Timber.d("ICE connection state: $state")
                    _iceConnectionState.value = state
                }

                override fun onIceConnectionReceivingChange(receiving: Boolean) {}

                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {
                    Timber.d("ICE gathering state: $state")
                }

                override fun onIceCandidate(candidate: IceCandidate) {
                    Timber.d("Local ICE candidate: ${candidate.sdpMid}")
                    this@WebRTCManager.onIceCandidate?.invoke(candidate)
                }

                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) {}

                override fun onAddStream(stream: MediaStream) {
                    Timber.d("Remote stream added with ${stream.audioTracks.size} audio tracks")
                }

                override fun onRemoveStream(stream: MediaStream) {}
                override fun onDataChannel(channel: DataChannel) {}
                override fun onRenegotiationNeeded() {
                    Timber.d("Renegotiation needed")
                }

                override fun onAddTrack(receiver: RtpReceiver, streams: Array<out MediaStream>) {
                    Timber.d("Remote track added: ${receiver.track()?.kind()}")
                }
            }
        )

        val constraints = MediaConstraints()
        audioSource = peerConnectionFactory?.createAudioSource(constraints)
        localAudioTrack = peerConnectionFactory?.createAudioTrack("local_audio", audioSource)
        localAudioTrack?.setEnabled(false)

        peerConnection?.addTrack(localAudioTrack, listOf("local_stream"))
    }

    fun createOffer() {
        val constraints = audioOnlyConstraints()
        peerConnection?.createOffer(object : SdpObserverAdapter("createOffer") {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(SdpObserverAdapter("setLocalDesc-offer"), sdp)
                onLocalSdp?.invoke(sdp)
            }
        }, constraints)
    }

    fun createAnswer() {
        val constraints = audioOnlyConstraints()
        peerConnection?.createAnswer(object : SdpObserverAdapter("createAnswer") {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(SdpObserverAdapter("setLocalDesc-answer"), sdp)
                onLocalSdp?.invoke(sdp)
            }
        }, constraints)
    }

    fun setRemoteDescription(sdp: SessionDescription) {
        peerConnection?.setRemoteDescription(SdpObserverAdapter("setRemoteDesc"), sdp)
    }

    fun addIceCandidate(candidate: IceCandidate) {
        peerConnection?.addIceCandidate(candidate)
    }

    fun setMicEnabled(enabled: Boolean) {
        localAudioTrack?.setEnabled(enabled)
        Timber.d("Mic enabled: $enabled")
    }

    @Suppress("DEPRECATION")
    fun release() {
        onIceCandidate = null
        onLocalSdp = null
        localAudioTrack?.dispose()
        localAudioTrack = null
        audioSource?.dispose()
        audioSource = null
        peerConnection?.dispose()
        peerConnection = null
        peerConnectionFactory?.dispose()
        peerConnectionFactory = null
        _iceConnectionState.value = PeerConnection.IceConnectionState.NEW

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.isSpeakerphoneOn = false
    }

    private fun audioOnlyConstraints() = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
    }

    private open class SdpObserverAdapter(private val tag: String) : SdpObserver {
        override fun onCreateSuccess(sdp: SessionDescription) {
            Timber.d("[$tag] SDP create success")
        }

        override fun onSetSuccess() {
            Timber.d("[$tag] SDP set success")
        }

        override fun onCreateFailure(error: String) {
            Timber.e("[$tag] SDP create failure: $error")
        }

        override fun onSetFailure(error: String) {
            Timber.e("[$tag] SDP set failure: $error")
        }
    }
}
