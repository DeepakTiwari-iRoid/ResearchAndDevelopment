package com.app.research.skyview.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class Orientation(
    val yaw: Float = 0f,    // azimuth: 0-360 degrees
    val pitch: Float = 0f,  // -90 (down) to +90 (up)
    val roll: Float = 0f
)

class OrientationManager(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    companion object {
        private const val FILTER_ALPHA = 0.15f
    }

    fun observeOrientation(): Flow<Orientation> = callbackFlow {
        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)

        var filteredYaw = Float.NaN
        var filteredPitch = Float.NaN
        var filteredRoll = Float.NaN

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                // Remap for phone held upright in portrait mode
                val remappedMatrix = FloatArray(9)
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Z,
                    remappedMatrix
                )

                SensorManager.getOrientation(remappedMatrix, orientationAngles)

                val rawYaw = ((Math.toDegrees(orientationAngles[0].toDouble()).toFloat() + 360f) % 360f)
                val rawPitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
                val rawRoll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

                if (filteredYaw.isNaN()) {
                    filteredYaw = rawYaw
                    filteredPitch = rawPitch
                    filteredRoll = rawRoll
                } else {
                    filteredYaw = lowPassAngle(filteredYaw, rawYaw, FILTER_ALPHA)
                    filteredPitch = lowPass(filteredPitch, rawPitch, FILTER_ALPHA)
                    filteredRoll = lowPass(filteredRoll, rawRoll, FILTER_ALPHA)
                }

                trySend(Orientation(filteredYaw, filteredPitch, filteredRoll))
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val success = rotationSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
        } ?: false

        if (!success) {
            close(IllegalStateException("Rotation vector sensor not available"))
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    /** Standard low-pass filter */
    private fun lowPass(filtered: Float, raw: Float, alpha: Float): Float {
        return filtered + alpha * (raw - filtered)
    }

    /** Low-pass filter that handles 0/360 wraparound for angles */
    private fun lowPassAngle(filtered: Float, raw: Float, alpha: Float): Float {
        var delta = raw - filtered
        // Handle wraparound
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        val result = filtered + alpha * delta
        return (result + 360f) % 360f
    }
}
