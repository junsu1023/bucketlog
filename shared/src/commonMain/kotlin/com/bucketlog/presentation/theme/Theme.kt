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
    onPrimary = Color.White,
    primaryContainer = BucketLogColors.LightAccent,
    onPrimaryContainer = Color.White,
    background = BucketLogColors.LightBackground,
    onBackground = BucketLogColors.LightOnSurface,
    surface = BucketLogColors.LightSurface,
    onSurface = BucketLogColors.LightOnSurface,
    surfaceVariant = BucketLogColors.LightSurface,
    onSurfaceVariant = BucketLogColors.LightOnSurfaceVariant,
    tertiary = BucketLogColors.LightCompleted,
    onTertiary = Color.White,
    outline = BucketLogColors.LightOnSurfaceVariant,
    error = BucketLogColors.LightError,
)

private val DarkColors = darkColorScheme(
    primary = BucketLogColors.DarkAccent,
    onPrimary = Color.Black,
    primaryContainer = BucketLogColors.DarkAccent,
    onPrimaryContainer = Color.Black,
    background = BucketLogColors.DarkBackground,
    onBackground = BucketLogColors.DarkOnSurface,
    surface = BucketLogColors.DarkSurface,
    onSurface = BucketLogColors.DarkOnSurface,
    surfaceVariant = BucketLogColors.DarkSurface,
    onSurfaceVariant = BucketLogColors.DarkOnSurfaceVariant,
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
            typography = BucketLogTypography,
            content = content,
        )
    }
}
