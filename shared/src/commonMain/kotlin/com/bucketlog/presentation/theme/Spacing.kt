package com.bucketlog.presentation.theme

import androidx.compose.ui.unit.dp

/** docs/DESIGN.md §4 스페이싱 그리드(4dp 배수). 화면 전반의 padding/gap을 이 값들로 통일한다. */
object BucketLogSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val huge = 48.dp
    val massive = 64.dp

    /** 카드 모서리 반경(§5.1). */
    val CardRadius = 16.dp

    /** 카테고리 칩 모서리 반경(§5.5) — 캡슐에 가깝게. */
    val ChipRadius = 16.dp

    /** 사진 그리드 썸네일 모서리 반경(§5.7). */
    val PhotoGridRadius = 8.dp
}
