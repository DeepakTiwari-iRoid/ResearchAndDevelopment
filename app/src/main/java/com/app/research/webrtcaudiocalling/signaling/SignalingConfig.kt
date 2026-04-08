package com.app.research.webrtcaudiocalling.signaling

import com.app.research.webrtcaudiocalling.signaling.SignalingConfig.SOCKET_BASE_URL
import com.app.research.webrtcaudiocalling.signaling.SignalingConfig.SOCKET_PATH


/**
 * Socket.IO server configuration for WebRTC signaling.
 *
 * **Important:** [SOCKET_BASE_URL] must be only scheme + host + port (no path).
 * The Socket.IO path (e.g. `/goalpic/socket.io`) goes in [SOCKET_PATH] — same as
 * `IO.Options.path` in your other app.
 *
 * **Server relay (required):** the backend must join sockets to a Socket.IO room on
 * `webrtc:join` and **broadcast** `webrtc:offer`, `webrtc:answer`, and `webrtc:ice-candidate`
 * to the other peer in that room. Without this, the callee never receives the offer and
 * the UI stays on “Connecting…”.
 */
object SignalingConfig {
    const val SOCKET_BASE_URL = "https://dev.iroidsolutions.com//" //TODO: remove// from last
    const val SOCKET_PATH = "/goalpic/socket.io"
    // Your other app receives WebRTC signaling data inside this generic event.
    // If your server uses a different event name, change this to match.
    const val SOCKET_INCOMING_EVENT = "new_message"
    const val SOCKET_TOKEN =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxODA2NjQzMTMxLCJpYXQiOjE3NzUxMDcxMzEsImp0aSI6IjgzMzJkNzk3N2RkNjRiOGVhMzFkNTIyNjQzZWRjZTU1IiwidXNlcl9pZCI6MzAxfQ.yqxPkj2M4QOKYKWy1phLM4OzbxYTjjRQ44lOBplCZRw"
}
