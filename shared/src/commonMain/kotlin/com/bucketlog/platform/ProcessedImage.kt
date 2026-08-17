package com.bucketlog.platform

/** docs/ARCHITECTURE.md §5. 원본은 버리고 표시용/썸네일 두 장만 들고 다닌다. */
data class ProcessedImage(
    val display: ByteArray,
    val thumbnail: ByteArray,
    val width: Int,
    val height: Int,
)
