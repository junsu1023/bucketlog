package com.bucketlog.presentation.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.usecase.ObserveGoalOverviewsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** 보관함(A-01 완료 그리드 · A-02 접어둠 리스트). MVP-SCOPE.md §2.5 */
class ArchiveViewModel(observeGoalOverviews: ObserveGoalOverviewsUseCase) : ViewModel() {

    private val tab = MutableStateFlow(ArchiveTab.COMPLETED)
    private val completedOverviews = observeGoalOverviews(GoalStatus.COMPLETED)
    private val archivedOverviews = observeGoalOverviews(GoalStatus.ARCHIVED)

    val uiState: StateFlow<ArchiveUiState> = combine(
        tab, completedOverviews, archivedOverviews,
    ) { selectedTab, completed, archived ->
        ArchiveUiState(
            tab = selectedTab,
            completed = completed,
            archived = archived,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ArchiveUiState())

    fun onIntent(intent: ArchiveIntent) {
        when (intent) {
            is ArchiveIntent.SelectTab -> tab.value = intent.tab
        }
    }
}
