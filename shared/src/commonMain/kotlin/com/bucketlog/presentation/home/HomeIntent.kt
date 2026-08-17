package com.bucketlog.presentation.home

import com.bucketlog.domain.model.Category

sealed interface HomeIntent {
    data class SelectYearFilter(val filter: BucketYearFilter) : HomeIntent
    data class CheckInTextChanged(val goalId: String, val text: String) : HomeIntent
    data class SubmitCheckIn(val goalId: String) : HomeIntent

    /** H-06 빈 상태 프리셋 제안 — 탭하면 바로 추가된다. */
    data class AddPresetGoal(val title: String, val category: Category) : HomeIntent

    data object DismissError : HomeIntent
}
