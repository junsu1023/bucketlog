package com.bucketlog.presentation.theme

import androidx.compose.ui.graphics.Color
import com.bucketlog.domain.model.Category

/**
 * 홈 화면 위젯 전용 카테고리별 파스텔 배경. 앱 본체엔 카테고리별 색상 구분이 없어(칩은 전부
 * 중립 톤, docs/DESIGN.md §5.5) 위젯을 위해 새로 정했다 — 낮은 채도의 연한 톤만 쓰고, 진한
 * 포인트 컬러는 쓰지 않는다(위젯 전체를 강한 색으로 채우지 않기 위함).
 */
fun Category.widgetPastelColor(): Color = when (this) {
    Category.TRAVEL -> Color(0xFFFFF3D6)
    Category.HOBBY -> Color(0xFFE3EEDD)
    Category.RELATIONSHIP -> Color(0xFFFCE8E6)
    Category.CHALLENGE -> Color(0xFFE1EEF7)
    Category.LEARNING -> Color(0xFFEDE7F6)
    Category.HEALTH -> Color(0xFFDFF3EE)
    Category.OTHER -> Color(0xFFF1EDE6)
}
