package com.bucketlog.presentation.home

import com.bucketlog.domain.usecase.GoalOverview

data class HomeUiState(
    val yearFilter: BucketYearFilter,
    val thisYear: Int = 0,
    val availableYears: List<Int> = emptyList(),
    val overviews: List<GoalOverview> = emptyList(),
    val summaryTotal: Int = 0,
    val summaryCompleted: Int = 0,
    /** H-06 프리셋 제안 탭에서 이미 추가한 걸 중복 제안하지 않기 위한 전체 목표 제목 집합. */
    val existingTitles: Set<String> = emptySet(),
    val checkInDrafts: Map<String, String> = emptyMap(),
    val throwback: ThrowbackBanner? = null,
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
)

/** H-05 연도 전환 — 데이터에 실제 존재하는 연도만 동적으로 보여주고, "언젠가"는 항상 마지막에 고정한다. */
sealed interface BucketYearFilter {
    data class Year(val year: Int) : BucketYearFilter
    data object Someday : BucketYearFilter
}
