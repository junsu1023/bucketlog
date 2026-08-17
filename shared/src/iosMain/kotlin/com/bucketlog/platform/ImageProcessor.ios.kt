package com.bucketlog.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

private const val DISPLAY_MAX_DIMENSION = 1080.0
private const val DISPLAY_QUALITY = 0.8
private const val THUMBNAIL_MAX_DIMENSION = 320.0
private const val THUMBNAIL_QUALITY = 0.7

actual class ImageProcessor actual constructor() {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun process(source: ByteArray): ProcessedImage = withContext(Dispatchers.Default) {
        val original = UIImage(data = source.toNSData())

        // UIImage(data:)는 EXIF Orientation을 메타데이터로만 들고 있고, 아래처럼 다시 그려서
        // 리사이즈하는 과정에서 픽셀에 반영된다 — Android처럼 별도 회전 보정이 필요 없다.
        val displayImage = resize(original, DISPLAY_MAX_DIMENSION)
        val thumbnailImage = resize(original, THUMBNAIL_MAX_DIMENSION)

        val displayBytes = requireNotNull(UIImageJPEGRepresentation(displayImage, DISPLAY_QUALITY)).toByteArray()
        val thumbnailBytes = requireNotNull(UIImageJPEGRepresentation(thumbnailImage, THUMBNAIL_QUALITY)).toByteArray()

        val (width, height) = displayImage.size.useContents { width to height }

        ProcessedImage(display = displayBytes, thumbnail = thumbnailBytes, width = width.toInt(), height = height.toInt())
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun resize(image: UIImage, maxDimension: Double): UIImage {
    val (width, height) = image.size.useContents { width to height }
    val longSide = maxOf(width, height)
    if (longSide <= maxDimension) return image
    val scale = maxDimension / longSide
    val newWidth = width * scale
    val newHeight = height * scale
    val renderer = UIGraphicsImageRenderer(size = CGSizeMake(newWidth, newHeight))
    return renderer.imageWithActions { _ ->
        image.drawInRect(CGRectMake(0.0, 0.0, newWidth, newHeight))
    }
}
