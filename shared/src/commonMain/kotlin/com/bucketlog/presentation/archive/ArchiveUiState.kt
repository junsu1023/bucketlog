package com.bucketlog.presentation.archive

import com.bucketlog.domain.repository.MonthlyEntry
import com.bucketlog.domain.usecase.GoalOverview
import com.bucketlog.presentation.common.MonthKey

data class ArchiveUiState(
    val tab: ArchiveTab = ArchiveTab.COMPLETED,
    val completed: List<GoalOverview> = emptyList(),
    val archived: List<GoalOverview> = emptyList(),
    val monthlyEntries: List<MonthlyEntry> = emptyList(),
    val targetMonth: MonthKey = MonthKey.current(),
    val isLoading: Boolean = true,
)

enum class ArchiveTab { COMPLETED, ARCHIVED, MONTHLY }
