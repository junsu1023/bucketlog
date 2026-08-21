package com.bucketlog.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun renderShareCard(request: ShareCardRenderRequest): ByteArray = withContext(Dispatchers.Default) {
    val width = request.width
    val height = request.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    canvas.drawColor(Color.rgb(0x20, 0x1c, 0x19))

    request.photoBytes?.let { bytes ->
        val source = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        if (source != null) {
            canvas.drawBitmap(source, cropRectFor(source.width, source.height, width, height), Rect(0, 0, width, height), null)
            source.recycle()
        }
    }

    val scrimTop = height * 0.42f
    val scrimPaint = Paint().apply {
        shader = LinearGradient(
            0f, scrimTop, 0f, height.toFloat(),
            Color.TRANSPARENT, Color.argb(191, 0, 0, 0),
            Shader.TileMode.CLAMP,
        )
    }
    canvas.drawRect(0f, scrimTop, width.toFloat(), height.toFloat(), scrimPaint)

    val padding = width * 0.044f

    val watermarkPaint = TextPaint().apply {
        isAntiAlias = true
        color = Color.argb(204, 255, 255, 255)
        textSize = width * 0.032f
        typeface = Typeface.DEFAULT
    }
    canvas.drawText(request.appName, padding, padding + watermarkPaint.textSize, watermarkPaint)

    val contentWidth = (width - padding * 2).toInt().coerceAtLeast(1)
    val datePaint = TextPaint().apply {
        isAntiAlias = true
        color = Color.argb(217, 255, 255, 255)
        textSize = width * 0.034f
        typeface = Typeface.DEFAULT_BOLD
    }
    val titlePaint = TextPaint().apply {
        isAntiAlias = true
        color = Color.WHITE
        textSize = width * 0.06f
        typeface = Typeface.DEFAULT_BOLD
    }
    val retrospectPaint = TextPaint().apply {
        isAntiAlias = true
        color = Color.argb(230, 255, 255, 255)
        textSize = width * 0.038f
        typeface = Typeface.DEFAULT
    }

    val blocks = buildList {
        request.dateText?.let { add(staticLayoutOf(it, datePaint, contentWidth)) }
        add(staticLayoutOf(request.goalTitle, titlePaint, contentWidth))
        request.retrospect?.takeIf { it.isNotBlank() }?.let { add(staticLayoutOf(it, retrospectPaint, contentWidth)) }
    }

    val spacing = width * 0.018f
    val totalHeight = blocks.sumOf { it.height }.toFloat() + spacing * (blocks.size - 1)
    val startY = height - padding - totalHeight

    canvas.save()
    canvas.translate(padding, startY)
    blocks.forEach { layout ->
        layout.draw(canvas)
        canvas.translate(0f, layout.height + spacing)
    }
    canvas.restore()

    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    bitmap.recycle()
    stream.toByteArray()
}

/** Compose의 ContentScale.Crop과 동일한 중앙 기준 크롭 사각형(원본 좌표계)을 계산한다. */
private fun cropRectFor(sourceWidth: Int, sourceHeight: Int, targetWidth: Int, targetHeight: Int): Rect {
    val sourceRatio = sourceWidth.toFloat() / sourceHeight
    val targetRatio = targetWidth.toFloat() / targetHeight
    return if (sourceRatio > targetRatio) {
        val cropWidth = (sourceHeight * targetRatio).toInt()
        val x = (sourceWidth - cropWidth) / 2
        Rect(x, 0, x + cropWidth, sourceHeight)
    } else {
        val cropHeight = (sourceWidth / targetRatio).toInt()
        val y = (sourceHeight - cropHeight) / 2
        Rect(0, y, sourceWidth, y + cropHeight)
    }
}

private fun staticLayoutOf(text: String, paint: TextPaint, width: Int): StaticLayout =
    StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .build()
