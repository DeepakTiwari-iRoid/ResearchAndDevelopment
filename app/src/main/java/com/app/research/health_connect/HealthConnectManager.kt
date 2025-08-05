package com.app.research.health_connect

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.changes.Change
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

// The minimum android level that can use Health Connect
const val MIN_SUPPORTED_SDK = Build.VERSION_CODES.O_MR1

/**
 * Demonstrates reading and writing from Health Connect.
 */
class HealthConnectManager(private val context: Context) {
    val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    var availability = mutableStateOf(HealthConnectAvailability.NOT_SUPPORTED)
        private set

    init {
        checkAvailability()
    }

    fun checkAvailability() {
        availability.value = when {
            HealthConnectClient.getSdkStatus(context) == SDK_AVAILABLE -> HealthConnectAvailability.INSTALLED
            isSupported() -> HealthConnectAvailability.NOT_INSTALLED
            else -> HealthConnectAvailability.NOT_SUPPORTED
        }
    }

    fun isFeatureAvailable(feature: Int): Boolean {
        return healthConnectClient
            .features
            .getFeatureStatus(feature) == HealthConnectFeatures.FEATURE_STATUS_AVAILABLE
    }


    /**
     * Determines whether all the specified permissions are already granted. It is recommended to
     * call [PermissionController.getGrantedPermissions] first in the permissions flow, as if the
     * permissions are already granted then there is no need to request permissions via
     * [PermissionController.createRequestPermissionResultContract].
     */
    suspend fun hasAllPermissions(permissions: Set<String>): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions()
            .containsAll(permissions)
    }

    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    suspend fun readWeightInputs(start: Instant, end: Instant): List<StepsRecord> {
        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(start, end)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }

    /**
     * TODO: Obtains a list of [ExerciseSessionRecord]s in a specified time frame. An Exercise Session Record is a
     * period of time given to an activity, that would make sense to a user, e.g. "Afternoon run"
     * etc. It does not necessarily mean, however, that the user was *running* for that entire time,
     * more that conceptually, this was the activity being undertaken.
     */
    suspend fun readExerciseSessions(start: Instant, end: Instant): List<ExerciseSessionRecord> {
        val request = ReadRecordsRequest(
            recordType = ExerciseSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(start, end)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }


    /**
     * TODO: Reads aggregated data and raw data for selected data types, for a given [ExerciseSessionRecord].
     */
    suspend fun readAssociatedSessionData(
        uid: String,
    ): ExerciseSessionData {
        val exerciseSession = healthConnectClient.readRecord(ExerciseSessionRecord::class, uid)
        // Use the start time and end time from the session, for reading raw and aggregate data.
        val timeRangeFilter = TimeRangeFilter.between(
            startTime = exerciseSession.record.startTime,
            endTime = exerciseSession.record.endTime
        )
        val aggregateDataTypes = setOf(
            ExerciseSessionRecord.EXERCISE_DURATION_TOTAL,
            TotalCaloriesBurnedRecord.ENERGY_TOTAL,
            HeartRateRecord.BPM_MAX,
            StepsRecord.COUNT_TOTAL,
        )
        // Limit the data read to just the application that wrote the session. This may or may not
        // be desirable depending on the use case: In some cases, it may be useful to combine with
        // data written by other apps.

        val aggregateRequest = AggregateRequest(
            metrics = aggregateDataTypes,
            timeRangeFilter = timeRangeFilter,
        )

        val aggregateData = healthConnectClient.aggregate(aggregateRequest)

        return ExerciseSessionData(
            uid = uid,
            totalActiveTime = aggregateData[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL],
            totalEnergyBurned = aggregateData[TotalCaloriesBurnedRecord.ENERGY_TOTAL],
            maxHeartRate = aggregateData[HeartRateRecord.BPM_MAX],
        )
    }


    suspend fun getExerciseData(
        healthConnectClient: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ): ExerciseSessionData {

        val timeRangeFilter = TimeRangeFilter.between(startTime, endTime)

        val aggregateDataTypes = setOf(
            ExerciseSessionRecord.EXERCISE_DURATION_TOTAL, // Active duration
            TotalCaloriesBurnedRecord.ENERGY_TOTAL,       // Calories burned
            HeartRateRecord.BPM_MAX,
            StepsRecord.COUNT_TOTAL// Max heart rate
        )

        val aggregateRequest = AggregateRequest(
            metrics = aggregateDataTypes,
            timeRangeFilter = timeRangeFilter
        )

        val aggregateData = healthConnectClient.aggregate(aggregateRequest)

        return ExerciseSessionData(
            totalActiveTime = aggregateData[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL],
            totalEnergyBurned = aggregateData[TotalCaloriesBurnedRecord.ENERGY_TOTAL],
            maxHeartRate = aggregateData[HeartRateRecord.BPM_MAX],
            totalSteps = aggregateData[StepsRecord.COUNT_TOTAL]
        )
    }

    suspend fun readStepsByTimeRange(
        startTime: Instant,
        endTime: Instant
    ): List<StepsRecord> {
        try {

            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                    dataOriginFilter = setOf(DataOrigin("com.google.android.apps.fitness"))
                )
            )
            Log.d(
                "HealthSession Mngr",
                "rec ${response.records.map { Triple(it.count, it.startTime, it.endTime) }}"
            )
            val uniqueOrigins = response.records.map { it.metadata.dataOrigin.packageName }.toSet()
            Log.d("HealthSession Mngr", "Available Data Origins: ${uniqueOrigins.toList()}")
            return response.records
        } catch (e: Exception) {
            // Run error handling here
            Log.d("HealthSession Mngr", e.localizedMessage ?: "something went wrong")
            return emptyList()
        }
    }


    suspend fun readAggregateStepsByTimeRange(
        startTime: Instant,
        endTime: Instant
    ): Long {
        try {

            val response = healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

            val stepResult = response[StepsRecord.COUNT_TOTAL] ?: 0L
            Log.d("HealthSession Mngr", "rec ${response.dataOrigins}")
            Log.d("HealthSession Mngr", "setResult Count Total Aggregate : ${stepResult}")
            return stepResult
        } catch (e: Exception) {
            // Run error handling here
            Log.d("HealthSession Mngr", e.localizedMessage ?: "something went wrong")
            return 0L
        }
    }


    suspend fun readExerciseDataByTimeRange(
        startTime: Instant,
        endTime: Instant
    ): ExerciseSessionData {
        try {
            val aggregateDataTypes = setOf(
                StepsRecord.COUNT_TOTAL,
                HeartRateRecord.BPM_MAX,
                HeartRateRecord.BPM_MIN,
                HeartRateRecord.BPM_AVG,
                HeartRateRecord.MEASUREMENTS_COUNT,
                TotalCaloriesBurnedRecord.ENERGY_TOTAL
            )

            val aggregateData = healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = aggregateDataTypes,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )


            val result = ExerciseSessionData(
                totalEnergyBurned = aggregateData[TotalCaloriesBurnedRecord.ENERGY_TOTAL],
                maxHeartRate = aggregateData[HeartRateRecord.BPM_MAX],
                minHeartRate = aggregateData[HeartRateRecord.BPM_MIN],
                avgHeartRate = aggregateData[HeartRateRecord.MEASUREMENTS_COUNT],
                totalSteps = aggregateData[StepsRecord.COUNT_TOTAL]
            )

            /* val stepResult = response[StepsRecord.COUNT_TOTAL] ?: 0L
             Log.d("HealthSession Mngr", "rec ${response.dataOrigins}")
             Log.d("HealthSession Mngr", "rec ${stepResult}")*/

            return result
        } catch (e: Exception) {
            // Run error handling here
            Log.d("HealthSession Mngr", e.localizedMessage ?: "something went wrong")
            return ExerciseSessionData(uid = "ERROR")
        }

    }


    /**
     * Convenience function to reuse code for reading data.
     */
    private suspend inline fun <reified T : Record> readData(
        timeRangeFilter: TimeRangeFilter,
        dataOriginFilter: Set<DataOrigin> = setOf(),
    ): List<T> {
        val request = ReadRecordsRequest(
            recordType = T::class,
            dataOriginFilter = dataOriginFilter,
            timeRangeFilter = timeRangeFilter
        )
        return healthConnectClient.readRecords(request).records
    }

    private fun isSupported() = Build.VERSION.SDK_INT >= MIN_SUPPORTED_SDK

    // Represents the two types of messages that can be sent in a Changes flow.
    sealed class ChangesMessage {
        data class NoMoreChanges(val nextChangesToken: String) : ChangesMessage()
        data class ChangeList(val changes: List<Change>) : ChangesMessage()
    }
}

/**
 * Health Connect requires that the underlying Health Connect APK is installed on the device.
 * [HealthConnectAvailability] represents whether this APK is indeed installed, whether it is not
 * installed but supported on the device, or whether the device is not supported (based on Android
 * version).
 */
enum class HealthConnectAvailability {
    INSTALLED,
    NOT_INSTALLED,
    NOT_SUPPORTED
}