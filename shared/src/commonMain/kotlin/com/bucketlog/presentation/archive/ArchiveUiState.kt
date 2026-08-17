package com.bucketlog.presentation.archive

import com.bucketlog.domain.usecase.GoalOverview

data class ArchiveUiState(
    val tab: ArchiveTab = ArchiveTab.COMPLETED,
    val completed: List<GoalOverview> = emptyList(),
    val archived: List<GoalOverview> = emptyList(),
    val isLoading: Boolean = true,
)

enum class ArchiveTab { COMPLETED, ARCHIVED }
