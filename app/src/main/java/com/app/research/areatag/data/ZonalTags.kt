package com.app.research.areatag.data


import com.google.gson.annotations.SerializedName
import java.util.UUID

data class Zone(
    @SerializedName("zoneId")
    val zoneId: String = "",
    @SerializedName("title")
    val title: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("createdAt")
    val createdAt: Long = 0L,
    @SerializedName("tags")
    val tags: List<Tag> = emptyList()
) {
    data class Tag(
        @SerializedName("uuid")
        val uuid: String = UUID.randomUUID().toString(),
        @SerializedName("id")
        val id: Int = 0,
        @SerializedName("lat")
        val latitude: Double = 0.0,
        @SerializedName("log")
        val longitude: Double = 0.0,
        @SerializedName("yaw")
        val yaw: Float = 0f,
        @SerializedName("pitch")
        val pitch: Float = 0f,
        @SerializedName("title")
        val title: String = "",
        @SerializedName("description")
        val description: String = "",
        @SerializedName("createdAt")
        val createdAt: String = ""
    )
}