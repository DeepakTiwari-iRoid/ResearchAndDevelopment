package com.app.research.health_connect

import androidx.health.connect.client.units.Energy
import java.time.Duration


/**
 * Represents data, both aggregated and raw, associated with a single exercise session. Used to
 * collate results from aggregate and raw reads from Health Connect in one object.
 */
data class ExerciseSessionData(
    val uid: String = "",
    val totalActiveTime: Duration? = null,
    val totalEnergyBurned: Energy? = null,
    val maxHeartRate: Long? = null,
    val minHeartRate: Long? = null,
    val avgHeartRate: Long? = null,
    val totalSteps: Long? = null,
)
