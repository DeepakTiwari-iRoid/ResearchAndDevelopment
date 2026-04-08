package com.app.research.webrtcaudiocalling.signaling

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import timber.log.Timber
import java.net.URI

/**
 * Uses [io.socket:socket.io-client] with the same options as your GoalPic app.
 * Emits **JSONObject** payloads (not raw strings) so Node servers receive a proper object.
 *
 * Incoming payloads are the same shape your other app gets with
 * `val data = args[0] as JSONObject` — we already normalize that in the `webrtc:*` listeners.
 * If your server only sends one event (e.g. chat `new_message`), use [ingestWebRtcJsonObject].
 */
class SignalingClient(
    private val serverUri: String = SignalingConfig.SOCKET_BASE_URL,
    private val socketPath: String = SignalingConfig.SOCKET_PATH,
    private val token: String = SignalingConfig.SOCKET_TOKEN
) {

    private val gson = Gson()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var socket: Socket? = null

    var onOfferReceived: ((SessionDescription) -> Unit)? = null
    var onAnswerReceived: ((SessionDescription) -> Unit)? = null
    var onIceCandidateReceived: ((IceCandidate) -> Unit)? = null
    var onConnected: (() -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    private fun postMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block()
        else mainHandler.post(block)
    }

    fun connect() {
        if (socket?.connected() == true) return
        socket?.off()
        socket?.disconnect()
        socket = null

        try {
            val options = IO.Options().apply {
                path = socketPath
                transports = arrayOf("websocket", "polling")
                upgrade = true
                rememberUpgrade = true
                timeout = 20_000
                forceNew = true
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 1000
                reconnectionDelayMax = 5000
                if (token.isNotBlank()) {
                    extraHeaders = mapOf("Authorization" to listOf(token))
                }
            }

            val s = IO.socket(URI.create(serverUri), options)

            s.on(Socket.EVENT_CONNECT) {
                Timber.tag(TAG).d("Connected to signaling server")
                postMain { onConnected?.invoke() }
            }

            s.on(Socket.EVENT_DISCONNECT) {
                Timber.tag(TAG).d("Disconnected from signaling server")
                postMain { onDisconnected?.invoke() }
            }

            s.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val msg = args.firstOrNull()?.let { formatConnectError(it) } ?: "connect_error"
                Timber.tag(TAG).e("Connect error: $msg")
                postMain { onError?.invoke(msg) }
            }

            s.on(Events.OFFER) { args ->
                val json = args.toSignalingJsonString(gson)
                if (json == null) {
                    Timber.tag(TAG).w("webrtc:offer — could not read payload, args=${args.toList()}")
                    return@on
                }
                Timber.tag(TAG).d("Received offer payload: ${json.take(120)}…")
                dispatchOfferJson(json)
            }

            s.on(Events.ANSWER) { args ->
                val json = args.toSignalingJsonString(gson)
                if (json == null) {
                    Timber.tag(TAG).w("webrtc:answer — could not read payload, args=${args.toList()}")
                    return@on
                }
                Timber.tag(TAG).d("Received answer")
                dispatchAnswerJson(json)
            }

            s.on(Events.ICE_CANDIDATE) { args ->
                val json = args.toSignalingJsonString(gson) ?: return@on
                dispatchIceJson(json)
            }

            // Bridge for servers that send WebRTC signaling through a generic event.
            // Your other app receives `val data = args[0] as JSONObject`.
            s.on(SignalingConfig.SOCKET_INCOMING_EVENT) { args ->
                val first = args.firstOrNull()
                val jsonObj = when (first) {
                    is JSONObject -> first
                    is String -> runCatching { JSONObject(first) }.getOrNull()
                    else -> null
                }

                if (jsonObj == null) {
                    Timber.tag(TAG).w(
                        "SOCKET_INCOMING_EVENT payload is not JSONObject. type=${first?.javaClass?.name}"
                    )
                    return@on
                }

                ingestWebRtcJsonObject(jsonObj)
            }

            socket = s
            s.connect()
        } catch (e: Exception) {
            Timber.e(e, "Failed to create socket")
            postMain { onError?.invoke(e.message ?: "Socket setup failed") }
        }
    }

    fun joinRoom(roomId: String) {
        val json = JSONObject().apply { put("roomId", roomId) }
        socket?.emit(Events.JOIN, json)
        Timber.tag(TAG).d("Emitted join room: $roomId")
    }

    fun sendOffer(roomId: String, sdp: SessionDescription) {
        val payload = SdpPayload(roomId = roomId, type = sdp.type.canonicalForm(), sdp = sdp.description)
        socket?.emit(Events.OFFER, JSONObject(gson.toJson(payload)))
        Timber.tag(TAG).d("Sent offer to room: $roomId")
    }

    fun sendAnswer(roomId: String, sdp: SessionDescription) {
        val payload = SdpPayload(roomId = roomId, type = sdp.type.canonicalForm(), sdp = sdp.description)
        socket?.emit(Events.ANSWER, JSONObject(gson.toJson(payload)))
        Timber.tag(TAG).d("Sent answer to room: $roomId")
    }

    fun sendIceCandidate(roomId: String, candidate: IceCandidate) {
        val payload = IceCandidatePayload(
            roomId = roomId,
            sdpMid = candidate.sdpMid,
            sdpMLineIndex = candidate.sdpMLineIndex,
            sdp = candidate.sdp
        )
        socket?.emit(Events.ICE_CANDIDATE, JSONObject(gson.toJson(payload)))
    }

    fun leaveRoom(roomId: String) {
        socket?.emit(Events.LEAVE, JSONObject().apply { put("roomId", roomId) })
        Timber.tag(TAG).d("Left room: $roomId")
    }

    fun disconnect() {
        socket?.off()
        socket?.disconnect()
        socket = null
        onOfferReceived = null
        onAnswerReceived = null
        onIceCandidateReceived = null
        onConnected = null
        onDisconnected = null
        onError = null
    }

    /**
     * Forward WebRTC signaling when your app receives a shared event, e.g.
     * `Emitter.Listener { args -> ingestWebRtcJsonObject(args[0] as JSONObject) }`.
     * Expects the same JSON as [Events.OFFER] / [Events.ANSWER] / [Events.ICE_CANDIDATE]
     * (`roomId`, `type`+`sdp` for SDP, or `sdpMid`/`sdpMLineIndex`/`sdp` for ICE).
     */
    fun ingestWebRtcJsonObject(data: JSONObject) {
        when {
            data.has("sdpMid") && data.has("sdp") && data.has("roomId") ->
                dispatchIceJson(data.toString())

            data.optString("type").equals("offer", ignoreCase = true) ->
                dispatchOfferJson(data.toString())

            data.optString("type").equals("answer", ignoreCase = true) ->
                dispatchAnswerJson(data.toString())

            else ->
                Timber.tag(TAG).w(
                    "ingestWebRtcJsonObject: unrecognized JSON (need type offer/answer or ICE fields): " +
                        data.toString().take(300)
                )
        }
    }

    private fun dispatchOfferJson(json: String) {
        val payload = runCatching { gson.fromJson(json, SdpPayload::class.java) }.getOrNull()
        if (payload == null) {
            Timber.tag(TAG).e("Failed to parse offer: $json")
            return
        }
        val sdp = SessionDescription(SessionDescription.Type.fromCanonicalForm(payload.type), payload.sdp)
        postMain { onOfferReceived?.invoke(sdp) }
    }

    private fun dispatchAnswerJson(json: String) {
        val payload = runCatching { gson.fromJson(json, SdpPayload::class.java) }.getOrNull()
        if (payload == null) {
            Timber.tag(TAG).e("Failed to parse answer: $json")
            return
        }
        val sdp = SessionDescription(SessionDescription.Type.fromCanonicalForm(payload.type), payload.sdp)
        postMain { onAnswerReceived?.invoke(sdp) }
    }

    private fun dispatchIceJson(json: String) {
        val payload = runCatching { gson.fromJson(json, IceCandidatePayload::class.java) }.getOrNull()
        if (payload == null) {
            Timber.tag(TAG).e("Failed to parse ice-candidate: $json")
            return
        }
        Timber.tag(TAG).d("Received ICE candidate")
        val candidate = IceCandidate(payload.sdpMid, payload.sdpMLineIndex, payload.sdp)
        postMain { onIceCandidateReceived?.invoke(candidate) }
    }

    object Events {
        const val JOIN = "webrtc:join"
        const val LEAVE = "webrtc:leave"
        const val OFFER = "webrtc:offer"
        const val ANSWER = "webrtc:answer"
        const val ICE_CANDIDATE = "webrtc:ice-candidate"
    }

    private data class SdpPayload(
        @SerializedName("roomId") val roomId: String,
        @SerializedName("type") val type: String,
        @SerializedName("sdp") val sdp: String
    )

    private data class IceCandidatePayload(
        @SerializedName("roomId") val roomId: String,
        @SerializedName("sdpMid") val sdpMid: String,
        @SerializedName("sdpMLineIndex") val sdpMLineIndex: Int,
        @SerializedName("sdp") val sdp: String
    )

    companion object {
        internal const val TAG = "WebRTC-Signal"

        private fun formatConnectError(arg: Any): String = when (arg) {
            is Exception -> arg.message ?: arg.javaClass.simpleName
            else -> arg.toString()
        }
    }
}

/**
 * Server / client may deliver [JSONObject], [String], [JSONArray], or [Map] (e.g. LinkedHashMap).
 */
private fun Array<out Any>.toSignalingJsonString(gson: Gson): String? {
    val first = firstOrNull() ?: return null
    return first.toSignalingJsonString(gson)
}

private fun Any.toSignalingJsonString(gson: Gson): String? = when (this) {
    is String -> this
    is JSONObject -> this.toString()
    is JSONArray -> {
        if (length() == 0) null
        else (get(0) as? Any)?.toSignalingJsonString(gson)
    }
    is Map<*, *> -> runCatching { gson.toJson(this) }.getOrNull()
    else -> {
        Timber.tag("WebRTC-Signal").d("Signaling payload type: ${this::class.java.name}")
        runCatching { gson.toJson(this) }.getOrNull()
            ?: toString().takeIf { it.trimStart().startsWith("{") }
    }
}
