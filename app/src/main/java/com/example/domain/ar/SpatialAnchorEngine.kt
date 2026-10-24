package com.example.domain.ar

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class SpatialState(
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val yaw: Float = 0f,
    val qx: Float = 0f,
    val qy: Float = 0f,
    val qz: Float = 0f,
    val qw: Float = 1f,
    val isLeveled: Boolean = false,
    val levelAngleDeviation: Float = 0f,
    // Spatial 3D World Drift-Free Counter-Transformations
    val deltaOffsetX: Float = 0f,
    val deltaOffsetY: Float = 0f,
    val deltaRotation: Float = 0f,
    val relativeDistanceZ: Float = 1.0f,
    val isShakeDetected: Boolean = false,
    val isAnchorLocked: Boolean = false,
    val featurePoints: List<Offset> = emptyList()
)

class SpatialAnchorEngine(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
    private val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var currentPitch = 0f
    private var currentRoll = 0f
    private var currentYaw = 0f
    private var currentQx = 0f
    private var currentQy = 0f
    private var currentQz = 0f
    private var currentQw = 1f

    // 3D Rotation Matrices for zero gimbal-lock at any angle
    private val currentMatrix = FloatArray(9) { if (it % 4 == 0) 1f else 0f }
    private val anchorMatrix = FloatArray(9) { if (it % 4 == 0) 1f else 0f }
    private val relMatrix = FloatArray(9)

    // Pinned 3D World Anchor
    private var isAnchorLocked = false
    private var anchorPitch = 0f
    private var anchorRoll = 0f
    private var anchorYaw = 0f

    // Low-pass filtered translation offsets and anti-shake dampening
    private var smoothDeltaX = 0f
    private var smoothDeltaY = 0f
    private var smoothDeltaRot = 0f

    // Linear translation tracking from accelerometer
    private var linearVelX = 0f
    private var linearVelY = 0f
    private var linearTransX = 0f
    private var linearTransY = 0f
    private var isShakeDetected = false

    // Distance estimation along Z axis
    private var estimatedDistance = 1.0f

    // Camera simulated focal length for desk paper projection (~1380 px)
    private val focalLength = 1380.0f

    // Mode: Constant Fixed Dimensions (True 1:1 Size Invariant when moving phone near/far)
    var isFixedDimensionLockEnabled = true

    var onSpatialUpdate: ((SpatialState) -> Unit)? = null

    private val basePoints = listOf(
        Offset(0.25f, 0.35f),
        Offset(0.72f, 0.28f),
        Offset(0.50f, 0.52f),
        Offset(0.33f, 0.70f),
        Offset(0.68f, 0.76f),
        Offset(0.18f, 0.62f),
        Offset(0.82f, 0.44f),
        Offset(0.42f, 0.22f),
        Offset(0.58f, 0.84f),
        Offset(0.14f, 0.25f),
        Offset(0.86f, 0.70f),
        Offset(0.48f, 0.38f)
    )

    fun startListening() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        accelSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    fun lockCurrentAnchor() {
        isAnchorLocked = true
        System.arraycopy(currentMatrix, 0, anchorMatrix, 0, 9)
        anchorPitch = currentPitch
        anchorRoll = currentRoll
        anchorYaw = currentYaw
        smoothDeltaX = 0f
        smoothDeltaY = 0f
        smoothDeltaRot = 0f
        linearVelX = 0f
        linearVelY = 0f
        linearTransX = 0f
        linearTransY = 0f
        estimatedDistance = 1.0f
    }

    fun unlockAnchor() {
        isAnchorLocked = false
        smoothDeltaX = 0f
        smoothDeltaY = 0f
        smoothDeltaRot = 0f
        linearVelX = 0f
        linearVelY = 0f
        linearTransX = 0f
        linearTransY = 0f
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(currentMatrix, event.values)

            if (event.values.size >= 4) {
                currentQx = event.values[0]
                currentQy = event.values[1]
                currentQz = event.values[2]
                currentQw = event.values[3]
            }

            val orientationAngles = FloatArray(3)
            SensorManager.getOrientation(currentMatrix, orientationAngles)

            currentYaw = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            currentPitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
            currentRoll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

            val angleDeviation = sqrt((currentPitch * currentPitch + currentRoll * currentRoll).toDouble()).toFloat()
            val isLeveled = angleDeviation < 4.5f

            var calculatedDeltaX = 0f
            var calculatedDeltaY = 0f
            var calculatedDeltaRot = 0f

            if (isAnchorLocked) {
                // Exact 3D rotation compensation using Rel = Current * Anchor^T
                // Avoids Euler angle gimbal lock when phone is held horizontally flat over paper!
                // Rel[i, j] = sum_k (Current[i, k] * Anchor[j, k])
                for (i in 0 until 3) {
                    for (j in 0 until 3) {
                        relMatrix[i * 3 + j] = currentMatrix[i * 3 + 0] * anchorMatrix[j * 3 + 0] +
                                              currentMatrix[i * 3 + 1] * anchorMatrix[j * 3 + 1] +
                                              currentMatrix[i * 3 + 2] * anchorMatrix[j * 3 + 2]
                    }
                }

                val r02 = relMatrix[2]
                val r12 = relMatrix[5]
                val r22 = relMatrix[8].coerceAtLeast(0.15f)

                // Precise perspective displacement
                val targetDeltaX = -(r02 / r22) * focalLength + linearTransX
                val targetDeltaY = (r12 / r22) * focalLength + linearTransY

                // In-plane roll rotation
                val targetDeltaRot = -Math.toDegrees(atan2(relMatrix[1].toDouble(), relMatrix[0].toDouble())).toFloat()

                // Anti-Shake Dynamic Stability Filter
                // If phone is vigorously shaken, increase damping to prevent anchor jitter
                val alpha = if (isShakeDetected) 0.15f else 0.45f

                // Deadband threshold to eliminate sensor micro-tremors (< 0.5 pixel)
                val diffX = targetDeltaX - smoothDeltaX
                val diffY = targetDeltaY - smoothDeltaY
                val diffR = targetDeltaRot - smoothDeltaRot

                smoothDeltaX = if (abs(diffX) < 0.4f) targetDeltaX else smoothDeltaX + alpha * diffX
                smoothDeltaY = if (abs(diffY) < 0.4f) targetDeltaY else smoothDeltaY + alpha * diffY
                smoothDeltaRot = if (abs(diffR) < 0.2f) targetDeltaRot else smoothDeltaRot + alpha * diffR

                calculatedDeltaX = smoothDeltaX
                calculatedDeltaY = smoothDeltaY
                calculatedDeltaRot = smoothDeltaRot
            }

            val jitterX = sin(currentYaw * 0.05f) * 0.015f
            val jitterY = cos(currentPitch * 0.05f) * 0.015f
            val dynamicPoints = basePoints.map {
                Offset(
                    (it.x + jitterX).coerceIn(0.05f, 0.95f),
                    (it.y + jitterY).coerceIn(0.05f, 0.95f)
                )
            }

            onSpatialUpdate?.invoke(
                SpatialState(
                    pitch = currentPitch,
                    roll = currentRoll,
                    yaw = currentYaw,
                    qx = currentQx,
                    qy = currentQy,
                    qz = currentQz,
                    qw = currentQw,
                    isLeveled = isLeveled,
                    levelAngleDeviation = angleDeviation,
                    deltaOffsetX = calculatedDeltaX,
                    deltaOffsetY = calculatedDeltaY,
                    deltaRotation = calculatedDeltaRot,
                    relativeDistanceZ = estimatedDistance,
                    isShakeDetected = isShakeDetected,
                    isAnchorLocked = isAnchorLocked,
                    featurePoints = dynamicPoints
                )
            )
        } else if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION || event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val ax = event.values[0]
            val ay = event.values[1]
            val az = event.values[2]
            val accelNorm = sqrt((ax * ax + ay * ay + az * az).toDouble()).toFloat()
            isShakeDetected = accelNorm > 12.0f

            if (isAnchorLocked) {
                // Integrate lateral hand shift with friction decay to compensate phone displacement
                linearVelX = (linearVelX + ax * 0.018f) * 0.86f
                linearVelY = (linearVelY + ay * 0.018f) * 0.86f
                linearTransX = (linearTransX - linearVelX * 140f) * 0.94f
                linearTransY = (linearTransY + linearVelY * 140f) * 0.94f

                // Micro-depth Z tracking if dynamic perspective enabled
                if (!isFixedDimensionLockEnabled) {
                    val zShift = (az * 0.005f)
                    estimatedDistance = (estimatedDistance + zShift).coerceIn(0.4f, 3.0f)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
