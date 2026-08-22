package com.bucketlog.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.jetbrains_mono
import bucketlog.shared.generated.resources.playfair_display
import org.jetbrains.compose.resources.Font

/**
 * docs/DESIGN.md §3 타이포그래피. 목표 제목/디스플레이류는 Playfair Display(세리프), 날짜·수치는
 * JetBrains Mono를 임베드해 쓴다. 두 폰트 다 가변 폰트(variable font) 파일 하나에 굵기별로
 * Font()를 여러 번 등록해 인스턴싱한다. 본문(한글 포함)은 Noto Sans KR을 따로 임베드하지 않고
 * 시스템 기본 폰트를 쓴다 — Android는 이미 Noto Sans 계열이 기본이라 사실상 동일하고, 완전한
 * CJK 글리프 세트를 굵기별로 여러 개 담으면 앱 용량이 수십 MB 늘어나 배보다 배꼽이 커진다.
 */
@Composable
private fun playfairDisplay(): FontFamily = FontFamily(
    Font(Res.font.playfair_display, weight = FontWeight.Normal),
    Font(Res.font.playfair_display, weight = FontWeight.Medium),
    Font(Res.font.playfair_display, weight = FontWeight.SemiBold),
)

@Composable
private fun jetBrainsMono(): FontFamily = FontFamily(
    Font(Res.font.jetbrains_mono, weight = FontWeight.Normal),
    Font(Res.font.jetbrains_mono, weight = FontWeight.Medium),
)

/** 날짜·수치용 모노스페이스 스타일. 사용처에서 `MaterialTheme.typography.bodySmall.merge(MonoLabel())`로 합성한다. */
@Composable
fun MonoLabel(): TextStyle = TextStyle(fontFamily = jetBrainsMono())

@Composable
fun bucketLogTypography(): Typography {
    val playfair = playfairDisplay()
    val defaults = Typography()
    return defaults.copy(
        // Display — 섹션 헤더("올해의 작은 것들") 등 이 앱에서 가장 무게감 있는 텍스트.
        headlineSmall = defaults.headlineSmall.copy(
            fontFamily = playfair,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = (28 * 1.4).sp,
        ),
        // Title 1 — 목표 상세 화면 타이틀, 다이얼로그 제목(대부분 목표 이름이 그대로 들어간다).
        titleLarge = defaults.titleLarge.copy(
            fontFamily = playfair,
            fontWeight = FontWeight.SemiBold,
            lineHeight = (defaults.titleLarge.fontSize.value * 1.4).sp,
        ),
        // Title 2 — 홈 목표 카드 제목.
        titleMedium = defaults.titleMedium.copy(
            fontFamily = playfair,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = (18 * 1.4).sp,
        ),
        titleSmall = defaults.titleSmall.copy(fontFamily = playfair, fontWeight = FontWeight.Medium),
        // Body — 본문/메모. 행간을 넉넉히(160%) 줘 기록을 읽기 편하게 한다.
        bodyLarge = defaults.bodyLarge.copy(lineHeight = (defaults.bodyLarge.fontSize.value * 1.6).sp),
        // Body Small — 보조 텍스트(마지막 기록 시점 등). 150% 행간.
        bodyMedium = defaults.bodyMedium.copy(lineHeight = (defaults.bodyMedium.fontSize.value * 1.5).sp),
        // Caption — 플레이스홀더 등. 140% 행간.
        bodySmall = defaults.bodySmall.copy(lineHeight = (defaults.bodySmall.fontSize.value * 1.4).sp),
    )
}
