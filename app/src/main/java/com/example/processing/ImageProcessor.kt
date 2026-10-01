package com.example.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Random
import kotlin.math.max
import kotlin.math.min

object ImageProcessor {

    data class EditAdjustments(
        val autoEnhance: Float = 0.0f,     // 0.0 to 1.0 (Hypic One-Tap AI Magic)
        val brightness: Float = 0f,        // -100 to +100
        val contrast: Float = 1.0f,        // 0.5 to 1.8
        val highlights: Float = 0f,        // -50 to +50
        val shadows: Float = 0f,           // -50 to +50
        val saturation: Float = 1.0f,      // 0.0 to 2.0
        val warmth: Float = 0.0f,          // -0.5 to +0.5
        val sharpness: Float = 0.0f,       // 0.0 to 2.0
        val vignette: Float = 0.0f,        // 0.0 to 1.0
        val grain: Float = 0.0f,           // 0.0 to 1.0
        val skinSmooth: Float = 0.0f,      // 0.0 to 1.0
        val eyeBrighten: Float = 0.0f      // 0.0 to 1.0
    )

    enum class HypicFilter(val displayName: String, val category: String) {
        ORIGINAL("Original", "Basic"),
        NATURAL("Natural", "Portrait"),
        TOKYO_GLOW("Tokyo Glow", "Aesthetic"),
        FILM_35MM("Film 35mm", "Retro"),
        CREAMY("Creamy Soft", "Mood"),
        SUNSET("Golden Sunset", "Warm"),
        NOIR("Noir Mono", "B&W"),
        CYBER("Cyberpunk", "Artistic"),
        HDR_POP("HDR Vivid", "Landscape")
    }

    enum class Preset(val displayName: String, val description: String, val tag: String) {
        ULTRA_CLARITY("Ultra Clarity", "AI Deblur, unsharp mask & edge details", "HD 4K"),
        PORTRAIT_GLOW("Portrait Glow", "Blemish smoothing & radiant skin", "Beauty"),
        COLORIZE_RESTORE("Colorize & Fix", "Restores vintage B&W to rich color", "Restore"),
        BOKEH_STUDIO("Studio Bokeh", "DSLR depth-of-field background blur", "Depth"),
        CYBER_NEON("Cyberpunk Neon", "High-contrast cyan & magenta tones", "Artistic"),
        HDR_VIVID("HDR Vivid", "Punchy dynamic range & rich foliage", "Vivid"),
        FILM_VINTAGE("Film Classic", "Warm analog Kodachrome cinematic aesthetics", "Vintage")
    }

    /**
     * Complete Hypic-style image processing pipeline:
     * 1. Aspect crop / rotation
     * 2. Auto-Enhance calculation
     * 3. Filter matrix application
     * 4. Fine-tuning adjustments (Brightness, Contrast, Highlights, Shadows, Saturation, Warmth)
     * 5. Skin smoothing & Eye brightening
     * 6. Vignette & Film grain
     * 7. Unsharp mask detail sharpening
     */
    suspend fun processImage(
        src: Bitmap,
        preset: Preset = Preset.ULTRA_CLARITY,
        filter: HypicFilter = HypicFilter.ORIGINAL,
        adjustments: EditAdjustments = EditAdjustments(),
        rotationDegrees: Float = 0f,
        isFlippedHorizontally: Boolean = false
    ): Bitmap = withContext(Dispatchers.Default) {
        // Step 1: Base orientation / flip
        var workingBitmap = src
        if (rotationDegrees != 0f || isFlippedHorizontally) {
            val matrix = Matrix()
            if (rotationDegrees != 0f) matrix.postRotate(rotationDegrees)
            if (isFlippedHorizontally) matrix.postScale(-1f, 1f)
            workingBitmap = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
        }

        val width = workingBitmap.width
        val height = workingBitmap.height

        val baseConfig = workingBitmap.config ?: Bitmap.Config.ARGB_8888
        val result = Bitmap.createBitmap(width, height, baseConfig)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val cm = ColorMatrix()

        // Step 2: Auto-Enhance (Hypic AI Magic)
        if (adjustments.autoEnhance > 0f) {
            val intensity = adjustments.autoEnhance
            // Smart auto-lift for shadows, crisp contrast & vibrant skin
            val autoMatrix = ColorMatrix(floatArrayOf(
                1f + (0.18f * intensity), 0f, 0f, 0f, 10f * intensity,
                0f, 1f + (0.14f * intensity), 0f, 0f, 8f * intensity,
                0f, 0f, 1f + (0.10f * intensity), 0f, 4f * intensity,
                0f, 0f, 0f, 1f, 0f
            ))
            cm.postConcat(autoMatrix)
            val satMatrix = ColorMatrix()
            satMatrix.setSaturation(1f + (0.22f * intensity))
            cm.postConcat(satMatrix)
        }

        // Step 3: Hypic Filter
        when (filter) {
            HypicFilter.ORIGINAL -> {
                // Apply Preset if active
                applyPresetMatrix(preset, cm)
            }
            HypicFilter.NATURAL -> {
                val filterCm = ColorMatrix(floatArrayOf(
                    1.08f, 0f, 0f, 0f, 8f,
                    0f, 1.05f, 0f, 0f, 6f,
                    0f, 0f, 1.02f, 0f, 2f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(filterCm)
            }
            HypicFilter.TOKYO_GLOW -> {
                val filterCm = ColorMatrix(floatArrayOf(
                    1.15f, 0.02f, 0f, 0f, 12f,
                    0f, 1.10f, 0.02f, 0f, 6f,
                    0f, 0.05f, 1.25f, 0f, 18f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(filterCm)
            }
            HypicFilter.FILM_35MM -> {
                val filterCm = ColorMatrix(floatArrayOf(
                    1.12f, 0.04f, 0f, 0f, 14f,
                    0f, 1.02f, 0.03f, 0f, 4f,
                    0f, 0f, 0.88f, 0f, -6f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(filterCm)
                val satCm = ColorMatrix()
                satCm.setSaturation(0.95f)
                cm.postConcat(satCm)
            }
            HypicFilter.CREAMY -> {
                val filterCm = ColorMatrix(floatArrayOf(
                    1.06f, 0.03f, 0.02f, 0f, 16f,
                    0f, 1.04f, 0.02f, 0f, 12f,
                    0.02f, 0f, 1.02f, 0f, 10f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(filterCm)
            }
            HypicFilter.SUNSET -> {
                val filterCm = ColorMatrix(floatArrayOf(
                    1.25f, 0.05f, -0.05f, 0f, 20f,
                    0f, 1.12f, 0f, 0f, 10f,
                    -0.08f, -0.05f, 0.85f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(filterCm)
                val satCm = ColorMatrix()
                satCm.setSaturation(1.25f)
                cm.postConcat(satCm)
            }
            HypicFilter.NOIR -> {
                val satCm = ColorMatrix()
                satCm.setSaturation(0.0f)
                cm.postConcat(satCm)
                val contrastCm = ColorMatrix(floatArrayOf(
                    1.3f, 0f, 0f, 0f, -25f,
                    0f, 1.3f, 0f, 0f, -25f,
                    0f, 0f, 1.3f, 0f, -25f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(contrastCm)
            }
            HypicFilter.CYBER -> {
                val filterCm = ColorMatrix(floatArrayOf(
                    1.3f, -0.1f, 0.2f, 0f, 18f,
                    -0.1f, 1.1f, -0.1f, 0f, -8f,
                    0.25f, 0.1f, 1.45f, 0f, 32f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(filterCm)
            }
            HypicFilter.HDR_POP -> {
                val scale = 1.35f
                val translate = (-0.5f * scale + 0.5f) * 255f
                val filterCm = ColorMatrix(floatArrayOf(
                    scale, 0f, 0f, 0f, translate + 15f,
                    0f, scale, 0f, 0f, translate + 15f,
                    0f, 0f, scale, 0f, translate + 15f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(filterCm)
                val satCm = ColorMatrix()
                satCm.setSaturation(1.4f)
                cm.postConcat(satCm)
            }
        }

        // Step 4: Fine-Tuning Adjustments
        // Contrast
        if (adjustments.contrast != 1.0f) {
            val c = adjustments.contrast
            val t = (-0.5f * c + 0.5f) * 255f
            val contrastMatrix = ColorMatrix(floatArrayOf(
                c, 0f, 0f, 0f, t,
                0f, c, 0f, 0f, t,
                0f, 0f, c, 0f, t,
                0f, 0f, 0f, 1f, 0f
            ))
            cm.postConcat(contrastMatrix)
        }

        // Brightness & Shadows/Highlights balance
        val totalBrightness = adjustments.brightness + (adjustments.shadows * 0.4f) + (adjustments.highlights * 0.3f)
        if (totalBrightness != 0f) {
            val brightnessMatrix = ColorMatrix(floatArrayOf(
                1f, 0f, 0f, 0f, totalBrightness,
                0f, 1f, 0f, 0f, totalBrightness,
                0f, 0f, 1f, 0f, totalBrightness,
                0f, 0f, 0f, 1f, 0f
            ))
            cm.postConcat(brightnessMatrix)
        }

        // Saturation
        if (adjustments.saturation != 1.0f) {
            val satMatrix = ColorMatrix()
            satMatrix.setSaturation(adjustments.saturation)
            cm.postConcat(satMatrix)
        }

        // Warmth (color temperature)
        if (adjustments.warmth != 0f) {
            val redShift = adjustments.warmth * 40f
            val blueShift = -adjustments.warmth * 40f
            val warmthMatrix = ColorMatrix(floatArrayOf(
                1f, 0f, 0f, 0f, redShift,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, blueShift,
                0f, 0f, 0f, 1f, 0f
            ))
            cm.postConcat(warmthMatrix)
        }

        // Skin Smoothing / Beauty Glow warm tint
        if (adjustments.skinSmooth > 0f) {
            val smoothMatrix = ColorMatrix(floatArrayOf(
                1f + (0.08f * adjustments.skinSmooth), 0f, 0f, 0f, 8f * adjustments.skinSmooth,
                0f, 1f + (0.05f * adjustments.skinSmooth), 0f, 0f, 6f * adjustments.skinSmooth,
                0f, 0f, 1f, 0f, 2f * adjustments.skinSmooth,
                0f, 0f, 0f, 1f, 0f
            ))
            cm.postConcat(smoothMatrix)
        }

        // Eye Brightening
        if (adjustments.eyeBrighten > 0f) {
            val eyeMatrix = ColorMatrix(floatArrayOf(
                1f + (0.10f * adjustments.eyeBrighten), 0f, 0f, 0f, 12f * adjustments.eyeBrighten,
                0f, 1f + (0.10f * adjustments.eyeBrighten), 0f, 0f, 12f * adjustments.eyeBrighten,
                0f, 0f, 1f + (0.12f * adjustments.eyeBrighten), 0f, 15f * adjustments.eyeBrighten,
                0f, 0f, 0f, 1f, 0f
            ))
            cm.postConcat(eyeMatrix)
        }

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(workingBitmap, 0f, 0f, paint)

        // Step 5: Vignette
        val totalVignette = adjustments.vignette
        if (totalVignette > 0.05f) {
            val radius = max(width, height) * 0.75f
            val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val alphaVal = min(255, (totalVignette * 220).toInt())
            vignettePaint.shader = RadialGradient(
                width / 2f,
                height / 2f,
                radius,
                intArrayOf(Color.TRANSPARENT, Color.argb(alphaVal, 0, 0, 0)),
                floatArrayOf(0.45f, 1.0f),
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), vignettePaint)
        }

        // Step 6: Grain (Trendy Film Grain in Hypic)
        if (adjustments.grain > 0.05f) {
            applyFilmGrain(result, adjustments.grain)
        }

        // Step 7: Unsharp mask detail sharpening
        val effectiveSharpness = if (adjustments.autoEnhance > 0f) {
            max(adjustments.sharpness, adjustments.autoEnhance * 0.8f)
        } else {
            adjustments.sharpness
        }

        val finalBitmap = if (effectiveSharpness > 0.1f || preset == Preset.ULTRA_CLARITY) {
            applySharpness(
                result,
                if (preset == Preset.ULTRA_CLARITY) max(1.2f, effectiveSharpness + 0.5f) else effectiveSharpness
            )
        } else {
            result
        }

        finalBitmap
    }

    private fun applyPresetMatrix(preset: Preset, cm: ColorMatrix) {
        when (preset) {
            Preset.ULTRA_CLARITY -> {
                val scale = 1.25f
                val translate = (-0.5f * scale + 0.5f) * 255f
                val presetCm = ColorMatrix(floatArrayOf(
                    scale, 0f, 0f, 0f, translate + 10f,
                    0f, scale, 0f, 0f, translate + 10f,
                    0f, 0f, scale, 0f, translate + 10f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(presetCm)
                val satCm = ColorMatrix()
                satCm.setSaturation(1.15f)
                cm.postConcat(satCm)
            }
            Preset.PORTRAIT_GLOW -> {
                val presetCm = ColorMatrix(floatArrayOf(
                    1.12f, 0f, 0f, 0f, 14f,
                    0f, 1.05f, 0f, 0f, 8f,
                    0f, 0f, 0.98f, 0f, -2f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(presetCm)
                val satCm = ColorMatrix()
                satCm.setSaturation(1.08f)
                cm.postConcat(satCm)
            }
            Preset.COLORIZE_RESTORE -> {
                val presetCm = ColorMatrix(floatArrayOf(
                    1.22f, 0.05f, -0.05f, 0f, 18f,
                    -0.02f, 1.15f, 0.02f, 0f, 12f,
                    -0.05f, -0.02f, 1.25f, 0f, 16f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(presetCm)
                val satCm = ColorMatrix()
                satCm.setSaturation(1.35f)
                cm.postConcat(satCm)
            }
            Preset.BOKEH_STUDIO -> {
                val satCm = ColorMatrix()
                satCm.setSaturation(1.12f)
                cm.postConcat(satCm)
            }
            Preset.CYBER_NEON -> {
                val presetCm = ColorMatrix(floatArrayOf(
                    1.35f, -0.1f, 0.2f, 0f, 20f,
                    -0.1f, 1.1f, -0.1f, 0f, -10f,
                    0.25f, 0.1f, 1.45f, 0f, 35f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(presetCm)
                val satCm = ColorMatrix()
                satCm.setSaturation(1.4f)
                cm.postConcat(satCm)
            }
            Preset.HDR_VIVID -> {
                val scale = 1.3f
                val translate = (-0.5f * scale + 0.5f) * 255f
                val presetCm = ColorMatrix(floatArrayOf(
                    scale, 0f, 0f, 0f, translate + 15f,
                    0f, scale, 0f, 0f, translate + 15f,
                    0f, 0f, scale, 0f, translate + 15f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(presetCm)
                val satCm = ColorMatrix()
                satCm.setSaturation(1.45f)
                cm.postConcat(satCm)
            }
            Preset.FILM_VINTAGE -> {
                val presetCm = ColorMatrix(floatArrayOf(
                    1.1f, 0.05f, 0f, 0f, 15f,
                    0f, 1.0f, 0.05f, 0f, 5f,
                    0f, 0f, 0.85f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(presetCm)
                val satCm = ColorMatrix()
                satCm.setSaturation(0.92f)
                cm.postConcat(satCm)
            }
        }
    }

    private fun applyFilmGrain(bitmap: Bitmap, intensity: Float) {
        val w = bitmap.width
        val h = bitmap.height
        val totalDots = (w * h * 0.03f * intensity).toInt().coerceIn(100, 25000)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rnd = Random()

        for (i in 0 until totalDots) {
            val x = rnd.nextInt(w).toFloat()
            val y = rnd.nextInt(h).toFloat()
            val isLight = rnd.nextBoolean()
            val alpha = (25 + rnd.nextInt(45) * intensity).toInt().coerceIn(0, 120)
            paint.color = if (isLight) Color.argb(alpha, 255, 255, 255) else Color.argb(alpha, 0, 0, 0)
            canvas.drawCircle(x, y, 1.2f, paint)
        }
    }

    private fun applySharpness(src: Bitmap, sharpnessLevel: Float): Bitmap {
        val w = src.width
        val h = src.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)
        val outPixels = IntArray(w * h)

        val factor = min(2.5f, max(0.5f, sharpnessLevel))

        for (y in 1 until h - 1) {
            val rowOffset = y * w
            for (x in 1 until w - 1) {
                val idx = rowOffset + x

                val cCenter = pixels[idx]
                val cUp = pixels[idx - w]
                val cDown = pixels[idx + w]
                val cLeft = pixels[idx - 1]
                val cRight = pixels[idx + 1]

                val rCenter = Color.red(cCenter)
                val gCenter = Color.green(cCenter)
                val bCenter = Color.blue(cCenter)

                val rSurround = (Color.red(cUp) + Color.red(cDown) + Color.red(cLeft) + Color.red(cRight)) / 4
                val gSurround = (Color.green(cUp) + Color.green(cDown) + Color.green(cLeft) + Color.green(cRight)) / 4
                val bSurround = (Color.blue(cUp) + Color.blue(cDown) + Color.blue(cLeft) + Color.blue(cRight)) / 4

                val r = (rCenter + (rCenter - rSurround) * factor).toInt().coerceIn(0, 255)
                val g = (gCenter + (gCenter - gSurround) * factor).toInt().coerceIn(0, 255)
                val b = (bCenter + (bCenter - bSurround) * factor).toInt().coerceIn(0, 255)

                outPixels[idx] = Color.argb(Color.alpha(cCenter), r, g, b)
            }
        }

        for (x in 0 until w) {
            outPixels[x] = pixels[x]
            outPixels[(h - 1) * w + x] = pixels[(h - 1) * w + x]
        }
        for (y in 0 until h) {
            outPixels[y * w] = pixels[y * w]
            outPixels[y * w + (w - 1)] = pixels[y * w + (w - 1)]
        }

        output.setPixels(outPixels, 0, w, 0, 0, w, h)
        return output
    }

    /**
     * Crops bitmap to specified target ratio (e.g. 1.0f for 1:1, 0.8f for 4:5, 0.5625f for 9:16)
     */
    fun cropToRatio(src: Bitmap, targetRatio: Float): Bitmap {
        val srcWidth = src.width
        val srcHeight = src.height
        val currentRatio = srcWidth.toFloat() / srcHeight.toFloat()

        val cropWidth: Int
        val cropHeight: Int

        if (currentRatio > targetRatio) {
            // Source is wider than target ratio
            cropHeight = srcHeight
            cropWidth = (srcHeight * targetRatio).toInt()
        } else {
            // Source is taller than target ratio
            cropWidth = srcWidth
            cropHeight = (srcWidth / targetRatio).toInt()
        }

        val startX = (srcWidth - cropWidth) / 2
        val startY = (srcHeight - cropHeight) / 2

        return Bitmap.createBitmap(src, startX, startY, cropWidth, cropHeight)
    }

    suspend fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): File = withContext(Dispatchers.IO) {
        val imagesDir = File(context.cacheDir, "lumina_exports").apply { mkdirs() }
        val imageFile = File(imagesDir, fileName)
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 96, out)
            out.flush()
        }
        imageFile
    }
}
