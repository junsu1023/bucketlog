package com.bucketlog.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.usecase.AddCheckInEntryUseCase
import com.bucketlog.domain.usecase.AddProgressEntryUseCase
import com.bucketlog.domain.usecase.ArchiveGoalUseCase
import com.bucketlog.domain.usecase.CompleteGoalUseCase
import com.bucketlog.domain.usecase.DeleteGoalUseCase
import com.bucketlog.domain.usecase.ObserveGoalOverviewsUseCase
import com.bucketlog.domain.usecase.RestoreGoalUseCase
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
    private val addProgressEntry: AddProgressEntryUseCase,
    private val completeGoal: CompleteGoalUseCase,
    private val archiveGoal: ArchiveGoalUseCase,
    private val restoreGoal: RestoreGoalUseCase,
    private val deleteGoal: DeleteGoalUseCase,
) : ViewModel() {

    private val statusFilter = MutableStateFlow(GoalStatus.IN_PROGRESS)
    private val checkInDrafts = MutableStateFlow<Map<String, String>>(emptyMap())
    private val pendingAction = MutableStateFlow<PendingAction?>(null)
    private val hasError = MutableStateFlow(false)

    private val overviews = statusFilter.flatMapLatest { observeGoalOverviews(it) }

    val uiState: StateFlow<HomeUiState> = combine(
        statusFilter, overviews, checkInDrafts, pendingAction, hasError,
    ) { status, overviewList, drafts, pending, error ->
        HomeUiState(
            statusFilter = status,
            overviews = overviewList,
            checkInDrafts = drafts,
            pendingAction = pending,
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

            is HomeIntent.RequestComplete ->
                pendingAction.value = PendingAction.ConfirmComplete(intent.goalId, intent.title)
            is HomeIntent.ConfirmComplete -> confirmComplete(intent.retrospect, intent.photoBytes)

            is HomeIntent.RequestArchive ->
                pendingAction.value = PendingAction.ConfirmArchive(intent.goalId, intent.title)
            is HomeIntent.ConfirmArchive -> confirmArchive(intent.reason)

            is HomeIntent.RequestDelete ->
                pendingAction.value = PendingAction.ConfirmDelete(intent.goalId, intent.title)
            HomeIntent.ConfirmDelete -> confirmDelete()

            is HomeIntent.RequestAddProgress ->
                pendingAction.value = PendingAction.AddProgress(intent.goalId, intent.title, intent.isRepeatable)
            is HomeIntent.ConfirmAddProgress ->
                confirmAddProgress(intent.memo, intent.photoBytes, intent.incrementCount)

            is HomeIntent.Restore -> run(goalId = intent.goalId) { restoreGoal(it) }

            HomeIntent.DismissDialog -> pendingAction.value = null
            HomeIntent.DismissError -> hasError.value = false
        }
    }

    private fun submitCheckIn(goalId: String) {
        val text = checkInDrafts.value[goalId]?.trim().orEmpty()
        if (text.isBlank()) return
        viewModelScope.launch {
            runCatching { addCheckInEntry(goalId, text) }
                .onSuccess { checkInDrafts.update { it - goalId } }
                .onFailure(::reportError)
        }
    }

    private fun confirmComplete(retrospect: String?, photoBytes: List<ByteArray>) {
        val goalId = (pendingAction.value as? PendingAction.ConfirmComplete)?.goalId ?: return
        run(goalId = goalId) { completeGoal(it, retrospect, photoBytes) }
    }

    private fun confirmArchive(reason: String?) {
        val goalId = (pendingAction.value as? PendingAction.ConfirmArchive)?.goalId ?: return
        run(goalId = goalId) { archiveGoal(it, reason) }
    }

    private fun confirmDelete() {
        val goalId = (pendingAction.value as? PendingAction.ConfirmDelete)?.goalId ?: return
        run(goalId = goalId) { deleteGoal(it) }
    }

    private fun confirmAddProgress(memo: String?, photoBytes: List<ByteArray>, incrementCount: Boolean) {
        val goalId = (pendingAction.value as? PendingAction.AddProgress)?.goalId ?: return
        run(goalId = goalId) { addProgressEntry(it, memo, photoBytes, incrementCount) }
    }

    private inline fun run(goalId: String, crossinline block: suspend (String) -> Unit) {
        viewModelScope.launch {
            runCatching { block(goalId) }
                .onSuccess { pendingAction.value = null }
                .onFailure(::reportError)
        }
    }

    private fun reportError(t: Throwable) {
        hasError.value = true
    }
}
