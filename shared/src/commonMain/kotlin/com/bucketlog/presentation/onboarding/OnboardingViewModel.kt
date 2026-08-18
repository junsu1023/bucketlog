package com.bucketlog.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bucketlog.domain.model.Category
import com.bucketlog.domain.model.GoalType
import com.bucketlog.domain.repository.GoalRepository
import com.bucketlog.domain.usecase.AddGoalUseCase
import com.bucketlog.platform.NotificationScheduler
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** O-01/O-02 — 첫 실행(목표 0개) 판정 + 프리셋 탭하여 바로 추가. MVP-SCOPE.md §2.7 */
class OnboardingViewModel(
    private val goalRepository: GoalRepository,
    private val addGoal: AddGoalUseCase,
    private val notificationScheduler: NotificationScheduler,
) : ViewModel() {

    val thisYear: Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).year

    // App.kt가 최초 화면을 정할 때 한 번만 참고하는 판정값 — 이후 목표가 추가돼도 다시 안 바뀐다.
    private val _shouldShowOnboarding = MutableStateFlow<Boolean?>(null)
    val shouldShowOnboarding: StateFlow<Boolean?> = _shouldShowOnboarding.asStateFlow()

    private val addedTitles = MutableStateFlow<Set<String>>(emptySet())
    private val hasError = MutableStateFlow(false)
    // O-03: 온보딩에서 프리셋을 탭해 첫 목표가 만들어진 순간에도 물어봐야 한다 —
    // AddGoalScreen 경로(직접 입력)만 물어보면 대부분의 첫 목표(프리셋 탭)를 놓친다.
    private val permissionPromptGoalTitle = MutableStateFlow<String?>(null)

    val uiState: StateFlow<OnboardingUiState> = combine(
        addedTitles, hasError, permissionPromptGoalTitle,
    ) { titles, error, promptTitle ->
        OnboardingUiState(
            addedTitles = titles,
            hasError = error,
            showNotificationPermissionPrompt = promptTitle != null,
            permissionPromptGoalTitle = promptTitle.orEmpty(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OnboardingUiState())

    init {
        viewModelScope.launch {
            _shouldShowOnboarding.value = goalRepository.observeAll().first().isEmpty()
        }
    }

    fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.AddPreset -> addPreset(intent.title, intent.category)
            OnboardingIntent.DismissError -> hasError.value = false
            OnboardingIntent.RequestNotificationPermission -> requestNotificationPermission()
            OnboardingIntent.SkipNotificationPermission -> permissionPromptGoalTitle.value = null
        }
    }

    private fun addPreset(title: String, category: Category) {
        if (title in addedTitles.value) return
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
            }.onSuccess {
                addedTitles.update { it + title }
                // docs/NOTIFICATIONS.md §4: 첫 목표 등록 직후에만 — 두 번째 프리셋부턴 안 묻는다.
                if (goalRepository.observeAll().first().size == 1) {
                    permissionPromptGoalTitle.value = title
                }
            }.onFailure { hasError.value = true }
        }
    }

    private fun requestNotificationPermission() {
        viewModelScope.launch {
            runCatching { notificationScheduler.requestPermission() }
            permissionPromptGoalTitle.value = null
        }
    }
}
