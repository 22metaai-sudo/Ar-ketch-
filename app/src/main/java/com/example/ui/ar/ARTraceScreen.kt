package com.example.ui.ar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.DrawingProject
import com.example.data.model.FilterMode
import com.example.data.model.TimelapseSession
import com.example.data.repository.DrawingRepository
import com.example.domain.ar.SpatialAnchorEngine
import com.example.domain.ar.SpatialState
import com.example.domain.camera.TorchManager
import com.example.domain.image.EdgeDetectionProcessor
import com.example.domain.recorder.TimelapseRecorder
import com.example.ui.theme.AmberCaution
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldLocked
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.RubyRecording
import com.example.ui.theme.SurfaceElevated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARTraceScreen(
    imageSource: String,
    initialTitle: String,
    repository: DrawingRepository,
    existingProject: DrawingProject? = null,
    onNavigateBack: () -> Unit,
    onNavigateToGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State properties initialized from existing project if resuming
    var isLocked by remember { mutableStateOf(existingProject?.isLocked ?: false) }
    var scale by remember { mutableFloatStateOf(existingProject?.scale ?: 1.0f) }
    var rotation by remember { mutableFloatStateOf(existingProject?.rotation ?: 0.0f) }
    var offset by remember { mutableStateOf(Offset(existingProject?.offsetX ?: 0f, existingProject?.offsetY ?: 0f)) }
    var opacity by remember { mutableFloatStateOf(existingProject?.opacity ?: 0.65f) }
    var filterMode by remember {
        mutableStateOf(
            existingProject?.filterMode?.let {
                try { FilterMode.valueOf(it) } catch (e: Exception) { FilterMode.NORMAL }
            } ?: FilterMode.NORMAL
        )
    }
    var edgeThreshold by remember { mutableIntStateOf(existingProject?.edgeThreshold ?: 45) }
    var isFlippedHorizontal by remember { mutableStateOf(existingProject?.isFlippedHorizontal ?: false) }
    var isFlippedVertical by remember { mutableStateOf(existingProject?.isFlippedVertical ?: false) }
    var showGrid by remember { mutableStateOf(existingProject?.showGrid ?: false) }
    var gridSize by remember { mutableIntStateOf(existingProject?.gridSize ?: 3) }
    var gridColorHex by remember { mutableStateOf(existingProject?.gridColorHex ?: 0x4400F2FE) }
    var cropRatio by remember { mutableStateOf(existingProject?.cropRatio ?: "ORIGINAL") }

    // Camera Zoom (Grand angle 0.5x, 1x, etc.)
    var cameraZoomRatio by remember { mutableFloatStateOf(existingProject?.cameraZoomRatio ?: 1.0f) }

    // Viewport Screen Zoom (Loupe / Inspection Macro when locked)
    var viewportZoom by remember { mutableFloatStateOf(existingProject?.viewportZoom ?: 1.0f) }
    var viewportOffset by remember {
        mutableStateOf(Offset(existingProject?.viewportOffsetX ?: 0f, existingProject?.viewportOffsetY ?: 0f))
    }

    // Floating Circular Loupe Lens (Microscope / Telescope Macro)
    var isLoupeActive by remember { mutableStateOf(false) }
    var loupeZoom by remember { mutableFloatStateOf(4.0f) }
    var loupePosition by remember { mutableStateOf(Offset(300f, 400f)) }

    // Camera Permissions
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Hardware Managers
    val torchManager = remember { TorchManager(context) }
    val isTorchOn by torchManager.isTorchOn.collectAsState()
    val isStrobeOn by torchManager.isStrobeOn.collectAsState()

    val spatialAnchorEngine = remember { SpatialAnchorEngine(context) }
    var spatialState by remember { mutableStateOf(SpatialState()) }

    val timelapseRecorder = remember { TimelapseRecorder(context, coroutineScope) }
    val timelapseState by timelapseRecorder.state.collectAsState()

    var activeImageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var liveCompositeSnapshotProvider by remember { mutableStateOf<(() -> Bitmap?)?>(null) }

    // Sensor lifecycle
    DisposableEffect(Unit) {
        spatialAnchorEngine.onSpatialUpdate = { updatedState ->
            spatialState = updatedState
        }
        spatialAnchorEngine.startListening()

        onDispose {
            spatialAnchorEngine.stopListening()
            torchManager.release()
            if (timelapseState.isRecording) {
                timelapseRecorder.stopRecording()
            }
        }
    }

    // Bitmaps
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // UI Panels toggles
    var showFilterSheet by remember { mutableStateOf(false) }
    var showGridSheet by remember { mutableStateOf(false) }
    var showZoomPanel by remember { mutableStateOf(false) }
    var showOpacitySlider by remember { mutableStateOf(false) }

    // Load original bitmap
    LaunchedEffect(imageSource) {
        withContext(Dispatchers.IO) {
            val bmp = loadBitmap(context, imageSource)
            originalBitmap = bmp
            processedBitmap = bmp
        }
    }

    // Apply filters
    LaunchedEffect(filterMode, edgeThreshold, originalBitmap) {
        val orig = originalBitmap ?: return@LaunchedEffect
        if (filterMode == FilterMode.SOBEL_EDGES || filterMode == FilterMode.BINARIZED || filterMode == FilterMode.BELLE_VUE || filterMode == FilterMode.SKETCH_PENCIL) {
            val filtered = EdgeDetectionProcessor.processBitmap(
                source = orig,
                mode = filterMode,
                threshold = edgeThreshold
            )
            processedBitmap = filtered
        } else {
            processedBitmap = orig
        }
    }

    // Frame capture for REAL process recording
    DisposableEffect(liveCompositeSnapshotProvider, processedBitmap) {
        timelapseRecorder.frameCaptureProvider = {
            liveCompositeSnapshotProvider?.invoke() ?: processedBitmap
        }
        onDispose {
            timelapseRecorder.frameCaptureProvider = null
        }
    }

    fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(50)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    // Auto-Save implementation
    fun persistProject(isAuto: Boolean = true) {
        coroutineScope.launch {
            val project = DrawingProject(
                id = existingProject?.id ?: 0L,
                title = initialTitle,
                category = "AR Trace",
                imageSource = imageSource,
                lastEditedAt = System.currentTimeMillis(),
                isLocked = isLocked,
                scale = scale,
                rotation = rotation,
                offsetX = offset.x,
                offsetY = offset.y,
                opacity = opacity,
                filterMode = filterMode.name,
                edgeThreshold = edgeThreshold,
                isFlippedHorizontal = isFlippedHorizontal,
                isFlippedVertical = isFlippedVertical,
                isTorchEnabled = isTorchOn,
                showGrid = showGrid,
                gridSize = gridSize,
                gridColorHex = gridColorHex,
                cameraZoomRatio = cameraZoomRatio,
                viewportZoom = viewportZoom,
                viewportOffsetX = viewportOffset.x,
                viewportOffsetY = viewportOffset.y,
                cropRatio = cropRatio,
                isAutoSaved = isAuto,
                anchorX = spatialState.pitch,
                anchorY = spatialState.roll,
                anchorZ = spatialState.yaw,
                anchorQx = spatialState.qx,
                anchorQy = spatialState.qy,
                anchorQz = spatialState.qz,
                anchorQw = spatialState.qw
            )
            repository.saveProject(project)
            if (!isAuto) {
                snackbarHostState.showSnackbar("💾 Projet et position 3D sauvegardés avec succès !")
            }
        }
    }

    // Auto-save on back navigation or exit
    BackHandler {
        persistProject(isAuto = true)
        onNavigateBack()
    }

    // Real High Resolution Photo Capture
    fun captureRealPhoto() {
        triggerHapticFeedback()
        val imageCapture = activeImageCapture
        val photoFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir,
            "AR_Trace_${System.currentTimeMillis()}.jpg"
        )

        if (imageCapture != null) {
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("📸 Photo HD enregistrée : ${photoFile.name}")
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("ARTraceScreen", "Capture photo failed", exception)
                    }
                }
            )
        } else {
            // Fallback composite snapshot
            liveCompositeSnapshotProvider?.invoke()?.let { bmp ->
                try {
                    FileOutputStream(photoFile).use { out ->
                        bmp.compress(Bitmap.CompressFormat.JPEG, 92, out)
                    }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("📸 Capture instantanée enregistrée !")
                    }
                } catch (e: Exception) {
                    Log.e("ARTraceScreen", "Snapshot error", e)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark)
    ) {
        // 1. Live Camera Feed with Zoom and Focus
        CameraPreviewView(
            hasCameraPermission = hasCameraPermission,
            torchManager = torchManager,
            cameraZoomRatio = cameraZoomRatio,
            onImageCaptureReady = { capture -> activeImageCapture = capture },
            onCompositeFrameProvider = { provider -> liveCompositeSnapshotProvider = provider },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Spatial Canvas Overlay with Viewport Loupe Zoom & Custom Grid
        SpatialCanvasOverlay(
            imageBitmap = processedBitmap,
            isLocked = isLocked,
            scale = scale,
            rotation = rotation,
            offset = offset,
            opacity = opacity,
            filterMode = filterMode,
            isFlippedHorizontal = isFlippedHorizontal,
            isFlippedVertical = isFlippedVertical,
            showGrid = showGrid,
            gridSize = gridSize,
            gridColor = Color(gridColorHex),
            cropRatio = cropRatio,
            isLoupeActive = isLoupeActive,
            loupeZoom = loupeZoom,
            loupePosition = loupePosition,
            viewportZoom = viewportZoom,
            viewportOffset = viewportOffset,
            spatialState = spatialState,
            onTapToPlace = { relativeOffset ->
                offset = relativeOffset
                isLocked = true
                spatialAnchorEngine.lockCurrentAnchor()
                triggerHapticFeedback()
                persistProject(isAuto = true)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("🎯 Image Placée & Ancrée Immédiatement sur la Surface !")
                }
            },
            onTransform = { scaleFactor, rotationDelta, panDelta ->
                scale = (scale * scaleFactor).coerceIn(0.15f, 6.0f)
                rotation = (rotation + rotationDelta) % 360f
                offset = Offset(offset.x + panDelta.x, offset.y + panDelta.y)
                persistProject(isAuto = true)
            },
            onTransformEnd = {
                // Immediate Auto-Anchor when pinch or drag gesture finishes!
                isLocked = true
                spatialAnchorEngine.lockCurrentAnchor()
                triggerHapticFeedback()
                persistProject(isAuto = true)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("🔒 Ancrage Automatique Verrouillé : Image Immobile")
                }
            },
            onViewportTransform = { zoomFactor, panDelta ->
                viewportZoom = (viewportZoom * zoomFactor).coerceIn(1.0f, 30.0f)
                viewportOffset = Offset(viewportOffset.x + panDelta.x, viewportOffset.y + panDelta.y)
            },
            modifier = Modifier.fillMaxSize()
        )

        // 3. Top HUD Bar (Retour, Statut 6DOF, Torche/Strobe, Guide, Photo HD)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xDD090D16))
                        .clickable {
                            persistProject(isAuto = true)
                            onNavigateBack()
                        }
                        .testTag("ar_back_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                }

                // Status Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isLocked) EmeraldLocked.copy(alpha = 0.95f) else Color(0xDD090D16))
                        .clickable {
                            val nextLock = !isLocked
                            isLocked = nextLock
                            if (nextLock) spatialAnchorEngine.lockCurrentAnchor() else spatialAnchorEngine.unlockAnchor()
                            triggerHapticFeedback()
                            persistProject(isAuto = true)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = if (isLocked) ObsidianDark else CyanNeon,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLocked) "ANCRAGE VERROUILLÉ [IMMOBILE 3D]" else "TOUCHEZ OU PINCHEZ POUR POSER",
                        color = if (isLocked) ObsidianDark else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                }

                // Top Right: Flash / Strobe & Guide
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Torch / Strobe Toggle
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isTorchOn) AmberCaution else Color(0xDD090D16))
                            .clickable {
                                torchManager.toggleTorch()
                                triggerHapticFeedback()
                            }
                            .testTag("torch_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Torche Flash",
                            tint = if (isTorchOn) ObsidianDark else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Setup Guide
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xDD090D16))
                            .clickable { onNavigateToGuide() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Guide", tint = CyanNeon)
                    }
                }
            }

            // Camera Zoom Quick Selector Bar (0.5x Grand Angle, 1x, 2x, 5x, 10x)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xDD090D16))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CAM :", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                listOf(0.5f to "0.5x Grand Angle", 1.0f to "1x", 2.0f to "2x", 5.0f to "5x").forEach { (ratio, label) ->
                    val isSelected = (cameraZoomRatio == ratio)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CyanNeon else Color.Transparent)
                            .clickable {
                                cameraZoomRatio = ratio
                                triggerHapticFeedback()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            label,
                            color = if (isSelected) ObsidianDark else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }

            // Real Timelapse Recording Status Banner
            if (timelapseState.isRecording) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xDD7F1D1D))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = RubyRecording,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "REC EN COURS • ${String.format("%02d:%02d", timelapseState.elapsedSeconds / 60, timelapseState.elapsedSeconds % 60)} • ${timelapseState.frameCount} images (2s)",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 4. Floating Viewport Loupe Magnifier Controller (Quand verrouillé)
        if (isLocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xEE090D16)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.width(54.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom Loupe", tint = EmeraldLocked, modifier = Modifier.size(20.dp))
                        Text(
                            "${(viewportZoom * 10).toInt() / 10f}x",
                            color = EmeraldLocked,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = {
                            viewportZoom = (viewportZoom + 0.5f).coerceAtMost(30f)
                        }) {
                            Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        IconButton(onClick = {
                            viewportZoom = (viewportZoom - 0.5f).coerceAtLeast(1f)
                            if (viewportZoom == 1f) viewportOffset = Offset.Zero
                        }) {
                            Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        IconButton(onClick = {
                            viewportZoom = 1.0f
                            viewportOffset = Offset.Zero
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset Zoom", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // 5. Bottom Studio Control Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 10.dp, start = 12.dp, end = 12.dp)
        ) {
            // Secondary Tools Toolbar
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xDD090D16)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shaders / Filters
                    IconButton(onClick = { showFilterSheet = !showFilterSheet }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filtres",
                            tint = if (filterMode != FilterMode.NORMAL) CyanNeon else Color.White
                        )
                    }

                    // Opacity Slider
                    IconButton(onClick = { showOpacitySlider = !showOpacitySlider }) {
                        Icon(
                            Icons.Default.Opacity,
                            contentDescription = "Opacité",
                            tint = if (showOpacitySlider) CyanNeon else Color.White
                        )
                    }

                    // Grid customizer
                    IconButton(onClick = { showGridSheet = !showGridSheet }) {
                        Icon(
                            Icons.Default.GridOn,
                            contentDescription = "Grille",
                            tint = if (showGrid) CyanNeon else Color.White
                        )
                    }

                    // Loupe Microscope tool
                    IconButton(onClick = {
                        isLoupeActive = !isLoupeActive
                        triggerHapticFeedback()
                    }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Loupe Macro",
                            tint = if (isLoupeActive) EmeraldLocked else Color.White
                        )
                    }

                    // Photo HD Capture Button
                    IconButton(onClick = { captureRealPhoto() }) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = "Prendre Photo HD",
                            tint = CyanNeon
                        )
                    }

                    // Flip Horizontal
                    IconButton(onClick = { isFlippedHorizontal = !isFlippedHorizontal }) {
                        Icon(
                            Icons.Default.Flip,
                            contentDescription = "Miroir",
                            tint = if (isFlippedHorizontal) CyanNeon else Color.White
                        )
                    }

                    // Center & Reset
                    IconButton(onClick = {
                        scale = 1.0f
                        rotation = 0.0f
                        offset = Offset.Zero
                        viewportZoom = 1.0f
                        viewportOffset = Offset.Zero
                    }) {
                        Icon(Icons.Default.CenterFocusStrong, contentDescription = "Recentrer", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Primary Bottom Actions: [REC Processus Vidéo] et [🔒 LOCK CONFIRMATION]
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timelapse REC Button
                Button(
                    onClick = {
                        if (timelapseState.isRecording) {
                            val finalState = timelapseRecorder.stopRecording()
                            coroutineScope.launch {
                                repository.saveTimelapseSession(
                                    TimelapseSession(
                                        projectId = existingProject?.id ?: 0L,
                                        projectTitle = initialTitle,
                                        durationSeconds = finalState.elapsedSeconds,
                                        frameCount = finalState.frameCount,
                                        intervalSeconds = finalState.intervalSeconds,
                                        folderPath = finalState.currentSessionDir ?: ""
                                    )
                                )
                                snackbarHostState.showSnackbar("🎬 Vidéo de processus enregistrée (${finalState.frameCount} frames réelles) !")
                            }
                        } else {
                            timelapseRecorder.startRecording(intervalSeconds = 2)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (timelapseState.isRecording) RubyRecording else Color(0xCC1E293B),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(54.dp)
                ) {
                    Icon(
                        imageVector = if (timelapseState.isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = if (timelapseState.isRecording) Color.White else RubyRecording,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (timelapseState.isRecording) "Stop REC" else "REC Vidéo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // The Golden Anchor Button: [ 🔒 LOCK CONFIRMATION ]
                Button(
                    onClick = {
                        val nextLock = !isLocked
                        isLocked = nextLock
                        triggerHapticFeedback()
                        persistProject(isAuto = true)
                        if (nextLock) {
                            spatialAnchorEngine.lockCurrentAnchor()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("🔒 Ancrage 3D Total Actif : Image Immobile dans l'Espace")
                            }
                        } else {
                            spatialAnchorEngine.unlockAnchor()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("🔓 Touchez ou pinchez n'importe où pour ré-ancrer instantanément")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLocked) EmeraldLocked else CyanNeon,
                        contentColor = ObsidianDark
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("lock_confirmation_button")
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = ObsidianDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLocked) "VERROUILLÉ [STABILITÉ TOTALE]" else "AUTO-ANCRAGE ACTIF",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = ObsidianDark
                    )
                }
            }
        }

        // 6. Expandable Filter & Shader Settings Sheet
        AnimatedVisibility(
            visible = showFilterSheet,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Filtres de Traçage & Shaders",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        IconButton(onClick = { showFilterSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FilterMode.values().forEach { mode ->
                        val isSelected = (filterMode == mode)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SurfaceElevated else Color.Transparent)
                                .clickable {
                                    filterMode = mode
                                    triggerHapticFeedback()
                                }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) CyanNeon else Color(0xFF334155)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = ObsidianDark, modifier = Modifier.size(12.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mode.displayName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                                Text(mode.description, color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (filterMode == FilterMode.SOBEL_EDGES || filterMode == FilterMode.BINARIZED) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Sensibilité des Contours : $edgeThreshold",
                            color = CyanNeon,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Slider(
                            value = edgeThreshold.toFloat(),
                            onValueChange = { edgeThreshold = it.toInt() },
                            valueRange = 10f..100f,
                            colors = SliderDefaults.colors(thumbColor = CyanNeon, activeTrackColor = CyanNeon),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 7. Expandable Grid & Crop Customization Sheet
        AnimatedVisibility(
            visible = showGridSheet,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Grille d'Alignement & Découpage",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        IconButton(onClick = { showGridSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Grid Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Afficher la Grille", color = Color.White, fontSize = 14.sp)
                        Button(
                            onClick = { showGrid = !showGrid },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showGrid) CyanNeon else SurfaceElevated
                            )
                        ) {
                            Text(if (showGrid) "ACTIVÉE" else "DÉSACTIVÉE", color = if (showGrid) ObsidianDark else Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Divisions : ${gridSize}x${gridSize}", color = CyanNeon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2, 3, 4, 6, 8, 10).forEach { size ->
                            FilterChip(
                                selected = (gridSize == size),
                                onClick = { gridSize = size },
                                label = { Text("${size}x${size}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanNeon,
                                    selectedLabelColor = ObsidianDark,
                                    containerColor = SurfaceElevated,
                                    labelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Format de Découpage / Rognage :", color = CyanNeon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ORIGINAL" to "Plein", "SQUARE" to "1:1 Carré", "4_3" to "4:3", "16_9" to "16:9", "A4" to "A4").forEach { (code, label) ->
                            FilterChip(
                                selected = (cropRatio == code),
                                onClick = { cropRatio = code },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanNeon,
                                    selectedLabelColor = ObsidianDark,
                                    containerColor = SurfaceElevated,
                                    labelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Opacity Quick Slider
        AnimatedVisibility(
            visible = showOpacitySlider,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xEE090D16)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.width(56.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Opacity, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(18.dp))
                    Text(
                        "${(opacity * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Slider(
                        value = opacity,
                        onValueChange = { opacity = it },
                        valueRange = 0.05f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = CyanNeon, activeTrackColor = CyanNeon),
                        modifier = Modifier.height(150.dp)
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp)
        )
    }
}

private fun loadBitmap(context: Context, source: String): Bitmap? {
    return try {
        if (source.startsWith("res:")) {
            val resName = source.removePrefix("res:")
            val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
            if (resId != 0) {
                BitmapFactory.decodeResource(context.resources, resId)
            } else null
        } else if (source.startsWith("content://") || source.startsWith("file://")) {
            val uri = Uri.parse(source)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } else if (source.startsWith("http://") || source.startsWith("https://")) {
            val url = java.net.URL(source)
            url.openStream().use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } else {
            BitmapFactory.decodeFile(source)
        }
    } catch (e: Exception) {
        null
    }
}
