package com.bucketlog.presentation.addgoal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bucketlog.domain.model.GoalType
import com.bucketlog.domain.usecase.AddGoalUseCase
import com.bucketlog.domain.usecase.AddProgressEntryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class AddGoalViewModel(
    private val addGoal: AddGoalUseCase,
    private val addProgressEntry: AddProgressEntryUseCase,
) : ViewModel() {

    val thisYear: Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).year

    private val _uiState = MutableStateFlow(AddGoalUiState(bucketYear = thisYear))
    val uiState: StateFlow<AddGoalUiState> = _uiState.asStateFlow()

    fun onIntent(intent: AddGoalIntent) {
        when (intent) {
            is AddGoalIntent.TitleChanged -> _uiState.update { it.copy(title = intent.value) }
            is AddGoalIntent.NoteChanged -> _uiState.update { it.copy(note = intent.value) }
            is AddGoalIntent.CategoryChanged -> _uiState.update { it.copy(category = intent.value) }
            is AddGoalIntent.TypeChanged -> _uiState.update { it.copy(type = intent.value) }
            is AddGoalIntent.TargetCountChanged ->
                _uiState.update { it.copy(targetCountText = intent.value.filter(Char::isDigit)) }
            is AddGoalIntent.BucketYearChanged -> _uiState.update { it.copy(bucketYear = intent.value) }
            is AddGoalIntent.AddPhotos ->
                _uiState.update { it.copy(photoBytes = (it.photoBytes + intent.photoBytes).take(5)) }
            AddGoalIntent.ClearPhotos -> _uiState.update { it.copy(photoBytes = emptyList()) }
            AddGoalIntent.Save -> save()
            AddGoalIntent.DismissError -> _uiState.update { it.copy(hasError = false) }
        }
    }

    /**
     * 화면 진입 직전에 호출한다. koinViewModel()이 이 인스턴스를 화면 재진입마다
     * 재사용하므로(별도 백스택이 없어 ViewModelStoreOwner가 바뀌지 않음), 초기화하지
     * 않으면 이전 입력값과 saved=true가 남아 즉시 Home으로 튕겨나간다.
     */
    fun resetForm() {
        _uiState.value = AddGoalUiState(bucketYear = thisYear)
    }

    private fun save() {
        val state = _uiState.value
        if (!state.canSave || state.isSaving) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching {
                val goal = addGoal(
                    title = state.title,
                    note = state.note.ifBlank { null },
                    category = state.category,
                    type = state.type,
                    targetCount = if (state.type == GoalType.REPEATABLE) state.targetCountText.toIntOrNull() else null,
                    bucketYear = state.bucketYear,
                )
                // 사진은 Goal이 아니라 Entry에 붙는다(docs/DATA-MODEL.md) — 목표 생성 직후
                // 첫 진행 기록(E-02)을 하나 만들어 사진을 담는다. 카운트는 올리지 않는다.
                if (state.photoBytes.isNotEmpty()) {
                    addProgressEntry(goal.id, memo = null, photoBytes = state.photoBytes, incrementCount = false)
                }
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, saved = true) }
            }.onFailure {
                _uiState.update { it.copy(isSaving = false, hasError = true) }
            }
        }
    }
}
