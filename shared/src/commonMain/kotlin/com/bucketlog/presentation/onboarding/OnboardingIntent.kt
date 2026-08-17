package com.bucketlog.presentation.onboarding

import com.bucketlog.domain.model.Category

sealed interface OnboardingIntent {
    data class AddPreset(val title: String, val category: Category) : OnboardingIntent
    data object DismissError : OnboardingIntent
}
