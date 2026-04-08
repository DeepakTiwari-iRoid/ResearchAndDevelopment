package com.app.research.arsample

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.Matrix

/**
 * Provides a 4x4 rotation matrix from the device's rotation-vector sensor.
 * The matrix can be fed directly into OpenGL as the view/camera matrix.
 */
class SensorOrientationManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    /** Current 4x4 rotation matrix (column-major, OpenGL-ready). */
    val rotationMatrix = FloatArray(16).also { Matrix.setIdentityM(it, 0) }

    private val tempMatrix = FloatArray(16)
    private val remapMatrix = FloatArray(16)
    private val invertedMatrix = FloatArray(16)

    fun start() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        SensorManager.getRotationMatrixFromVector(tempMatrix, event.values)

        // Remap: device X -> world X, device Z -> world Y
        // This makes "phone upright in portrait" look at the horizon.
        SensorManager.remapCoordinateSystem(
            tempMatrix,
            SensorManager.AXIS_X,
            SensorManager.AXIS_Z,
            remapMatrix
        )

        // Transpose (= inverse for rotation matrices) so that the camera
        // follows the device naturally: tilt up → view goes up, rotate right → view goes right.
        Matrix.transposeM(invertedMatrix, 0, remapMatrix, 0)

        synchronized(rotationMatrix) {
            System.arraycopy(invertedMatrix, 0, rotationMatrix, 0, 16)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}