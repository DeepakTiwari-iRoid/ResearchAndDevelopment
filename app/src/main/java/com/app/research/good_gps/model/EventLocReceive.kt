package com.app.research.good_gps.model

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class EventLocReceive(
    @SerializedName("type")
    val type: String = "",
    @SerializedName("eventName")
    val eventName: String = "",
    @SerializedName("socketId")
    val socketId: String = "",
    @SerializedName("user")
    val user: User = User(),
    @SerializedName("payload")
    val payload: Payload = Payload(),
    @SerializedName("timestamp")
    val timestamp: String = "",
    @SerializedName("message")
    val message: String = ""
)

data class User(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("email")
    val email: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("iat")
    val iat: Int = 0,
    @SerializedName("exp")
    val exp: Int = 0
)

data class Payload(
    @SerializedName("lat")
    val lat: String = "",
    @SerializedName("lng")
    val lng: String = "",
    @SerializedName("timestamp")
    val timestamp: String = "",
    @SerializedName("accuracy")
    val accuracy: Float = 0.0f,
    @SerializedName("speed")
    val speed: Float = 0.0f
){
    val latLng get() = LatLng(lat.toDouble(), lng.toDouble())
}