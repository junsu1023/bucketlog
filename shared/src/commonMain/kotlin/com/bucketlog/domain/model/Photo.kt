package com.bucketlog.domain.model

/**
 * 표시용(최대 1080px) / 썸네일(320px) 경로만 들고 있다. 원본은 저장하지 않는다.
 * 저장 정책: docs/ARCHITECTURE.md §5
 */
data class Photo(
    val id: String,
    val entryId: String,
    val path: String,
    val thumbnailPath: String,
    val order: Int,
    val width: Int,                  // 레이아웃 점프 방지용
    val height: Int,
)
