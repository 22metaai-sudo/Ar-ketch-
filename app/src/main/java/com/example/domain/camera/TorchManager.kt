package com.example.domain.camera

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.camera.core.CameraControl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TorchManager(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var backCameraId: String? = null

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    private val _isStrobeOn = MutableStateFlow(false)
    val isStrobeOn: StateFlow<Boolean> = _isStrobeOn.asStateFlow()

    private val _strobeFrequencyHz = MutableStateFlow(3)
    val strobeFrequencyHz: StateFlow<Int> = _strobeFrequencyHz.asStateFlow()

    private var activeCameraControl: CameraControl? = null
    private var strobeJob: Job? = null

    init {
        try {
            cameraManager?.cameraIdList?.forEach { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                if (facing == CameraCharacteristics.LENS_FACING_BACK && hasFlash) {
                    backCameraId = id
                    return@forEach
                }
            }
        } catch (e: Exception) {
            Log.w("TorchManager", "Unable to detect back camera flash", e)
        }
    }

    fun bindCameraControl(cameraControl: CameraControl?) {
        this.activeCameraControl = cameraControl
    }

    fun setTorchEnabled(enabled: Boolean) {
        _isTorchOn.value = enabled

        // Method 1: CameraX CameraControl if bound
        val control = activeCameraControl
        if (control != null) {
            try {
                control.enableTorch(enabled)
                return
            } catch (e: Exception) {
                Log.w("TorchManager", "CameraControl torch toggle failed", e)
            }
        }

        // Method 2: System CameraManager
        val camId = backCameraId
        if (cameraManager != null && camId != null) {
            try {
                cameraManager.setTorchMode(camId, enabled)
            } catch (e: CameraAccessException) {
                Log.w("TorchManager", "CameraManager setTorchMode error", e)
            } catch (e: Exception) {
                Log.w("TorchManager", "Flashlight hardware error", e)
            }
        }
    }

    fun toggleTorch(): Boolean {
        stopStrobe()
        val next = !_isTorchOn.value
        setTorchEnabled(next)
        return next
    }

    fun setStrobeFrequency(hz: Int) {
        _strobeFrequencyHz.value = hz.coerceIn(1, 15)
    }

    fun toggleStrobe(coroutineScope: CoroutineScope): Boolean {
        val nextState = !_isStrobeOn.value
        _isStrobeOn.value = nextState

        if (nextState) {
            strobeJob?.cancel()
            strobeJob = coroutineScope.launch(Dispatchers.IO) {
                while (isActive) {
                    val halfPeriodMs = (1000L / (_strobeFrequencyHz.value * 2)).coerceAtLeast(40L)
                    setTorchEnabled(true)
                    delay(halfPeriodMs)
                    setTorchEnabled(false)
                    delay(halfPeriodMs)
                }
            }
        } else {
            stopStrobe()
        }
        return nextState
    }

    private fun stopStrobe() {
        strobeJob?.cancel()
        strobeJob = null
        _isStrobeOn.value = false
        setTorchEnabled(false)
    }

    fun release() {
        stopStrobe()
        if (_isTorchOn.value) {
            setTorchEnabled(false)
        }
        activeCameraControl = null
    }
}
