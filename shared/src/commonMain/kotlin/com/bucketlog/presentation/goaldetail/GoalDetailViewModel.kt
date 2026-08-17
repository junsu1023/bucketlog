package com.bucketlog.presentation.goaldetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.repository.GoalRepository
import com.bucketlog.domain.usecase.AddCheckInEntryUseCase
import com.bucketlog.domain.usecase.AddProgressEntryUseCase
import com.bucketlog.domain.usecase.ArchiveGoalUseCase
import com.bucketlog.domain.usecase.CompleteGoalUseCase
import com.bucketlog.domain.usecase.DeleteGoalUseCase
import com.bucketlog.domain.usecase.RestoreGoalUseCase
import com.bucketlog.platform.FileStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 목표 상세 — 세로 타임라인(D-01) + 퀵 체크인(D-02) + 액션 바(D-03). MVP-SCOPE.md §2.4 */
class GoalDetailViewModel(
    private val goalId: String,
    private val goalRepository: GoalRepository,
    private val entryRepository: EntryRepository,
    private val fileStorage: FileStorage,
    private val addCheckInEntry: AddCheckInEntryUseCase,
    private val addProgressEntry: AddProgressEntryUseCase,
    private val completeGoal: CompleteGoalUseCase,
    private val archiveGoal: ArchiveGoalUseCase,
    private val restoreGoal: RestoreGoalUseCase,
    private val deleteGoal: DeleteGoalUseCase,
) : ViewModel() {

    private val checkInDraft = MutableStateFlow("")
    private val pendingAction = MutableStateFlow<PendingAction?>(null)
    private val hasError = MutableStateFlow(false)
    private val deleted = MutableStateFlow(false)

    private val base = combine(
        goalRepository.observeById(goalId),
        entryRepository.observeByGoal(goalId),
        checkInDraft,
        pendingAction,
        hasError,
    ) { goal, entries, draft, pending, error ->
        GoalDetailUiState(
            goal = goal,
            timeline = entries.map { entry ->
                TimelineEntry(
                    entry = entry,
                    photoPaths = entry.photos.map { "file://" + fileStorage.resolveAbsolutePath(it.thumbnailPath) },
                )
            },
            progressCount = entries.sumOf { it.countDelta },
            checkInDraft = draft,
            pendingAction = pending,
            isLoading = false,
            hasError = error,
        )
    }

    val uiState: StateFlow<GoalDetailUiState> = combine(base, deleted) { state, isDeleted ->
        state.copy(deleted = isDeleted)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalDetailUiState())

    fun onIntent(intent: GoalDetailIntent) {
        when (intent) {
            is GoalDetailIntent.CheckInTextChanged -> checkInDraft.value = intent.text
            GoalDetailIntent.SubmitCheckIn -> submitCheckIn()

            GoalDetailIntent.RequestComplete -> pendingAction.value = PendingAction.ConfirmComplete
            is GoalDetailIntent.ConfirmComplete -> confirmComplete(intent.retrospect, intent.photoBytes)

            GoalDetailIntent.RequestArchive -> pendingAction.value = PendingAction.ConfirmArchive
            is GoalDetailIntent.ConfirmArchive -> confirmArchive(intent.reason)

            GoalDetailIntent.RequestDelete -> pendingAction.value = PendingAction.ConfirmDelete
            GoalDetailIntent.ConfirmDelete -> confirmDelete()

            GoalDetailIntent.RequestAddProgress -> pendingAction.value = PendingAction.AddProgress
            is GoalDetailIntent.ConfirmAddProgress -> confirmAddProgress(intent.memo, intent.photoBytes, intent.incrementCount)

            GoalDetailIntent.Restore -> run { restoreGoal(goalId) }

            GoalDetailIntent.DismissDialog -> pendingAction.value = null
            GoalDetailIntent.DismissError -> hasError.value = false
        }
    }

    private fun submitCheckIn() {
        val text = checkInDraft.value.trim()
        if (text.isBlank()) return
        viewModelScope.launch {
            runCatching { addCheckInEntry(goalId, text) }
                .onSuccess { checkInDraft.value = "" }
                .onFailure(::reportError)
        }
    }

    private fun confirmComplete(retrospect: String?, photoBytes: List<ByteArray>) {
        if (pendingAction.value !is PendingAction.ConfirmComplete) return
        run { completeGoal(goalId, retrospect, photoBytes) }
    }

    private fun confirmArchive(reason: String?) {
        if (pendingAction.value !is PendingAction.ConfirmArchive) return
        run { archiveGoal(goalId, reason) }
    }

    private fun confirmAddProgress(memo: String?, photoBytes: List<ByteArray>, incrementCount: Boolean) {
        if (pendingAction.value !is PendingAction.AddProgress) return
        run { addProgressEntry(goalId, memo, photoBytes, incrementCount) }
    }

    private fun confirmDelete() {
        if (pendingAction.value !is PendingAction.ConfirmDelete) return
        viewModelScope.launch {
            runCatching { deleteGoal(goalId) }
                .onSuccess {
                    pendingAction.value = null
                    deleted.value = true
                }
                .onFailure(::reportError)
        }
    }

    private inline fun run(crossinline block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }
                .onSuccess { pendingAction.value = null }
                .onFailure(::reportError)
        }
    }

    private fun reportError(t: Throwable) {
        hasError.value = true
    }
}
