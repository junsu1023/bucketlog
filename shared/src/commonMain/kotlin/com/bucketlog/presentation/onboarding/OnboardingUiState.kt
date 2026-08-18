package com.bucketlog.presentation.onboarding

data class OnboardingUiState(
    val addedTitles: Set<String> = emptySet(),
    val hasError: Boolean = false,
    /** O-03: 온보딩에서 프리셋을 탭해 첫 목표가 만들어진 직후에만 켜진다. */
    val showNotificationPermissionPrompt: Boolean = false,
    val permissionPromptGoalTitle: String = "",
)
