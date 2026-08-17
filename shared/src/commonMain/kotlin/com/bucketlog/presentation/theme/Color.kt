package com.bucketlog.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * docs/DESIGN.md §4 컬러 표. "오래 두고 볼 사진첩" 컨셉 — 따뜻한 톤, 앰버 강조, 올리브 완료.
 * 접어둠 상태에는 빨강을 쓰지 않는다(§4) — 중성 회색만 사용.
 */
object BucketLogColors {
    // 라이트
    val LightBackground = Color(0xFFFAF9F7)
    val LightSurface = Color(0xFFFFFFFF)
    val LightOnSurface = Color(0xFF1C1917)
    val LightOnSurfaceVariant = Color(0xFF78716C)
    val LightAccent = Color(0xFFB45309)
    val LightCompleted = Color(0xFF4D7C0F)
    val LightArchived = Color(0xFF78716C)
    val LightError = Color(0xFFB3261E)

    // 다크
    val DarkBackground = Color(0xFF14120F)
    val DarkSurface = Color(0xFF211E1A)
    val DarkOnSurface = Color(0xFFEDE9E3)
    val DarkOnSurfaceVariant = Color(0xFFA8A29E)
    val DarkAccent = Color(0xFFF59E0B)
    val DarkCompleted = Color(0xFF84CC16)
    val DarkArchived = Color(0xFF78716C)
    val DarkError = Color(0xFFF2B8B5)
}
