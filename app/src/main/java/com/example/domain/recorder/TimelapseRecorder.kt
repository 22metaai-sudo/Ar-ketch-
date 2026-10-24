package com.example.domain.recorder

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class TimelapseState(
    val isRecording: Boolean = false,
    val elapsedSeconds: Int = 0,
    val frameCount: Int = 0,
    val intervalSeconds: Int = 2,
    val currentSessionDir: String? = null,
    val latestFramePath: String? = null
)

class TimelapseRecorder(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val _state = MutableStateFlow(TimelapseState())
    val state: StateFlow<TimelapseState> = _state.asStateFlow()

    private var recordJob: Job? = null
    private var currentFolder: File? = null
    private var frameCounter = 0
    private var startTimeMillis = 0L

    var frameCaptureProvider: (() -> Bitmap?)? = null

    fun startRecording(intervalSeconds: Int = 2): Boolean {
        if (_state.value.isRecording) return false

        val folderName = "timelapse_${System.currentTimeMillis()}"
        val folder = File(context.filesDir, "timelapses/$folderName").apply { mkdirs() }
        currentFolder = folder
        frameCounter = 0
        startTimeMillis = System.currentTimeMillis()

        _state.value = TimelapseState(
            isRecording = true,
            elapsedSeconds = 0,
            frameCount = 0,
            intervalSeconds = intervalSeconds,
            currentSessionDir = folder.absolutePath
        )

        recordJob = coroutineScope.launch(Dispatchers.IO) {
            while (isActive) {
                captureFrame()
                val elapsed = ((System.currentTimeMillis() - startTimeMillis) / 1000).toInt()
                _state.value = _state.value.copy(
                    elapsedSeconds = elapsed,
                    frameCount = frameCounter
                )
                delay(intervalSeconds * 1000L)
            }
        }
        return true
    }

    private fun captureFrame() {
        try {
            val bitmap = frameCaptureProvider?.invoke() ?: return
            val folder = currentFolder ?: return
            frameCounter++
            val frameFile = File(folder, String.format("frame_%04d.jpg", frameCounter))
            FileOutputStream(frameFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            _state.value = _state.value.copy(
                frameCount = frameCounter,
                latestFramePath = frameFile.absolutePath
            )
        } catch (e: Exception) {
            Log.e("TimelapseRecorder", "Error capturing timelapse frame", e)
        }
    }

    fun stopRecording(): TimelapseState {
        recordJob?.cancel()
        recordJob = null
        val finalState = _state.value.copy(isRecording = false)
        _state.value = finalState
        return finalState
    }

    fun getSessionFrames(folderPath: String): List<File> {
        val dir = File(folderPath)
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        return dir.listFiles { file -> file.extension.lowercase() == "jpg" }
            ?.sortedBy { it.name } ?: emptyList()
    }
}
