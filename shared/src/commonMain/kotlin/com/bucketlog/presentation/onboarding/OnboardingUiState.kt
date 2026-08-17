package com.bucketlog.presentation.onboarding

data class OnboardingUiState(
    val addedTitles: Set<String> = emptySet(),
    val hasError: Boolean = false,
)
