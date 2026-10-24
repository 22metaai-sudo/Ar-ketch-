package com.example.ui.ar

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.example.data.model.FilterMode
import com.example.domain.ar.SpatialState
import com.example.domain.image.EdgeDetectionProcessor
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldLocked
import com.example.ui.theme.EmeraldLockedGlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SpatialCanvasOverlay(
    imageBitmap: Bitmap?,
    isLocked: Boolean,
    scale: Float,
    rotation: Float,
    offset: Offset,
    opacity: Float,
    filterMode: FilterMode,
    isFlippedHorizontal: Boolean,
    isFlippedVertical: Boolean,
    showGrid: Boolean,
    gridSize: Int,
    gridColor: Color,
    cropRatio: String,
    isLoupeActive: Boolean,
    loupeZoom: Float,
    loupePosition: Offset,
    viewportZoom: Float,
    viewportOffset: Offset,
    spatialState: SpatialState,
    onTapToPlace: (relativeOffset: Offset) -> Unit,
    onTransform: (scaleChange: Float, rotationChange: Float, offsetChange: Offset) -> Unit,
    onTransformEnd: () -> Unit,
    onViewportTransform: (zoomChange: Float, panChange: Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    // Tap Ripple Animation when placing/anchoring
    var lastPlacementTap by remember { mutableStateOf<Offset?>(null) }
    val tapRippleRadius = remember { Animatable(0f) }
    val tapRippleAlpha = remember { Animatable(0f) }

    val lockPulseAlpha = remember { Animatable(0.4f) }
    LaunchedEffect(isLocked) {
        if (isLocked) {
            lockPulseAlpha.animateTo(
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            lockPulseAlpha.snapTo(0.5f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it.toSize() }
            .pointerInput(isLocked) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val startPos = down.position
                    var hasTransformed = false

                    do {
                        val event = awaitPointerEvent()
                        val zoomChange = event.calculateZoom()
                        val rotationChange = event.calculateRotation()
                        val panChange = event.calculatePan()

                        val isSignificantZoom = abs(zoomChange - 1f) > 0.012f
                        val isSignificantRotation = abs(rotationChange) > 0.6f
                        val isSignificantPan = panChange.getDistance() > 2.0f

                        if (isSignificantZoom || isSignificantRotation || isSignificantPan) {
                            hasTransformed = true
                            if (!isLocked) {
                                onTransform(zoomChange, rotationChange, panChange)
                            } else {
                                // If already locked, multi-touch pinch still adjusts template scale and re-anchors
                                onTransform(zoomChange, rotationChange, panChange)
                            }
                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })

                    if (hasTransformed) {
                        // Pinch or gesture release -> Immediate Automatic Lock and Anchor!
                        onTransformEnd()
                    } else {
                        // Immediate placement on surface at touched point and Auto-Anchor!
                        lastPlacementTap = startPos
                        coroutineScope.launch {
                            tapRippleRadius.snapTo(10f)
                            tapRippleAlpha.snapTo(1f)
                            launch { tapRippleRadius.animateTo(120f, tween(450)) }
                            launch { tapRippleAlpha.animateTo(0f, tween(450)) }
                        }

                        val relOffset = if (canvasSize.width > 0 && canvasSize.height > 0) {
                            Offset(startPos.x - canvasSize.width / 2f, startPos.y - canvasSize.height / 2f)
                        } else Offset.Zero
                        onTapToPlace(relOffset)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 0. Draw Tap Placement Ripple
            if (tapRippleAlpha.value > 0f && lastPlacementTap != null) {
                val tapPt = lastPlacementTap!!
                drawCircle(
                    color = EmeraldLocked.copy(alpha = tapRippleAlpha.value),
                    radius = tapRippleRadius.value,
                    center = tapPt,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = CyanNeon.copy(alpha = tapRippleAlpha.value * 0.6f),
                    radius = tapRippleRadius.value * 0.5f,
                    center = tapPt,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // 1. Draw Surface SLAM feature points in calibration mode
            if (!isLocked) {
                drawSlamFeaturePoints(spatialState.featurePoints)
            }

            // 2. Alignment Grid with configurable divisions and color
            if (showGrid) {
                drawCustomGrid(canvasWidth, canvasHeight, gridSize, gridColor)
            }

            // 3. Crop ratio framing lines
            if (cropRatio != "ORIGINAL") {
                drawCropFraming(canvasWidth, canvasHeight, cropRatio)
            }

            // 4. Render Projected Trace Quad with Viewport Zoom
            if (imageBitmap != null && !imageBitmap.isRecycled) {
                val composeBitmap = imageBitmap.asImageBitmap()
                val imgWidth = composeBitmap.width.toFloat()
                val imgHeight = composeBitmap.height.toFloat()

                // Apply Viewport Loupe zoom & pan
                translate(left = viewportOffset.x, top = viewportOffset.y) {
                    scale(scale = viewportZoom, pivot = Offset(canvasWidth / 2f, canvasHeight / 2f)) {
                        val centerX = canvasWidth / 2f + offset.x
                        val centerY = canvasHeight / 2f + offset.y

                        val scaleXFactor = if (isFlippedHorizontal) -scale else scale
                        val scaleYFactor = if (isFlippedVertical) -scale else scale

                        val compensationOffsetX = if (isLocked) spatialState.deltaOffsetX else 0f
                        val compensationOffsetY = if (isLocked) spatialState.deltaOffsetY else 0f
                        val effectiveRotation = if (isLocked) rotation + spatialState.deltaRotation else rotation

                        translate(left = centerX + compensationOffsetX, top = centerY + compensationOffsetY) {
                            rotate(degrees = effectiveRotation, pivot = Offset.Zero) {
                                scale(scaleX = scaleXFactor, scaleY = scaleYFactor, pivot = Offset.Zero) {
                                    val topLeftX = -imgWidth / 2f
                                    val topLeftY = -imgHeight / 2f

                                    val colorFilter: ColorFilter? = if (filterMode != FilterMode.NORMAL) {
                                        EdgeDetectionProcessor.getComposeColorFilter(filterMode, opacity)
                                    } else null

                                    drawImage(
                                        image = composeBitmap,
                                        dstOffset = IntOffset(topLeftX.roundToInt(), topLeftY.roundToInt()),
                                        dstSize = IntSize(imgWidth.roundToInt(), imgHeight.roundToInt()),
                                        alpha = opacity,
                                        colorFilter = colorFilter
                                    )

                                    // Outer frame bounding box
                                    val frameColor = if (isLocked) {
                                        EmeraldLocked.copy(alpha = lockPulseAlpha.value)
                                    } else {
                                        CyanNeon.copy(alpha = 0.85f)
                                    }

                                    drawRoundRect(
                                        color = frameColor,
                                        topLeft = Offset(topLeftX, topLeftY),
                                        size = Size(imgWidth, imgHeight),
                                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                                        style = Stroke(
                                            width = if (isLocked) 3.5.dp.toPx() else 2.dp.toPx(),
                                            pathEffect = if (!isLocked) PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f) else null
                                        )
                                    )

                                    // Corner brackets
                                    val bracketLen = 30.dp.toPx()
                                    val strokeWidth = 4.dp.toPx()

                                    drawLine(frameColor, Offset(topLeftX, topLeftY), Offset(topLeftX + bracketLen, topLeftY), strokeWidth)
                                    drawLine(frameColor, Offset(topLeftX, topLeftY), Offset(topLeftX, topLeftY + bracketLen), strokeWidth)

                                    drawLine(frameColor, Offset(topLeftX + imgWidth, topLeftY), Offset(topLeftX + imgWidth - bracketLen, topLeftY), strokeWidth)
                                    drawLine(frameColor, Offset(topLeftX + imgWidth, topLeftY), Offset(topLeftX + imgWidth, topLeftY + bracketLen), strokeWidth)

                                    drawLine(frameColor, Offset(topLeftX, topLeftY + imgHeight), Offset(topLeftX + bracketLen, topLeftY + imgHeight), strokeWidth)
                                    drawLine(frameColor, Offset(topLeftX, topLeftY + imgHeight), Offset(topLeftX, topLeftY + imgHeight - bracketLen), strokeWidth)

                                    drawLine(frameColor, Offset(topLeftX + imgWidth, topLeftY + imgHeight), Offset(topLeftX + imgWidth - bracketLen, topLeftY + imgHeight), strokeWidth)
                                    drawLine(frameColor, Offset(topLeftX + imgWidth, topLeftY + imgHeight), Offset(topLeftX + imgWidth, topLeftY + bracketLen), strokeWidth)

                                    // Center alignment reticle
                                    val crossLen = 14.dp.toPx()
                                    drawLine(frameColor.copy(alpha = 0.6f), Offset(-crossLen, 0f), Offset(crossLen, 0f), 2.dp.toPx())
                                    drawLine(frameColor.copy(alpha = 0.6f), Offset(0f, -crossLen), Offset(0f, crossLen), 2.dp.toPx())
                                }
                            }
                        }
                    }
                }
            }

            // 5. Outil Loupe Circulaire / Microscope Macro Lens
            if (isLoupeActive && imageBitmap != null && !imageBitmap.isRecycled) {
                drawMicroscopicLoupe(
                    center = loupePosition,
                    zoom = loupeZoom,
                    bitmap = imageBitmap,
                    filterMode = filterMode,
                    opacity = opacity
                )
            }

            // 6. Horizon / Surface Leveler Target Bullseye in top right
            drawSurfaceLeveler(spatialState, canvasWidth, canvasHeight)
        }
    }
}

private fun DrawScope.drawCustomGrid(w: Float, h: Float, divisions: Int, color: Color) {
    val stroke = Stroke(width = 1.2.dp.toPx())
    val divCount = divisions.coerceIn(2, 16)

    for (i in 1 until divCount) {
        val x = w * (i.toFloat() / divCount)
        val y = h * (i.toFloat() / divCount)
        drawLine(color, Offset(x, 0f), Offset(x, h), stroke.width)
        drawLine(color, Offset(0f, y), Offset(w, y), stroke.width)
    }

    // Center Crosshair
    val cx = w / 2f
    val cy = h / 2f
    drawLine(color.copy(alpha = 0.8f), Offset(cx - 28.dp.toPx(), cy), Offset(cx + 28.dp.toPx(), cy), 2.dp.toPx())
    drawLine(color.copy(alpha = 0.8f), Offset(cx, cy - 28.dp.toPx()), Offset(cx, cy + 28.dp.toPx()), 2.dp.toPx())
}

private fun DrawScope.drawCropFraming(w: Float, h: Float, cropRatio: String) {
    val targetAspect = when (cropRatio) {
        "SQUARE" -> 1.0f
        "4_3" -> 4f / 3f
        "16_9" -> 16f / 9f
        "A4" -> 1.414f // Portrait A4
        else -> 1.0f
    }

    val boxWidth: Float
    val boxHeight: Float
    if (w / h > targetAspect) {
        boxHeight = h * 0.85f
        boxWidth = boxHeight * targetAspect
    } else {
        boxWidth = w * 0.85f
        boxHeight = boxWidth / targetAspect
    }

    val left = (w - boxWidth) / 2f
    val top = (h - boxHeight) / 2f

    drawRect(
        color = Color(0xAA000000),
        size = size
    )

    // Clear inner rectangle for framing
    val path = Path().apply {
        addRect(Rect(left, top, left + boxWidth, top + boxHeight))
    }

    clipPath(path, clipOp = ClipOp.Difference) {
        drawRect(Color(0x66000000))
    }

    drawRect(
        color = CyanNeon,
        topLeft = Offset(left, top),
        size = Size(boxWidth, boxHeight),
        style = Stroke(width = 2.dp.toPx())
    )
}

private fun DrawScope.drawMicroscopicLoupe(
    center: Offset,
    zoom: Float,
    bitmap: Bitmap,
    filterMode: FilterMode,
    opacity: Float
) {
    val loupeRadius = 80.dp.toPx()

    // Outer lens ring
    drawCircle(
        color = EmeraldLocked,
        radius = loupeRadius + 3.dp.toPx(),
        center = center,
        style = Stroke(width = 3.dp.toPx())
    )

    // Lens inner background
    drawCircle(
        color = Color(0xCC090D16),
        radius = loupeRadius,
        center = center
    )

    // Magnified content clip
    val lensPath = Path().apply {
        addOval(Rect(center.x - loupeRadius, center.y - loupeRadius, center.x + loupeRadius, center.y + loupeRadius))
    }

    clipPath(lensPath) {
        val composeBitmap = bitmap.asImageBitmap()
        val imgW = composeBitmap.width.toFloat()
        val imgH = composeBitmap.height.toFloat()

        translate(left = center.x, top = center.y) {
            scale(scale = zoom.coerceIn(2.0f, 30.0f), pivot = Offset.Zero) {
                val colorFilter = EdgeDetectionProcessor.getComposeColorFilter(filterMode, opacity)
                drawImage(
                    image = composeBitmap,
                    dstOffset = IntOffset((-imgW / 2f).roundToInt(), (-imgH / 2f).roundToInt()),
                    dstSize = IntSize(imgW.roundToInt(), imgH.roundToInt()),
                    alpha = opacity,
                    colorFilter = colorFilter
                )
            }
        }

        // Precision crosshair
        val reticleLen = loupeRadius * 0.9f
        drawLine(CyanNeon.copy(alpha = 0.7f), Offset(center.x - reticleLen, center.y), Offset(center.x + reticleLen, center.y), 1.dp.toPx())
        drawLine(CyanNeon.copy(alpha = 0.7f), Offset(center.x, center.y - reticleLen), Offset(center.x, center.y + reticleLen), 1.dp.toPx())
        drawCircle(CyanNeon, radius = 4.dp.toPx(), center = center, style = Stroke(1.dp.toPx()))
    }
}

private fun DrawScope.drawSlamFeaturePoints(points: List<Offset>) {
    points.forEach { pt ->
        val x = pt.x * size.width
        val y = pt.y * size.height
        drawCircle(
            color = CyanNeon.copy(alpha = 0.7f),
            radius = 3.dp.toPx(),
            center = Offset(x, y)
        )
        drawCircle(
            color = CyanNeon.copy(alpha = 0.3f),
            radius = 7.dp.toPx(),
            center = Offset(x, y),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

private fun DrawScope.drawSurfaceLeveler(state: SpatialState, w: Float, h: Float) {
    val radius = 22.dp.toPx()
    val center = Offset(w - 38.dp.toPx(), 44.dp.toPx())

    val isLevel = state.isLeveled
    val ringColor = if (isLevel) EmeraldLocked else Color(0x66FFFFFF)
    val bubbleColor = if (isLevel) EmeraldLockedGlow else CyanNeon

    drawCircle(
        color = ringColor,
        radius = radius,
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
    )

    drawCircle(
        color = ringColor.copy(alpha = 0.3f),
        radius = radius * 0.35f,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )

    val bubbleOffsetX = (state.roll / 15f).coerceIn(-1f, 1f) * (radius * 0.6f)
    val bubbleOffsetY = (state.pitch / 15f).coerceIn(-1f, 1f) * (radius * 0.6f)

    drawCircle(
        color = bubbleColor,
        radius = 5.dp.toPx(),
        center = Offset(center.x + bubbleOffsetX, center.y + bubbleOffsetY)
    )
}
