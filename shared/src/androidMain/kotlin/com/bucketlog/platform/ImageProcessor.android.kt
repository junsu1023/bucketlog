package com.bucketlog.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

private const val DISPLAY_MAX_DIMENSION = 1080
private const val DISPLAY_QUALITY = 80
private const val THUMBNAIL_MAX_DIMENSION = 320
private const val THUMBNAIL_QUALITY = 70

actual class ImageProcessor actual constructor() {
    actual suspend fun process(source: ByteArray): ProcessedImage = withContext(Dispatchers.Default) {
        val decoded = BitmapFactory.decodeByteArray(source, 0, source.size)
            ?: error("이미지를 디코딩할 수 없음")
        val rotated = rotateIfNeeded(decoded, readExifRotationDegrees(source))
        if (rotated !== decoded) decoded.recycle()

        val displayBitmap = resizeToMaxDimension(rotated, DISPLAY_MAX_DIMENSION)
        val thumbnailBitmap = resizeToMaxDimension(rotated, THUMBNAIL_MAX_DIMENSION)
        val displayBytes = compressToJpeg(displayBitmap, DISPLAY_QUALITY)
        val thumbnailBytes = compressToJpeg(thumbnailBitmap, THUMBNAIL_QUALITY)

        val width = displayBitmap.width
        val height = displayBitmap.height
        if (displayBitmap !== rotated) displayBitmap.recycle()
        if (thumbnailBitmap !== rotated) thumbnailBitmap.recycle()
        rotated.recycle()

        ProcessedImage(display = displayBytes, thumbnail = thumbnailBytes, width = width, height = height)
    }
}

private fun readExifRotationDegrees(source: ByteArray): Int {
    val orientation = ByteArrayInputStream(source).use { stream ->
        ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    }
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
}

private fun rotateIfNeeded(bitmap: Bitmap, degrees: Int): Bitmap {
    if (degrees == 0) return bitmap
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun resizeToMaxDimension(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val longSide = maxOf(bitmap.width, bitmap.height)
    if (longSide <= maxDimension) return bitmap
    val scale = maxDimension.toFloat() / longSide
    val newWidth = (bitmap.width * scale).toInt().coerceAtLeast(1)
    val newHeight = (bitmap.height * scale).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}

private fun compressToJpeg(bitmap: Bitmap, quality: Int): ByteArray =
    ByteArrayOutputStream().use { stream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        stream.toByteArray()
    }
