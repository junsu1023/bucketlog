package com.bucketlog.platform

import androidx.compose.runtime.Composable

enum class WidgetKind { SMALL_STEP, YEAR_PROGRESS, TODAY_MEMORY }

/**
 * 홈 화면 위젯 3종(Phase 1, docs/ROADMAP.md)을 길게 눌러 찾는 대신 앱 안에서 바로 고정 요청한다 —
 * 위젯 발견성이 낮다는 건 잘 알려진 문제라 설정 화면에 진입점을 둔다. iOS는 아직 위젯
 * 익스텐션이 없어 no-op.
 */
@Composable
expect fun rememberWidgetPinner(): (WidgetKind) -> Unit
