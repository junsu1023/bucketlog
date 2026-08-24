package com.bucketlog.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** docs/DESIGN.md §4 완료/접어둠 색상은 Material 표준 롤에 없어 CompositionLocal로 별도 노출한다. */
data class ExtraColors(val completed: Color, val archived: Color)

private val LightExtras = ExtraColors(
    completed = BucketLogColors.LightCompleted,
    archived = BucketLogColors.LightArchived,
)
private val DarkExtras = ExtraColors(
    completed = BucketLogColors.DarkCompleted,
    archived = BucketLogColors.DarkArchived,
)

val LocalExtraColors = staticCompositionLocalOf { LightExtras }

private val LightColors = lightColorScheme(
    primary = BucketLogColors.LightAccent,
    onPrimary = BucketLogColors.OnAccent,
    primaryContainer = BucketLogColors.LightAccent,
    onPrimaryContainer = BucketLogColors.OnAccent,
    // secondaryContainer는 커스터마이징하지 않으면 Material 기본(보라 계열)로 남아 FilterChip
    // 선택 상태가 이 앱의 톤과 어긋난다 — 칩이 많은 화면(목표 만들기, 온보딩)까지 한 번에 잡아준다.
    secondaryContainer = BucketLogColors.LightAccent,
    onSecondaryContainer = BucketLogColors.OnAccent,
    background = BucketLogColors.LightBackground,
    onBackground = BucketLogColors.LightOnSurface,
    surface = BucketLogColors.LightSurface,
    onSurface = BucketLogColors.LightOnSurface,
    surfaceVariant = BucketLogColors.LightSurfaceContainer,
    onSurfaceVariant = BucketLogColors.LightOnSurfaceVariant,
    // Card/Dialog/NavigationBar 등 기본 컴포넌트가 실제로 참조하는 5단계 톤 — 여기를 안 채우면
    // Material 기본 라벤더색으로 떨어진다(실기기 확인 후 발견).
    surfaceContainerLowest = BucketLogColors.LightSurfaceContainerLowest,
    surfaceContainerLow = BucketLogColors.LightSurfaceContainerLow,
    surfaceContainer = BucketLogColors.LightSurfaceContainer,
    surfaceContainerHigh = BucketLogColors.LightSurfaceContainerHigh,
    surfaceContainerHighest = BucketLogColors.LightSurfaceContainerHighest,
    tertiary = BucketLogColors.LightCompleted,
    onTertiary = Color.White,
    outline = BucketLogColors.LightOnSurfaceVariant,
    error = BucketLogColors.LightError,
)

private val DarkColors = darkColorScheme(
    primary = BucketLogColors.DarkAccent,
    onPrimary = BucketLogColors.OnAccent,
    primaryContainer = BucketLogColors.DarkAccent,
    onPrimaryContainer = BucketLogColors.OnAccent,
    secondaryContainer = BucketLogColors.DarkAccent,
    onSecondaryContainer = BucketLogColors.OnAccent,
    background = BucketLogColors.DarkBackground,
    onBackground = BucketLogColors.DarkOnSurface,
    surface = BucketLogColors.DarkSurface,
    onSurface = BucketLogColors.DarkOnSurface,
    surfaceVariant = BucketLogColors.DarkSurfaceContainer,
    onSurfaceVariant = BucketLogColors.DarkOnSurfaceVariant,
    surfaceContainerLowest = BucketLogColors.DarkSurfaceContainerLowest,
    surfaceContainerLow = BucketLogColors.DarkSurfaceContainerLow,
    surfaceContainer = BucketLogColors.DarkSurfaceContainer,
    surfaceContainerHigh = BucketLogColors.DarkSurfaceContainerHigh,
    surfaceContainerHighest = BucketLogColors.DarkSurfaceContainerHighest,
    tertiary = BucketLogColors.DarkCompleted,
    onTertiary = Color.Black,
    outline = BucketLogColors.DarkOnSurfaceVariant,
    error = BucketLogColors.DarkError,
)

/**
 * M-01(시스템 따름 + 수동 전환)의 "시스템 따름" 부분까지만 지금 구현한다.
 * 수동 전환은 설정 화면(4주차 이후)에서 저장된 선택값을 받아 override하면 된다.
 */
@Composable
fun BucketLogTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalExtraColors provides if (darkTheme) DarkExtras else LightExtras) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = bucketLogTypography(),
            content = content,
        )
    }
}
