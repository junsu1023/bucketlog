package com.bucketlog.presentation.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.usecase.GoalOverview
import com.bucketlog.domain.usecase.ObserveGoalOverviewsUseCase
import com.bucketlog.presentation.common.MonthKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** 보관함(A-01 완료 그리드 · A-02 접어둠 리스트 · A-03 전체 타임라인 · A-04 간단 통계). MVP-SCOPE.md §2.5, N-01 */
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
    private val allEntries = entryRepository.observeAllEntries()
    private val stats: Flow<ArchiveStats> = completedOverviews.map { it.toArchiveStats() }

    private val goalLists = combine(completedOverviews, archivedOverviews) { completed, archived ->
        completed to archived
    }
    private val entryLists = combine(monthlyEntries, allEntries) { monthly, all -> monthly to all }

    val uiState: StateFlow<ArchiveUiState> = combine(
        tab, targetMonth, goalLists, entryLists, stats,
    ) { selectedTab, month, (completed, archived), (monthly, all), archiveStats ->
        ArchiveUiState(
            tab = selectedTab,
            targetMonth = month,
            completed = completed,
            archived = archived,
            monthlyEntries = monthly,
            allEntries = all,
            stats = archiveStats,
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

private fun List<GoalOverview>.toArchiveStats(): ArchiveStats {
    val zone = TimeZone.currentSystemDefault()
    val byCategory = groupBy { it.goal.category }
        .mapValues { (_, goals) -> goals.size }
        .toList()
        .sortedByDescending { (_, count) -> count }
    val byMonth = mapNotNull { it.goal.completedAt }
        .map { completedAt ->
            val date = completedAt.toLocalDateTime(zone).date
            MonthKey(date.year, date.monthNumber)
        }
        .groupingBy { it }
        .eachCount()
        .toList()
        .sortedBy { (month, _) -> month.year * 100 + month.month }
        .takeLast(6)
    return ArchiveStats(totalCompleted = size, byCategory = byCategory, byMonth = byMonth)
}
