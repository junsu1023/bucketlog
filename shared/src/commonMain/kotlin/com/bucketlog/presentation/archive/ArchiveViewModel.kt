package com.bucketlog.presentation.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.usecase.ObserveGoalOverviewsUseCase
import com.bucketlog.presentation.common.MonthKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/** 보관함(A-01 완료 그리드 · A-02 접어둠 리스트 · "이번 달" 기록). MVP-SCOPE.md §2.5, N-01 */
@OptIn(ExperimentalCoroutinesApi::class)
class ArchiveViewModel(
    observeGoalOverviews: ObserveGoalOverviewsUseCase,
    private val entryRepository: EntryRepository,
) : ViewModel() {

    private val tab = MutableStateFlow(ArchiveTab.COMPLETED)
    private val targetMonth = MutableStateFlow(MonthKey.current())
    private val completedOverviews = observeGoalOverviews(GoalStatus.COMPLETED)
    private val archivedOverviews = observeGoalOverviews(GoalStatus.ARCHIVED)
    private val monthlyEntries = targetMonth.flatMapLatest { month ->
        entryRepository.observeEntriesInMonth(month.year, month.month)
    }

    val uiState: StateFlow<ArchiveUiState> = combine(
        tab, targetMonth, completedOverviews, archivedOverviews, monthlyEntries,
    ) { selectedTab, month, completed, archived, monthly ->
        ArchiveUiState(
            tab = selectedTab,
            completed = completed,
            archived = archived,
            monthlyEntries = monthly,
            targetMonth = month,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ArchiveUiState())

    fun onIntent(intent: ArchiveIntent) {
        when (intent) {
            is ArchiveIntent.SelectTab -> tab.value = intent.tab
            is ArchiveIntent.ShowMonth -> {
                targetMonth.value = intent.month
                tab.value = ArchiveTab.MONTHLY
            }
        }
    }
}
