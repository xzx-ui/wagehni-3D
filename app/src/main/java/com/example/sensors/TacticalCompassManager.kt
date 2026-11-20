package com.example.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class CompassTelemetry(
    val azimuthDegrees: Float = 0f,
    val cardinalArabic: String = "شمال",
    val cardinalCode: String = "N",
    val isHardwareSensorActive: Boolean = false,
    val magneticFieldStrengthUt: Float = 45.2f
)

class TacticalCompassManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    // Circular low-pass smoothing state (sin/cos components prevent 359° -> 0° wrap-around jump)
    private var smoothedSin = 0f
    private var smoothedCos = 1f
    private val smoothingAlpha = 0.14f

    private var manualOverrideActive = false

    private val _telemetry = MutableStateFlow(
        CompassTelemetry(
            azimuthDegrees = 18f,
            cardinalArabic = getCardinalArabic(18f),
            cardinalCode = getCardinalCode(18f),
            isHardwareSensorActive = (accelerometer != null && magnetometer != null)
        )
    )
    val telemetry: StateFlow<CompassTelemetry> = _telemetry.asStateFlow()

    fun startListening() {
        val acc = accelerometer
        val mag = magnetometer
        if (acc != null && mag != null) {
            sensorManager?.registerListener(this, acc, SensorManager.SENSOR_DELAY_UI)
            sensorManager?.registerListener(this, mag, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    fun setSimulatedAzimuth(degrees: Float) {
        manualOverrideActive = true
        val normalized = ((degrees % 360f) + 360f) % 360f
        _telemetry.value = _telemetry.value.copy(
            azimuthDegrees = normalized,
            cardinalArabic = getCardinalArabic(normalized),
            cardinalCode = getCardinalCode(normalized)
        )
    }

    fun resumeHardwareSensor() {
        manualOverrideActive = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || manualOverrideActive) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, gravity, 0, 3)
                hasGravity = true
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                hasGeomagnetic = true
            }
        }

        if (hasGravity && hasGeomagnetic) {
            val rotationMatrix = FloatArray(9)
            val inclinationMatrix = FloatArray(9)
            val success = SensorManager.getRotationMatrix(
                rotationMatrix,
                inclinationMatrix,
                gravity,
                geomagnetic
            )
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val rawRad = orientation[0]

                // Azimuth Smoothing via circular exponential moving average
                smoothedSin = smoothedSin + smoothingAlpha * (sin(rawRad) - smoothedSin)
                smoothedCos = smoothedCos + smoothingAlpha * (cos(rawRad) - smoothedCos)
                val smoothedRad = atan2(smoothedSin, smoothedCos)
                val deg = ((Math.toDegrees(smoothedRad.toDouble()).toFloat() + 360f) % 360f)

                val magStrength = kotlin.math.sqrt(
                    geomagnetic[0] * geomagnetic[0] +
                        geomagnetic[1] * geomagnetic[1] +
                        geomagnetic[2] * geomagnetic[2]
                )

                _telemetry.value = CompassTelemetry(
                    azimuthDegrees = deg,
                    cardinalArabic = getCardinalArabic(deg),
                    cardinalCode = getCardinalCode(deg),
                    isHardwareSensorActive = true,
                    magneticFieldStrengthUt = (magStrength * 10f).roundToInt() / 10f
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    companion object {
        fun getCardinalArabic(degrees: Float): String {
            val d = ((degrees % 360f) + 360f) % 360f
            return when {
                d >= 337.5f || d < 22.5f -> "شمال"
                d < 67.5f -> "شمال شرق"
                d < 112.5f -> "شرق"
                d < 157.5f -> "جنوب شرق"
                d < 202.5f -> "جنوب"
                d < 247.5f -> "جنوب غرب"
                d < 292.5f -> "غرب"
                else -> "شمال غرب"
            }
        }

        fun getCardinalCode(degrees: Float): String {
            val d = ((degrees % 360f) + 360f) % 360f
            return when {
                d >= 337.5f || d < 22.5f -> "N"
                d < 67.5f -> "NE"
                d < 112.5f -> "E"
                d < 157.5f -> "SE"
                d < 202.5f -> "S"
                d < 247.5f -> "SW"
                d < 292.5f -> "W"
                else -> "NW"
            }
        }
    }
}
