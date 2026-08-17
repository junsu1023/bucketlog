package com.bucketlog.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * docs/DESIGN.md §4 타이포 표.
 * - 목표 제목: 세리프/굵은 산세리프 — title* 스타일에 적용해 다이얼로그 제목 등에도 자연히 무게감이 실린다.
 * - 본문/메모: 산세리프, 행간 1.6배.
 * - 날짜/수치(모노스페이스)는 특정 용도에만 써야 하므로 여기서 전역으로 잡지 않고
 *   사용처(예: HomeScreen의 마지막 기록/진행 카운트)에서 FontFamily.Monospace를 직접 지정한다.
 */
private val defaults = Typography()

val BucketLogTypography = Typography(
    titleLarge = defaults.titleLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    titleMedium = defaults.titleMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    titleSmall = defaults.titleSmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    bodyLarge = defaults.bodyLarge.copy(lineHeight = (16 * 1.6).sp),
    bodyMedium = defaults.bodyMedium.copy(lineHeight = (14 * 1.6).sp),
)

/** 날짜·수치용 모노스페이스 스타일. 사용처에서 `MaterialTheme.typography.bodySmall.merge(MonoLabel)` 식으로 합성한다. */
val MonoLabel = TextStyle(fontFamily = FontFamily.Monospace)
