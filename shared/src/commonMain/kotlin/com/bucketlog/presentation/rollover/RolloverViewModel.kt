package com.bucketlog.presentation.rollover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.repository.GoalRepository
import com.bucketlog.domain.usecase.RolloverDecision
import com.bucketlog.domain.usecase.RolloverGoalsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** G-12 연말 이월. [year]에 진행 중이던(IN_PROGRESS) 목표만 대상으로 삼는다. */
class RolloverViewModel(
    private val year: Int,
    private val goalRepository: GoalRepository,
    private val rolloverGoals: RolloverGoalsUseCase,
) : ViewModel() {

    private val decisions = MutableStateFlow<Map<String, RolloverDecision>>(emptyMap())
    private val isSaving = MutableStateFlow(false)
    private val done = MutableStateFlow(false)

    val uiState: StateFlow<RolloverUiState> = combine(
        goalRepository.observeAll(), decisions, isSaving, done,
    ) { goals, decisionMap, saving, isDone ->
        RolloverUiState(
            year = year,
            goals = goals.filter { it.status == GoalStatus.IN_PROGRESS && it.bucketYear == year },
            decisions = decisionMap,
            isLoading = false,
            isSaving = saving,
            done = isDone,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RolloverUiState(year = year))

    fun onIntent(intent: RolloverIntent) {
        when (intent) {
            is RolloverIntent.SelectDecision ->
                decisions.update { it + (intent.goalId to intent.decision) }
            RolloverIntent.Confirm -> confirm()
        }
    }

    private fun confirm() {
        if (isSaving.value) return
        isSaving.value = true
        viewModelScope.launch {
            runCatching { rolloverGoals(year, decisions.value) }
            isSaving.value = false
            done.value = true
        }
    }
}
