package com.app.research.skyview.data

import java.util.UUID

data class SkyTag(
    val id: String = UUID.randomUUID().toString(),
    val latitude: Double,
    val longitude: Double,
    val yaw: Float,
    val pitch: Float,
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
