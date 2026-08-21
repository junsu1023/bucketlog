package com.bucketlog.platform

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSString
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.NSStringDrawingUsesLineFragmentOrigin
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UIRectFill
import platform.UIKit.boundingRectWithSize
import platform.UIKit.drawAtPoint
import platform.UIKit.drawInRect

/**
 * Android actual(ShareCardRenderer.android.kt)과 동일한 레이아웃을 Core Graphics로 다시 그린다.
 * CGGradient의 Kotlin/Native cinterop 타입 변환이 번거로워, 스크림은 여러 겹의 반투명 사각형을
 * 쌓아 그라데이션처럼 보이게 한다(시각적 결과는 동일). NSString의 drawInRect(rect:withAttributes:)는
 * 별도 paragraph style 없이도 기본으로 rect 너비에 맞춰 자동 줄바꿈한다.
 */
@OptIn(ExperimentalForeignApi::class)
actual suspend fun renderShareCard(request: ShareCardRenderRequest): ByteArray = withContext(Dispatchers.Default) {
    val width = request.width.toDouble()
    val height = request.height.toDouble()

    UIGraphicsBeginImageContextWithOptions(CGSizeMake(width, height), true, 1.0)

    UIColor.colorWithRed(0x20 / 255.0, green = 0x1c / 255.0, blue = 0x19 / 255.0, alpha = 1.0).setFill()
    UIRectFill(CGRectMake(0.0, 0.0, width, height))

    request.photoBytes?.let { bytes ->
        val image = UIImage(data = bytes.toNSData())
        val (sourceWidth, sourceHeight) = image.size.useContents { this.width to this.height }
        if (sourceWidth > 0 && sourceHeight > 0) {
            image.drawInRect(cropRectFor(sourceWidth, sourceHeight, width, height))
        }
    }

    drawScrim(width, height)

    val padding = width * 0.044

    val watermarkAttributes = mapOf<Any?, Any?>(
        NSFontAttributeName to UIFont.systemFontOfSize(width * 0.032),
        NSForegroundColorAttributeName to UIColor.whiteColor.colorWithAlphaComponent(0.8),
    )
    (request.appName as NSString).drawAtPoint(CGPointMake(padding, padding), withAttributes = watermarkAttributes)

    val contentWidth = width - padding * 2
    val spacing = width * 0.018

    val blocks = buildList {
        request.retrospect?.takeIf { it.isNotBlank() }?.let {
            add(
                it to mapOf<Any?, Any?>(
                    NSFontAttributeName to UIFont.systemFontOfSize(width * 0.038),
                    NSForegroundColorAttributeName to UIColor.whiteColor.colorWithAlphaComponent(0.9),
                ),
            )
        }
        add(
            request.goalTitle to mapOf<Any?, Any?>(
                NSFontAttributeName to UIFont.boldSystemFontOfSize(width * 0.06),
                NSForegroundColorAttributeName to UIColor.whiteColor,
            ),
        )
        request.dateText?.let {
            add(
                it to mapOf<Any?, Any?>(
                    NSFontAttributeName to UIFont.boldSystemFontOfSize(width * 0.034),
                    NSForegroundColorAttributeName to UIColor.whiteColor.colorWithAlphaComponent(0.85),
                ),
            )
        }
    }

    var cursorYFromBottom = padding
    blocks.forEach { (text, attributes) ->
        val nsText = text as NSString
        val textHeight = nsText.boundingRectWithSize(
            CGSizeMake(contentWidth, height),
            options = NSStringDrawingUsesLineFragmentOrigin,
            attributes = attributes,
            context = null,
        ).useContents { size.height }
        cursorYFromBottom += textHeight
        nsText.drawInRect(
            CGRectMake(padding, height - cursorYFromBottom, contentWidth, textHeight),
            withAttributes = attributes,
        )
        cursorYFromBottom += spacing
    }

    val uiImage = UIGraphicsGetImageFromCurrentImageContext()
    val pngData = uiImage?.let { UIImagePNGRepresentation(it) }
    UIGraphicsEndImageContext()
    pngData?.toByteArray() ?: ByteArray(0)
}

/** ContentScale.Crop과 동일 — 화면을 꽉 채우도록 확대한 뒤 넘치는 쪽을 중앙 기준으로 잘라낸다. */
@OptIn(ExperimentalForeignApi::class)
private fun cropRectFor(sourceWidth: Double, sourceHeight: Double, targetWidth: Double, targetHeight: Double): CValue<CGRect> {
    val sourceRatio = sourceWidth / sourceHeight
    val targetRatio = targetWidth / targetHeight
    return if (sourceRatio > targetRatio) {
        val scaledWidth = targetHeight * sourceRatio
        CGRectMake(-(scaledWidth - targetWidth) / 2, 0.0, scaledWidth, targetHeight)
    } else {
        val scaledHeight = targetWidth / sourceRatio
        CGRectMake(0.0, -(scaledHeight - targetHeight) / 2, targetWidth, scaledHeight)
    }
}

/** CGGradient cinterop 없이, 아래로 갈수록 진해지는 반투명 검정 띠를 여러 겹 쌓아 흉내낸다. */
@OptIn(ExperimentalForeignApi::class)
private fun drawScrim(width: Double, height: Double) {
    val scrimTop = height * 0.42
    val steps = 24
    val bandHeight = (height - scrimTop) / steps
    for (i in 0 until steps) {
        val progress = i.toDouble() / (steps - 1)
        val alpha = 0.75 * progress
        UIColor.colorWithRed(0.0, green = 0.0, blue = 0.0, alpha = alpha).setFill()
        UIRectFill(CGRectMake(0.0, scrimTop + bandHeight * i, width, bandHeight + 1))
    }
}
