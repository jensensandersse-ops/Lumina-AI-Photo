package com.example.processing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader

data class SampleItem(
    val id: String,
    val title: String,
    val category: String,
    val defaultPreset: ImageProcessor.Preset,
    val beforeDescription: String,
    val afterDescription: String
)

object SamplePhotos {

    val sampleList = listOf(
        SampleItem(
            id = "sample_portrait",
            title = "Studio Portrait",
            category = "Face Retouch",
            defaultPreset = ImageProcessor.Preset.PORTRAIT_GLOW,
            beforeDescription = "Low contrast & slight skin blemishes",
            afterDescription = "Radiant skin, enhanced eyes & warmth"
        ),
        SampleItem(
            id = "sample_vintage",
            title = "Vintage Archive",
            category = "Restoration",
            defaultPreset = ImageProcessor.Preset.COLORIZE_RESTORE,
            beforeDescription = "Faded 1970s monochrome archive",
            afterDescription = "Vibrant colorized tones & scratch fix"
        ),
        SampleItem(
            id = "sample_cyber",
            title = "Night Neon",
            category = "Artistic",
            defaultPreset = ImageProcessor.Preset.CYBER_NEON,
            beforeDescription = "Dim low-light city street scene",
            afterDescription = "Electrifying cyan and magenta neon"
        ),
        SampleItem(
            id = "sample_landscape",
            title = "Alpine Peak",
            category = "Clarity & HDR",
            defaultPreset = ImageProcessor.Preset.ULTRA_CLARITY,
            beforeDescription = "Hazy mountain with subdued colors",
            afterDescription = "4K ultra-sharp peaks & sky clarity"
        )
    )

    /**
     * Synthesizes a high-detail sample bitmap for testing & instant preview.
     */
    fun createSampleBitmap(id: String, width: Int = 720, height: Int = 960): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (id) {
            "sample_portrait" -> drawPortraitSample(canvas, width, height, paint)
            "sample_vintage" -> drawVintageSample(canvas, width, height, paint)
            "sample_cyber" -> drawCyberSample(canvas, width, height, paint)
            "sample_landscape" -> drawLandscapeSample(canvas, width, height, paint)
            else -> drawPortraitSample(canvas, width, height, paint)
        }

        return bitmap
    }

    private fun drawPortraitSample(canvas: Canvas, w: Int, h: Int, paint: Paint) {
        // Soft backdrop
        paint.shader = RadialGradient(
            w * 0.5f, h * 0.4f, w * 0.8f,
            Color.rgb(180, 160, 150),
            Color.rgb(55, 45, 45),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Shoulders / Clothes
        paint.color = Color.rgb(60, 70, 95)
        val bodyPath = Path().apply {
            moveTo(w * 0.15f, h.toFloat())
            quadTo(w * 0.25f, h * 0.72f, w * 0.4f, h * 0.74f)
            lineTo(w * 0.6f, h * 0.74f)
            quadTo(w * 0.75f, h * 0.72f, w * 0.85f, h.toFloat())
            close()
        }
        canvas.drawPath(bodyPath, paint)

        // Neck
        paint.color = Color.rgb(225, 175, 150)
        canvas.drawRect(w * 0.42f, h * 0.62f, w * 0.58f, h * 0.75f, paint)

        // Face oval
        paint.color = Color.rgb(238, 188, 162)
        val faceRect = RectF(w * 0.3f, h * 0.32f, w * 0.7f, h * 0.66f)
        canvas.drawOval(faceRect, paint)

        // Cheek blush
        paint.color = Color.argb(45, 230, 90, 100)
        canvas.drawCircle(w * 0.38f, h * 0.52f, w * 0.08f, paint)
        canvas.drawCircle(w * 0.62f, h * 0.52f, w * 0.08f, paint)

        // Hair
        paint.color = Color.rgb(45, 30, 22)
        val hairPath = Path().apply {
            moveTo(w * 0.25f, h * 0.45f)
            quadTo(w * 0.25f, h * 0.22f, w * 0.5f, h * 0.22f)
            quadTo(w * 0.75f, h * 0.22f, w * 0.75f, h * 0.45f)
            lineTo(w * 0.8f, h * 0.68f)
            lineTo(w * 0.7f, h * 0.5f)
            quadTo(w * 0.5f, h * 0.35f, w * 0.3f, h * 0.5f)
            lineTo(w * 0.2f, h * 0.68f)
            close()
        }
        canvas.drawPath(hairPath, paint)

        // Eyes
        paint.color = Color.rgb(255, 255, 255)
        canvas.drawOval(RectF(w * 0.37f, h * 0.45f, w * 0.45f, h * 0.49f), paint)
        canvas.drawOval(RectF(w * 0.55f, h * 0.45f, w * 0.63f, h * 0.49f), paint)

        paint.color = Color.rgb(50, 80, 110) // Hazel / Blue iris
        canvas.drawCircle(w * 0.41f, h * 0.47f, w * 0.024f, paint)
        canvas.drawCircle(w * 0.59f, h * 0.47f, w * 0.024f, paint)

        paint.color = Color.WHITE // Catchlight
        canvas.drawCircle(w * 0.418f, h * 0.463f, w * 0.008f, paint)
        canvas.drawCircle(w * 0.598f, h * 0.463f, w * 0.008f, paint)

        // Eyebrows
        paint.color = Color.rgb(70, 50, 40)
        paint.strokeWidth = 6f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(w * 0.36f, h * 0.43f, w * 0.46f, h * 0.435f, paint)
        canvas.drawLine(w * 0.54f, h * 0.435f, w * 0.64f, h * 0.43f, paint)
        paint.style = Paint.Style.FILL

        // Smile
        paint.color = Color.rgb(205, 95, 95)
        val lips = RectF(w * 0.43f, h * 0.57f, w * 0.57f, h * 0.61f)
        canvas.drawOval(lips, paint)
    }

    private fun drawVintageSample(canvas: Canvas, w: Int, h: Int, paint: Paint) {
        // Monochrome aged sepia backdrop
        paint.shader = LinearGradient(
            0f, 0f, w.toFloat(), h.toFloat(),
            Color.rgb(190, 175, 155),
            Color.rgb(110, 95, 80),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Classic architecture arch
        paint.color = Color.rgb(140, 125, 110)
        canvas.drawRoundRect(RectF(w * 0.15f, h * 0.2f, w * 0.85f, h * 0.95f), 120f, 120f, paint)

        paint.color = Color.rgb(85, 75, 65)
        canvas.drawRoundRect(RectF(w * 0.28f, h * 0.32f, w * 0.72f, h * 0.95f), 90f, 90f, paint)

        // Person silhouette in vintage setting
        paint.color = Color.rgb(50, 45, 40)
        canvas.drawCircle(w * 0.5f, h * 0.52f, w * 0.09f, paint) // Hat / Head
        val coat = Path().apply {
            moveTo(w * 0.38f, h * 0.9f)
            lineTo(w * 0.44f, h * 0.62f)
            lineTo(w * 0.56f, h * 0.62f)
            lineTo(w * 0.62f, h * 0.9f)
            close()
        }
        canvas.drawPath(coat, paint)

        // Add subtle film grain dots
        paint.color = Color.argb(40, 0, 0, 0)
        for (i in 0 until 120) {
            val rx = (i * 37) % w
            val ry = (i * 73) % h
            canvas.drawCircle(rx.toFloat(), ry.toFloat(), 1.5f, paint)
        }
    }

    private fun drawCyberSample(canvas: Canvas, w: Int, h: Int, paint: Paint) {
        // Dark midnight sky
        paint.shader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            Color.rgb(10, 8, 25),
            Color.rgb(25, 15, 45),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Neon Sky outlines & Skyscrapers
        paint.color = Color.rgb(18, 12, 35)
        canvas.drawRect(w * 0.05f, h * 0.3f, w * 0.3f, h.toFloat(), paint)
        canvas.drawRect(w * 0.35f, h * 0.18f, w * 0.65f, h.toFloat(), paint)
        canvas.drawRect(w * 0.7f, h * 0.38f, w * 0.95f, h.toFloat(), paint)

        // Neon glowing signs
        paint.color = Color.rgb(0, 240, 255) // Cyan neon
        canvas.drawRect(w * 0.38f, h * 0.28f, w * 0.42f, h * 0.45f, paint)
        paint.color = Color.rgb(255, 40, 180) // Magenta neon
        canvas.drawRect(w * 0.58f, h * 0.32f, w * 0.62f, h * 0.55f, paint)

        // Street reflection road
        val roadPaint = Paint()
        roadPaint.shader = LinearGradient(
            0f, h * 0.75f, 0f, h.toFloat(),
            Color.rgb(12, 10, 22),
            Color.rgb(40, 25, 60),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, h * 0.75f, w.toFloat(), h.toFloat(), roadPaint)

        // Wet street reflections
        paint.color = Color.argb(120, 0, 240, 255)
        canvas.drawOval(RectF(w * 0.25f, h * 0.82f, w * 0.55f, h * 0.88f), paint)
        paint.color = Color.argb(120, 255, 40, 180)
        canvas.drawOval(RectF(w * 0.5f, h * 0.86f, w * 0.8f, h * 0.92f), paint)
    }

    private fun drawLandscapeSample(canvas: Canvas, w: Int, h: Int, paint: Paint) {
        // Sky gradient
        paint.shader = LinearGradient(
            0f, 0f, 0f, h * 0.6f,
            Color.rgb(75, 145, 225),
            Color.rgb(220, 190, 160),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h * 0.6f, paint)
        paint.shader = null

        // Sun / Glow
        paint.color = Color.argb(190, 255, 235, 180)
        canvas.drawCircle(w * 0.68f, h * 0.25f, w * 0.12f, paint)

        // Distant Mountains
        paint.color = Color.rgb(110, 130, 165)
        val bgMtn = Path().apply {
            moveTo(0f, h * 0.55f)
            lineTo(w * 0.3f, h * 0.32f)
            lineTo(w * 0.65f, h * 0.48f)
            lineTo(w.toFloat(), h * 0.38f)
            lineTo(w.toFloat(), h * 0.6f)
            lineTo(0f, h * 0.6f)
            close()
        }
        canvas.drawPath(bgMtn, paint)

        // Foreground Mountain with snow cap
        paint.color = Color.rgb(65, 80, 105)
        val fgMtn = Path().apply {
            moveTo(0f, h * 0.68f)
            lineTo(w * 0.45f, h * 0.36f)
            lineTo(w * 0.85f, h * 0.68f)
            close()
        }
        canvas.drawPath(fgMtn, paint)

        // Snow peak
        paint.color = Color.WHITE
        val snowCap = Path().apply {
            moveTo(w * 0.38f, h * 0.42f)
            lineTo(w * 0.45f, h * 0.36f)
            lineTo(w * 0.53f, h * 0.43f)
            lineTo(w * 0.48f, h * 0.45f)
            lineTo(w * 0.43f, h * 0.43f)
            close()
        }
        canvas.drawPath(snowCap, paint)

        // Pine trees & lake
        paint.shader = LinearGradient(
            0f, h * 0.68f, 0f, h.toFloat(),
            Color.rgb(35, 75, 95),
            Color.rgb(20, 50, 65),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, h * 0.68f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null
    }
}
