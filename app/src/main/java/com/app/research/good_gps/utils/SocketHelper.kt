package com.app.research.good_gps.utils

import dev.icerock.moko.socket.Socket
import dev.icerock.moko.socket.SocketBuilder
import dev.icerock.moko.socket.SocketEvent
import dev.icerock.moko.socket.SocketOptions
import timber.log.Timber

class SocketHelper(
    endPoint: String,
    token: String,
    val builder: SocketBuilder.() -> Unit
) {

    val socket = Socket(
        endpoint = endPoint,
        config = SocketOptions(
            queryParams = mapOf("token" to token),
            transport = SocketOptions.Transport.WEBSOCKET,
        )
    ) {
        on(SocketEvent.Connect) {
            Timber.tag("Socket").d("Connected successfully")
        }

        on(SocketEvent.Connecting) {
            Timber.tag("Socket").d("Connecting...")
        }

        on(SocketEvent.Disconnect) {
            Timber.tag("Socket").d("Disconnected")
        }

        on(SocketEvent.Error) { error ->
            Timber.tag("Socket").e("Error: $error")
        }

        on(SocketEvent.Reconnect) {
            Timber.tag("Socket").d("Reconnected")
        }

        on(SocketEvent.ReconnectAttempt) { attempt ->
            Timber.tag("Socket").d("Reconnect attempt: $attempt")
        }

        on(SocketEvent.Ping) {
            Timber.tag("Socket").d("Ping")
        }

        on(SocketEvent.Pong) {
            Timber.tag("Socket").d("Pong")
        }

        builder(this)
    }

    companion object {
        const val UPDATE_USER_LOC = "update_user_location"
        const val END_POINT = "http://157.245.106.111:3322"
        const val TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MiwiZW1haWwiOiJkZWVwYWtAeW9wbWFpbC5jb20iLCJuYW1lIjoiRGVlcGFrIiwiaWF0IjoxNzU5ODE1NTAwLCJleHAiOjE3NjA0MjAzMDB9._cq-JHeDuwnt-MlmSUSmLHu7GejVzy5ly34wJi9-lJk"
    }
}