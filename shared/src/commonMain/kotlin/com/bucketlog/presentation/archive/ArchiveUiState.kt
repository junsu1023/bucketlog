package com.bucketlog.presentation.archive

import com.bucketlog.domain.model.Category
import com.bucketlog.domain.repository.MonthlyEntry
import com.bucketlog.domain.usecase.GoalOverview
import com.bucketlog.presentation.common.MonthKey

data class ArchiveUiState(
    val tab: ArchiveTab = ArchiveTab.COMPLETED,
    val completed: List<GoalOverview> = emptyList(),
    val archived: List<GoalOverview> = emptyList(),
    val monthlyEntries: List<MonthlyEntry> = emptyList(),
    val allEntries: List<MonthlyEntry> = emptyList(),
    val targetMonth: MonthKey = MonthKey.current(),
    val stats: ArchiveStats = ArchiveStats(),
    val isLoading: Boolean = true,
)

enum class ArchiveTab { COMPLETED, ARCHIVED, MONTHLY, ALL, STATS }

/** A-04 간단 통계. [byCategory]는 개수 내림차순, [byMonth]는 최근 6개월을 오래된 순으로. */
data class ArchiveStats(
    val totalCompleted: Int = 0,
    val byCategory: List<Pair<Category, Int>> = emptyList(),
    val byMonth: List<Pair<MonthKey, Int>> = emptyList(),
)
