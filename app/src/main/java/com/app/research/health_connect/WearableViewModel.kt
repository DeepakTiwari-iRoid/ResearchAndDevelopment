package com.app.research.health_connect

import android.os.RemoteException
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.ZonedDateTime
import java.util.UUID

class WearableViewModel(
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {
    val permissions = setOf(
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
    )

    private var startSession: ZonedDateTime = ZonedDateTime.now()
    private var endSession: ZonedDateTime = ZonedDateTime.now()

    var permissionsGranted = mutableStateOf(false)
        private set

    var uiState: UiState by mutableStateOf(UiState.Uninitialized)
        private set

    var steps: MutableState<List<StepsRecord>> = mutableStateOf(emptyList())
        private set


    val permissionsLauncher = healthConnectManager.requestPermissionsActivityContract()


    fun startStopSession() {
        viewModelScope.launch(Dispatchers.Main) {

            tryWithPermissionsCheck {
                endSession = ZonedDateTime.now()
                startSession = endSession.minusHours(1)

                Log.d("HealthSession", "Time ${startSession.toInstant()} ${endSession.toInstant()}")

                val step = healthConnectManager.readStepsByTimeRange(
                    startTime = startSession.toInstant(),
                    endTime = endSession.toInstant()
                )


                steps.value = step

                Log.d("HealthSession", "Final Values: ${step.map { it.count } } ")

            }
        }
    }

    private suspend fun tryWithPermissionsCheck(block: suspend () -> Unit) {
        permissionsGranted.value = healthConnectManager.hasAllPermissions(permissions)
        uiState = try {
            if (permissionsGranted.value) {
                block()
            }
            UiState.Done
        } catch (remoteException: RemoteException) {
            UiState.Error(remoteException)
        } catch (securityException: SecurityException) {
            UiState.Error(securityException)
        } catch (ioException: IOException) {
            UiState.Error(ioException)
        } catch (illegalStateException: IllegalStateException) {
            UiState.Error(illegalStateException)
        }
    }


    suspend fun getData(healthConnectClient: HealthConnectClient): ExerciseSessionData {
        val response = healthConnectManager.getExerciseData(
            healthConnectClient,
            startSession.toInstant(),
            endSession.toInstant()
        )
        return response // Return the UID to track this session
    }

}

sealed class UiState {
    object Uninitialized : UiState()
    object Done : UiState()

    // A random UUID is used in each Error object to allow errors to be uniquely identified,
    // and recomposition won't result in multiple snackbars.
    data class Error(val exception: Throwable, val uuid: UUID = UUID.randomUUID()) : UiState()
}


class WearableViewModelFactory(
    private val healthConnectManager: HealthConnectManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(WearableViewModel::class.java)) {
            return WearableViewModelFactory(
                healthConnectManager = healthConnectManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}