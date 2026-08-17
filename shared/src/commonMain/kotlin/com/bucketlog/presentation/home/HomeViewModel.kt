package com.bucketlog.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.usecase.AddCheckInEntryUseCase
import com.bucketlog.domain.usecase.ObserveGoalOverviewsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val observeGoalOverviews: ObserveGoalOverviewsUseCase,
    private val addCheckInEntry: AddCheckInEntryUseCase,
) : ViewModel() {

    private val statusFilter = MutableStateFlow(GoalStatus.IN_PROGRESS)
    private val checkInDrafts = MutableStateFlow<Map<String, String>>(emptyMap())
    private val hasError = MutableStateFlow(false)

    private val overviews = statusFilter.flatMapLatest { observeGoalOverviews(it) }

    val uiState: StateFlow<HomeUiState> = combine(
        statusFilter, overviews, checkInDrafts, hasError,
    ) { status, overviewList, drafts, error ->
        HomeUiState(
            statusFilter = status,
            overviews = overviewList,
            checkInDrafts = drafts,
            isLoading = false,
            hasError = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.SelectStatusFilter -> statusFilter.value = intent.status
            is HomeIntent.CheckInTextChanged ->
                checkInDrafts.update { it + (intent.goalId to intent.text) }
            is HomeIntent.SubmitCheckIn -> submitCheckIn(intent.goalId)

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
}
