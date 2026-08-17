package com.bucketlog.presentation.home

import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.usecase.GoalOverview

data class HomeUiState(
    val statusFilter: GoalStatus = GoalStatus.IN_PROGRESS,
    val overviews: List<GoalOverview> = emptyList(),
    val checkInDrafts: Map<String, String> = emptyMap(),
    val pendingAction: PendingAction? = null,
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
)

sealed interface PendingAction {
    data class ConfirmComplete(val goalId: String, val title: String) : PendingAction
    data class ConfirmArchive(val goalId: String, val title: String) : PendingAction
    data class ConfirmDelete(val goalId: String, val title: String) : PendingAction
}
