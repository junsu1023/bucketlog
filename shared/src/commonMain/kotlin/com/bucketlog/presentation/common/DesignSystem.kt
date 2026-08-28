package com.bucketlog.presentation.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.bucketlog.presentation.theme.BucketLogMotion

/**
 * 재설계 공용 프리미티브(docs/DESIGN.md 개정 반영 예정). 카드·강한 그림자 대신 여백과
 * 얇은 괘선으로 계층을 만들고, 사진은 물리적 마운트 프레임에 끼운 것처럼 보이게 한다.
 */

/**
 * 재설계 화면 헤더 — Material TopAppBar 대신 큰 세리프 제목 + (선택) 뒤로가기 + (선택) 우측 액션 + 하단 괘선.
 * 하단 탭 화면은 [onBack] 없이, 상세성 화면은 [onBack]과 함께 쓴다.
 */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    backLabel: String? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (onBack != null) 4.dp else 20.dp,
                    end = 8.dp,
                    top = if (onBack != null) 24.dp else 34.dp,
                    bottom = 12.dp,
                ),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = backLabel,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp, lineHeight = 32.sp),
                modifier = Modifier.weight(1f),
            )
            trailing()
        }
        Hairline(Modifier.padding(horizontal = 20.dp))
    }
}

/** 계층 구분용 1px 괘선. Material Divider 대신 이걸 쓴다 — 화면을 강하게 가르지 않는다. */
@Composable
fun Hairline(modifier: Modifier = Modifier, alpha: Float = 0.12f) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)),
    )
}

/**
 * 날짜·수치·메타데이터용 라벨. JetBrains Mono + 넓은 letter-spacing.
 * 문구 자체는 항상 string resource에서 온다 — 여기선 스타일만 담당한다.
 */
@Composable
fun MonoMeta(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    maxLines: Int = 1,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall
            .merge(com.bucketlog.presentation.theme.MonoLabel())
            .copy(letterSpacing = 0.16.em),
        color = color,
        maxLines = maxLines,
    )
}

/**
 * 캡슐형 필터/선택 칩. 기본은 투명 + 괘선 테두리, 선택 시 강조색 채움 + 짙은 텍스트(§2.1).
 * Material FilterChip 대신 톤을 직접 통제하려고 둔다.
 */
@Composable
fun PillChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val bg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(220),
        label = "pillBg",
    )
    val fg by animateColorAsState(
        when {
            selected -> MaterialTheme.colorScheme.onPrimary
            enabled -> MaterialTheme.colorScheme.onSurfaceVariant
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        },
        animationSpec = tween(220),
        label = "pillFg",
    )
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier
            .clip(shape)
            .background(bg)
            .then(
                if (selected) Modifier
                else Modifier.border(
                    1.dp,
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.16f),
                    shape,
                ),
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = fg)
    }
}

/**
 * 대표 사진을 "인화해서 마운트에 끼운" 것처럼 보이게 하는 프레임 + blur-up.
 * 로드 전엔 [fallbackBrush](없으면 중립 톤)로 자리를 잡고, 로드되면 blur 16→0 + 페이드로 초점이 맞는다.
 */
@Composable
fun MountedPhoto(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 16f / 9f,
    contentRadius: Dp = 11.dp,
    fallbackBrush: Brush? = null,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    var loaded by remember(model) { mutableStateOf(model == null) }
    val revealAlpha by animateFloatAsState(
        if (loaded) 1f else 0f,
        animationSpec = tween(BucketLogMotion.PhotoRevealMillis),
        label = "photoAlpha",
    )
    val revealBlur by animateDpAsState(
        if (loaded) 0.dp else 16.dp,
        animationSpec = tween(BucketLogMotion.PhotoRevealMillis),
        label = "photoBlur",
    )
    val fallback = fallbackBrush ?: Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.surfaceContainerHighest,
            MaterialTheme.colorScheme.surfaceContainer,
        ),
    )
    Box(
        modifier
            .clip(RoundedCornerShape(contentRadius + 4.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(4.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .clip(RoundedCornerShape(contentRadius)),
        ) {
            Box(Modifier.fillMaxSize().background(fallback))
            if (model != null) {
                AsyncImage(
                    model = model,
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(revealBlur)
                        .graphicsLayer { alpha = revealAlpha },
                    contentScale = ContentScale.Crop,
                    onState = { state ->
                        if (state is AsyncImagePainter.State.Success) loaded = true
                    },
                )
            }
            overlay()
        }
    }
}

/** 사진이 아직/영영 없을 때 헤더·썸네일에 까는 절제된 다크 그라디언트. seed로 몇 종을 고른다. */
@Composable
fun photoFallbackBrush(seed: Int): Brush {
    val palettes = listOf(
        listOf(Color(0xFF26424B), Color(0xFF7C5A33)),
        listOf(Color(0xFF7A4A26), Color(0xFF241A12)),
        listOf(Color(0xFF16233A), Color(0xFF3A4A63)),
        listOf(Color(0xFF2E3B2A), Color(0xFF141712)),
        listOf(Color(0xFF3A2A3B), Color(0xFF171317)),
    )
    val p = palettes[((seed % palettes.size) + palettes.size) % palettes.size]
    return Brush.linearGradient(p)
}
