package com.example.domain.image

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import com.example.data.model.FilterMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object EdgeDetectionProcessor {

    /**
     * GPU-accelerated ColorFilter matrix for instantaneous preview
     */
    fun getComposeColorFilter(mode: FilterMode, opacity: Float): ColorFilter {
        return when (mode) {
            FilterMode.NORMAL -> {
                val matrix = ColorMatrix()
                ColorFilter.colorMatrix(matrix)
            }
            FilterMode.GRAYSCALE -> {
                val matrix = ColorMatrix().apply {
                    setToSaturation(0f)
                }
                ColorFilter.colorMatrix(matrix)
            }
            FilterMode.NEGATIVE_INVERTED -> {
                // Negative inversion matrix: [-1, 0, 0, 0, 255, ...]
                val matrix = ColorMatrix(
                    floatArrayOf(
                        -1f,  0f,  0f, 0f, 255f,
                         0f, -1f,  0f, 0f, 255f,
                         0f,  0f, -1f, 0f, 255f,
                         0f,  0f,  0f, 1f,   0f
                    )
                )
                ColorFilter.colorMatrix(matrix)
            }
            FilterMode.BINARIZED -> {
                // High contrast binarization matrix
                val scale = 5.0f
                val translate = (-128f * scale) + 128f
                val matrix = ColorMatrix(
                    floatArrayOf(
                        scale, 0f, 0f, 0f, translate,
                        0f, scale, 0f, 0f, translate,
                        0f, 0f, scale, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    )
                ).apply {
                    setToSaturation(0f)
                }
                ColorFilter.colorMatrix(matrix)
            }
            FilterMode.SOBEL_EDGES -> {
                val matrix = ColorMatrix().apply {
                    setToSaturation(0f)
                }
                ColorFilter.colorMatrix(matrix)
            }
            FilterMode.BELLE_VUE -> {
                // High clarity, boosted contrast and vibrant sharpness
                val contrast = 1.35f
                val translate = (-128f * contrast) + 128f + 10f
                val matrix = ColorMatrix(
                    floatArrayOf(
                        contrast, 0f, 0f, 0f, translate,
                        0f, contrast, 0f, 0f, translate,
                        0f, 0f, contrast, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                ColorFilter.colorMatrix(matrix)
            }
            FilterMode.SKETCH_PENCIL -> {
                // Soft graphite sketch tone
                val matrix = ColorMatrix().apply {
                    setToSaturation(0.1f)
                }
                ColorFilter.colorMatrix(matrix)
            }
        }
    }

    /**
     * Full 3x3 Sobel kernel edge detection and outline extraction algorithm
     */
    suspend fun processBitmap(
        source: Bitmap,
        mode: FilterMode,
        threshold: Int = 40,
        tintColor: Int = Color.BLACK,
        maxDimension: Int = 720
    ): Bitmap = withContext(Dispatchers.Default) {
        if (mode == FilterMode.NORMAL) return@withContext source

        val origWidth = source.width
        val origHeight = source.height
        val scale = min(1.0f, maxDimension.toFloat() / max(origWidth, origHeight))
        val targetW = (origWidth * scale).toInt()
        val targetH = (origHeight * scale).toInt()

        val scaled = if (scale < 1.0f) {
            Bitmap.createScaledBitmap(source, targetW, targetH, true)
        } else {
            source.copy(Bitmap.Config.ARGB_8888, false)
        }

        val w = scaled.width
        val h = scaled.height
        val pixels = IntArray(w * h)
        scaled.getPixels(pixels, 0, w, 0, 0, w, h)

        val outputPixels = IntArray(w * h)

        when (mode) {
            FilterMode.SOBEL_EDGES -> {
                val gray = IntArray(w * h)
                for (i in pixels.indices) {
                    val p = pixels[i]
                    val r = (p shr 16) and 0xFF
                    val g = (p shr 8) and 0xFF
                    val b = p and 0xFF
                    gray[i] = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                }

                val thresholdSq = threshold * threshold

                for (y in 1 until h - 1) {
                    val yRowPrev = (y - 1) * w
                    val yRowCurr = y * w
                    val yRowNext = (y + 1) * w

                    for (x in 1 until w - 1) {
                        val p00 = gray[yRowPrev + (x - 1)]
                        val p01 = gray[yRowPrev + x]
                        val p02 = gray[yRowPrev + (x + 1)]

                        val p10 = gray[yRowCurr + (x - 1)]
                        val p12 = gray[yRowCurr + (x + 1)]

                        val p20 = gray[yRowNext + (x - 1)]
                        val p21 = gray[yRowNext + x]
                        val p22 = gray[yRowNext + (x + 1)]

                        val gx = (p02 + 2 * p12 + p22) - (p00 + 2 * p10 + p20)
                        val gy = (p20 + 2 * p21 + p22) - (p00 + 2 * p01 + p02)

                        val magSq = gx * gx + gy * gy

                        if (magSq > thresholdSq) {
                            val mag = min(255, sqrt(magSq.toDouble()).toInt())
                            outputPixels[yRowCurr + x] = (mag shl 24) or (tintColor and 0x00FFFFFF)
                        } else {
                            outputPixels[yRowCurr + x] = 0x00000000
                        }
                    }
                }
            }

            FilterMode.NEGATIVE_INVERTED -> {
                for (i in pixels.indices) {
                    val p = pixels[i]
                    val a = (p shr 24) and 0xFF
                    val r = 255 - ((p shr 16) and 0xFF)
                    val g = 255 - ((p shr 8) and 0xFF)
                    val b = 255 - (p and 0xFF)
                    outputPixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
                }
            }

            FilterMode.BINARIZED -> {
                for (i in pixels.indices) {
                    val p = pixels[i]
                    val a = (p shr 24) and 0xFF
                    val r = (p shr 16) and 0xFF
                    val g = (p shr 8) and 0xFF
                    val b = p and 0xFF
                    val lum = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                    val outColor = if (lum > threshold * 2.8) 0x00FFFFFF else (0xFF shl 24) or (tintColor and 0x00FFFFFF)
                    outputPixels[i] = if (a < 50) 0x00000000 else outColor
                }
            }

            FilterMode.GRAYSCALE -> {
                for (i in pixels.indices) {
                    val p = pixels[i]
                    val a = (p shr 24) and 0xFF
                    val r = (p shr 16) and 0xFF
                    val g = (p shr 8) and 0xFF
                    val b = p and 0xFF
                    val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                    outputPixels[i] = (a shl 24) or (gray shl 16) or (gray shl 8) or gray
                }
            }

            FilterMode.BELLE_VUE -> {
                for (i in pixels.indices) {
                    val p = pixels[i]
                    val a = (p shr 24) and 0xFF
                    var r = (p shr 16) and 0xFF
                    var g = (p shr 8) and 0xFF
                    var b = p and 0xFF

                    // High clarity S-curve
                    r = ((r - 128) * 1.35f + 138).toInt().coerceIn(0, 255)
                    g = ((g - 128) * 1.35f + 138).toInt().coerceIn(0, 255)
                    b = ((b - 128) * 1.35f + 138).toInt().coerceIn(0, 255)

                    outputPixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
                }
            }

            FilterMode.SKETCH_PENCIL -> {
                for (i in pixels.indices) {
                    val p = pixels[i]
                    val a = (p shr 24) and 0xFF
                    val r = (p shr 16) and 0xFF
                    val g = (p shr 8) and 0xFF
                    val b = p and 0xFF
                    val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                    // Pencil graphite tone with soft contrast
                    val graphite = (gray * 0.9f + 20).toInt().coerceIn(0, 255)
                    outputPixels[i] = (a shl 24) or (graphite shl 16) or (graphite shl 8) or graphite
                }
            }

            FilterMode.NORMAL -> {
                System.arraycopy(pixels, 0, outputPixels, 0, pixels.size)
            }
        }

        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        result.setPixels(outputPixels, 0, w, 0, 0, w, h)
        result
    }
}
