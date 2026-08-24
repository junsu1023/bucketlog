package com.bucketlog.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * docs/DESIGN.md §2 컬러 팔레트("Archive Your Moments"). 오래 두고 볼 사진첩 컨셉 — 따뜻한 톤,
 * 앰버 강조, 올리브 완료. 접어둠(Paused)에는 빨강을 쓰지 않는다 — 중성 회색만 사용.
 */
object BucketLogColors {
    // 라이트
    val LightBackground = Color(0xFFFAF8F4)
    val LightSurface = Color(0xFFFFFFFF)
    val LightOnSurface = Color(0xFF1E1E1B)
    val LightOnSurfaceVariant = Color(0xFF6B6B66)

    /** Surface(카드)와 살짝 구분되는 중립 톤 — 칩·배지 배경용(§5.5). 디자인 문서엔 hex가 없어 직접 정함. */
    val LightSurfaceContainer = Color(0xFFEFEBE4)

    /**
     * Material3의 surfaceContainer* 5단계 톤(Card/Dialog/NavigationBar 등이 기본값으로 씀)을
     * 명시적으로 지정하지 않으면 Material 기본 라벤더색으로 떨어진다 — 실기기에서 발견한 버그.
     * Card는 surfaceContainerHighest를, Dialog는 surfaceContainerHigh를, NavigationBar는
     * surfaceContainer를 기본으로 쓰므로 다섯 단계 전부 우리 팔레트로 채워야 한다.
     */
    val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
    val LightSurfaceContainerLow = Color(0xFFFAF8F4)
    val LightSurfaceContainerHigh = Color(0xFFEFEBE4)
    val LightSurfaceContainerHighest = Color(0xFFFFFFFF)
    /** H-01 목표 카드 전용 배경 — 사용자 지정 톤(순백보다 따뜻하게). */
    val LightGoalCard = Color(0xFFFFF8E8)
    val LightAccent = Color(0xFFD6A441)
    val LightCompleted = Color(0xFF6B7F4D)
    val LightArchived = Color(0xFFB3B3AD)
    val LightError = Color(0xFFD24D57)

    // 다크 (기본)
    val DarkBackground = Color(0xFF0F0F10)
    val DarkSurface = Color(0xFF1A1A1D)
    val DarkOnSurface = Color(0xFFE9E9EA)
    val DarkOnSurfaceVariant = Color(0xFFA1A1A6)
    val DarkSurfaceContainer = Color(0xFF232326)
    val DarkSurfaceContainerLowest = Color(0xFF0F0F10)
    val DarkSurfaceContainerLow = Color(0xFF161617)
    val DarkSurfaceContainerHigh = Color(0xFF29292C)
    val DarkSurfaceContainerHighest = Color(0xFF323235)
    val DarkAccent = Color(0xFFE0B14A)
    val DarkCompleted = Color(0xFF8A8A6B)
    val DarkArchived = Color(0xFF6E6E73)
    val DarkError = Color(0xFFF26D6D)

    /**
     * 강조색(Accent) 위에 올리는 텍스트/아이콘 색. 라이트·다크 모두 밝은 골드 톤 강조색이라
     * 흰색을 올리면 대비가 2.3:1까지 떨어져(§8 최소 3:1 미달) 항상 짙은 색을 사용한다.
     */
    val OnAccent = Color(0xFF1E1E1B)
}
