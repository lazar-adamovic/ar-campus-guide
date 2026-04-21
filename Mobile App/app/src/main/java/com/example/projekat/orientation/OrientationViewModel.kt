package com.example.projekat.orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs


class OrientationViewModel : ViewModel() {
    private var sensorManager: SensorManager? = null
    private var rotationSensor: Sensor? = null
    private var sensorEventListener: SensorEventListener? = null
    private val _azimut = MutableStateFlow(0f)
    val azimut: StateFlow<Float> = _azimut
    private val _isVertical = MutableStateFlow(false)
    val isVertical: StateFlow<Boolean> = _isVertical
    private var lastFilteredAzimut = 0f
    fun start(context: Context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    val remappedMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_X,
                        SensorManager.AXIS_Z,
                        remappedMatrix
                    )
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(remappedMatrix, orientation)
                    val az = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    val pit = Math.toDegrees(orientation[1].toDouble()).toFloat()
                    _isVertical.value = abs(pit) < 50
                    val normalizedAz = (az + 360) % 360
                    if (lastFilteredAzimut == 0f) lastFilteredAzimut = normalizedAz
                    val stabilizedAzimut = headingWrapFilter(normalizedAz)
                    _azimut.value = stabilizedAzimut
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        rotationSensor?.let {
            sensorManager?.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }
    fun stop() {
        sensorEventListener?.let { sensorManager?.unregisterListener(it) }
        sensorEventListener = null
    }
    private fun headingWrapFilter(currentAzimut: Float): Float {
        val alpha = 0.15f
        var delta = currentAzimut - lastFilteredAzimut
        if (delta > 180) delta -= 360 else if (delta < -180) delta += 360
        val newFilteredAzimut = lastFilteredAzimut + delta * alpha
        lastFilteredAzimut = (newFilteredAzimut + 360) % 360
        return lastFilteredAzimut
    }
}

