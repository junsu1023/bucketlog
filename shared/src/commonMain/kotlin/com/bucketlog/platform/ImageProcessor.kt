package com.bucketlog.platform

/**
 * 원본 → 표시용(최대 1080px, JPEG q80) + 썸네일(320px, JPEG q70).
 * EXIF 방향은 회전으로 반영한 뒤 버린다 — JPEG로 재인코딩하는 과정에서 원본 EXIF가
 * 자연히 사라지므로 별도 스트립 단계가 필요 없다. docs/ARCHITECTURE.md §5.
 */
expect class ImageProcessor() {
    suspend fun process(source: ByteArray): ProcessedImage
}
