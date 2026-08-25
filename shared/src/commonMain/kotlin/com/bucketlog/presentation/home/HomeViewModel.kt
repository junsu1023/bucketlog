package com.bucketlog.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bucketlog.domain.model.Category
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.model.GoalType
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.repository.GoalRepository
import com.bucketlog.domain.usecase.AddCheckInEntryUseCase
import com.bucketlog.domain.usecase.AddGoalUseCase
import com.bucketlog.domain.usecase.ObserveGoalOverviewsUseCase
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

class HomeViewModel(
    private val goalRepository: GoalRepository,
    entryRepository: EntryRepository,
    observeGoalOverviews: ObserveGoalOverviewsUseCase,
    private val addCheckInEntry: AddCheckInEntryUseCase,
    private val addGoal: AddGoalUseCase,
) : ViewModel() {

    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val thisYear: Int = today.year

    private val yearFilter = MutableStateFlow<BucketYearFilter>(BucketYearFilter.Year(thisYear))
    private val sortOption = MutableStateFlow(HomeSortOption.RECENT)
    private val categoryFilter = MutableStateFlow<Category?>(null)
    private val checkInDrafts = MutableStateFlow<Map<String, String>>(emptyMap())
    private val hasError = MutableStateFlow(false)

    private val allGoals = goalRepository.observeAll()
    private val inProgressOverviews = observeGoalOverviews(GoalStatus.IN_PROGRESS)

    // H-07: 1년 전 기록이 있으면 우선, 없으면 1개월 전으로 대체(pickThrowback 참고).
    private val throwback: Flow<ThrowbackBanner?> = combine(
        entryRepository.observeEntriesOnDate(today.minus(DatePeriod(years = 1))),
        entryRepository.observeEntriesOnDate(today.minus(DatePeriod(months = 1))),
    ) { yearAgo, monthAgo -> pickThrowback(yearAgo, monthAgo) }

    // combine()은 5개 Flow까지만 람다로 받는다 — G-11 정렬/카테고리 필터를 연도 필터와 하나로 묶어 자리를 아낀다.
    private val filters = combine(yearFilter, sortOption, categoryFilter) { year, sort, category -> Triple(year, sort, category) }

    private val baseState: Flow<HomeUiState> = combine(
        allGoals, inProgressOverviews, filters, checkInDrafts, hasError,
    ) { goals, overviews, (filter, sort, category), drafts, error ->
        // 추억 아카이브 앱이라 데이터가 없는 과거 연도도 항상 넘겨볼 수 있어야 한다 —
        // 목표 데이터에 있는 연도로만 제한하면 몇 년 전 기록을 보러 온 유저가 그 해를 아예 못 고른다.
        val availableYears = (goals.mapNotNull { it.bucketYear } + (thisYear - 4..thisYear))
            .distinct()
            .sortedDescending()
        val inBucket = when (filter) {
            is BucketYearFilter.Year -> goals.filter { it.bucketYear == filter.year }
            BucketYearFilter.Someday -> goals.filter { it.bucketYear == null }
        }
        var filteredOverviews = overviews.filter { overview ->
            when (filter) {
                is BucketYearFilter.Year -> overview.goal.bucketYear == filter.year
                BucketYearFilter.Someday -> overview.goal.bucketYear == null
            }
        }
        if (category != null) {
            filteredOverviews = filteredOverviews.filter { it.goal.category == category }
        }
        filteredOverviews = when (sort) {
            // 최신순: 가장 최근에 기록을 남긴 목표가 위로. 기록이 아직 없으면 만든 시각으로 대체한다.
            HomeSortOption.RECENT ->
                filteredOverviews.sortedByDescending { it.lastRecordedAt ?: it.goal.createdAt }
            // 마감임박순: 마감일이 가까운 목표가 위로. 마감일이 없는 목표는 맨 뒤로 보낸다.
            HomeSortOption.DUE_SOON ->
                filteredOverviews.sortedWith(compareBy(nullsLast()) { it.goal.dueDate })
        }
        HomeUiState(
            yearFilter = filter,
            thisYear = thisYear,
            availableYears = availableYears,
            overviews = filteredOverviews,
            summaryTotal = inBucket.size,
            summaryCompleted = inBucket.count { it.status == GoalStatus.COMPLETED },
            existingTitles = goals.map { it.title }.toSet(),
            checkInDrafts = drafts,
            sortOption = sort,
            categoryFilter = category,
            isLoading = false,
            hasError = error,
        )
    }

    val uiState: StateFlow<HomeUiState> = combine(baseState, throwback) { state, banner ->
        state.copy(throwback = banner)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeUiState(yearFilter = BucketYearFilter.Year(thisYear), thisYear = thisYear),
    )

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.SelectYearFilter -> yearFilter.value = intent.filter
            is HomeIntent.CheckInTextChanged ->
                checkInDrafts.update { it + (intent.goalId to intent.text) }
            is HomeIntent.SubmitCheckIn -> submitCheckIn(intent.goalId)
            is HomeIntent.AddPresetGoal -> addPresetGoal(intent.title, intent.category)
            is HomeIntent.SelectSortOption -> sortOption.value = intent.option
            is HomeIntent.SelectCategoryFilter -> categoryFilter.value = intent.category

            HomeIntent.DismissError -> hasError.value = false
        }
    }

    private fun submitCheckIn(goalId: String) {
        val text = checkInDrafts.value[goalId]?.trim().orEmpty()
        if (text.isBlank()) return
        viewModelScope.launch {
            runCatching { addCheckInEntry(goalId, text) }
                .onSuccess { checkInDrafts.update { it - goalId } }
                .onFailure { hasError.value = true }
        }
    }

    private fun addPresetGoal(title: String, category: Category) {
        if (title in uiState.value.existingTitles) return
        viewModelScope.launch {
            runCatching {
                addGoal(
                    title = title,
                    note = null,
                    category = category,
                    type = GoalType.ONE_TIME,
                    targetCount = null,
                    bucketYear = thisYear,
                )
            }.onFailure { hasError.value = true }
        }
    }
}
