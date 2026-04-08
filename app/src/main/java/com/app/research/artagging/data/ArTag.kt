package com.app.research.artagging.data

import java.util.UUID

data class ArTag(
    val id: String = UUID.randomUUID().toString(),
    val cloudAnchorId: String = "",
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    // Bearing (degrees 0-360) from the GPS position to the tag direction
    val bearing: Float = 0f,
    // Distance in meters from the user when placed (used for approximate re-placement)
    val distance: Float = 2f
)
