package com.example.ui.ar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.domain.camera.TorchManager
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldLocked
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@Composable
fun CameraPreviewView(
    hasCameraPermission: Boolean,
    torchManager: TorchManager,
    cameraZoomRatio: Float,
    onCameraBound: ((CameraControl, CameraInfo) -> Unit)? = null,
    onImageCaptureReady: ((ImageCapture) -> Unit)? = null,
    onCompositeFrameProvider: ((() -> Bitmap?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current

    var activeCameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var activeCameraInfo by remember { mutableStateOf<CameraInfo?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    // Tap-to-focus animation states
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    var showFocusRing by remember { mutableStateOf(false) }

    LaunchedEffect(showFocusRing) {
        if (showFocusRing) {
            delay(1500)
            showFocusRing = false
        }
    }

    // Apply camera zoom whenever ratio changes
    LaunchedEffect(cameraZoomRatio, activeCameraControl) {
        val control = activeCameraControl ?: return@LaunchedEffect
        try {
            control.setZoomRatio(cameraZoomRatio.coerceIn(0.5f, 15.0f))
        } catch (e: Exception) {
            Log.w("CameraPreviewView", "Zoom ratio application failed: ${e.message}")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val view = previewViewRef ?: return@detectTapGestures
                    val control = activeCameraControl ?: return@detectTapGestures

                    focusPoint = tapOffset
                    showFocusRing = true

                    try {
                        val factory = view.meteringPointFactory
                        val point = factory.createPoint(tapOffset.x, tapOffset.y)
                        val action = FocusMeteringAction.Builder(
                            point,
                            FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                        ).setAutoCancelDuration(3, TimeUnit.SECONDS).build()

                        control.startFocusAndMetering(action)
                    } catch (e: Exception) {
                        Log.e("CameraPreviewView", "Tap-to-focus error", e)
                    }
                }
            }
    ) {
        if (hasCameraPermission) {
            val previewView = remember {
                PreviewView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            }

            DisposableEffect(lifecycleOwner) {
                previewViewRef = previewView
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                onImageCaptureReady?.invoke(imageCapture)

                // Expose bitmap snapshot provider for live composite recording
                onCompositeFrameProvider?.invoke {
                    try {
                        previewView.bitmap
                    } catch (e: Exception) {
                        null
                    }
                }

                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )

                        activeCameraControl = camera.cameraControl
                        activeCameraInfo = camera.cameraInfo
                        torchManager.bindCameraControl(camera.cameraControl)
                        onCameraBound?.invoke(camera.cameraControl, camera.cameraInfo)
                    } catch (exc: Exception) {
                        Log.e("CameraPreviewView", "Use case binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(context))

                onDispose {
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        cameraProvider.unbindAll()
                        torchManager.bindCameraControl(null)
                        activeCameraControl = null
                        activeCameraInfo = null
                        previewViewRef = null
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }

            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Textured drawing desk backdrop when camera permission is not yet granted
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF262F40),
                                Color(0xFF141923),
                                Color(0xFF090D16)
                            )
                        )
                    )
            )
        }

        // Tap-to-Focus glowing HUD ring
        AnimatedVisibility(
            visible = showFocusRing && focusPoint != null,
            enter = fadeIn() + scaleIn(initialScale = 1.4f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
            modifier = Modifier.fillMaxSize()
        ) {
            focusPoint?.let { pt ->
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (pt.x - with(density) { 32.dp.toPx() }).roundToInt(),
                                (pt.y - with(density) { 32.dp.toPx() }).roundToInt()
                            )
                        }
                        .size(64.dp)
                        .border(2.dp, EmeraldLocked, CircleShape)
                )
            }
        }
    }
}
