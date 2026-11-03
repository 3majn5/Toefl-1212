package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import java.io.File

/** Numbers shown on the shareable card. */
data class ProgressCardData(
    val streakDays: Int,
    val level: Int,
    val totalXp: Int,
    val wordsLearned: Int,
    val wordsMastered: Int,
    val badges: Int,
    val bestChallenge: Int
)

/**
 * Renders a 1080×1350 progress card (the portrait size Instagram and X display uncropped)
 * and shares it through the Android share sheet.
 */
object ProgressCard {
    const val WIDTH = 1080
    const val HEIGHT = 1350

    private const val INDIGO = 0xFF5B5BD6.toInt()
    private const val LAVENDER = 0xFF9D7BF0.toInt()
    private const val CORAL = 0xFFFF7A59.toInt()
    private const val AMBER = 0xFFF5A524.toInt()
    private const val MINT = 0xFF14B88F.toInt()
    private const val WHITE = 0xFFFFFFFF.toInt()

    fun render(data: ProgressCardData): Bitmap {
        val bmp = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val w = WIDTH.toFloat()
        val h = HEIGHT.toFloat()

        // Background gradient with soft color blobs, matching the app's hero cards
        c.drawRect(0f, 0f, w, h, Paint().apply {
            shader = LinearGradient(0f, 0f, w, h, INDIGO, LAVENDER, Shader.TileMode.CLAMP)
        })
        fun blob(x: Float, y: Float, r: Float, color: Int) = c.drawCircle(x, y, r, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(x, y, r, (color and 0x00FFFFFF) or 0x88000000.toInt(), color and 0x00FFFFFF, Shader.TileMode.CLAMP)
        })
        blob(w * 0.85f, h * 0.12f, 520f, CORAL)
        blob(w * 0.1f, h * 0.55f, 480f, MINT)
        blob(w * 0.8f, h * 0.95f, 520f, AMBER)

        val bold = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        fun text(size: Float, color: Int = WHITE, typeface: Typeface = bold, alpha: Int = 255, align: Paint.Align = Paint.Align.LEFT) =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = size; this.color = color; this.typeface = typeface; this.alpha = alpha; textAlign = align
            }

        // Header
        c.drawText("TOEFL 35", 80f, 150f, text(64f))
        c.drawText("My vocabulary progress", 80f, 215f, text(38f, typeface = Typeface.DEFAULT, alpha = 220))

        // Streak hero
        c.drawText("🔥", 80f, 420f, text(150f))
        c.drawText("${data.streakDays}", 270f, 420f, text(170f))
        c.drawText("day streak", 280f, 480f, text(44f, alpha = 230, typeface = Typeface.DEFAULT))

        // Stat tiles (2 × 2)
        val tiles = listOf(
            "Level" to "${data.level}",
            "Total XP" to "%,d".format(data.totalXp),
            "Words learned" to "%,d".format(data.wordsLearned),
            "Mastered" to "%,d".format(data.wordsMastered)
        )
        val tileW = (w - 80f * 2 - 40f) / 2
        val tileH = 230f
        tiles.forEachIndexed { i, (label, value) ->
            val col = i % 2
            val row = i / 2
            val left = 80f + col * (tileW + 40f)
            val top = 560f + row * (tileH + 40f)
            c.drawRoundRect(RectF(left, top, left + tileW, top + tileH), 44f, 44f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = WHITE; alpha = 46
            })
            c.drawText(value, left + 44f, top + 130f, text(92f))
            c.drawText(label, left + 46f, top + 190f, text(36f, typeface = Typeface.DEFAULT, alpha = 225))
        }

        // Footer line
        val footer = buildString {
            append("🏅 ${data.badges} badges")
            if (data.bestChallenge > 0) append("   ⚡ ${data.bestChallenge} in 60 s")
        }
        c.drawText(footer, 80f, 1150f, text(44f))
        c.drawText("Learning TOEFL & Academic words, one day at a time", 80f, 1240f, text(32f, typeface = Typeface.DEFAULT, alpha = 210))
        return bmp
    }

    /** Saves the card to the app cache and opens the share sheet. */
    fun share(context: Context, data: ProgressCardData) {
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(dir, "toefl35_progress.png")
        file.outputStream().use { render(data).compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_TEXT,
                if (data.wordsLearned > 0) "🔥 ${data.streakDays}-day streak and ${data.wordsLearned} words learned on TOEFL 35! https://github.com/3majn5/Toefl-1212"
                else "🔥 ${data.streakDays}-day streak and ${data.totalXp} XP on TOEFL 35! https://github.com/3majn5/Toefl-1212"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Share your progress").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
