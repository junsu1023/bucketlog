package com.bucketlog.presentation.home

import com.bucketlog.domain.model.GoalStatus

sealed interface HomeIntent {
    data class SelectStatusFilter(val status: GoalStatus) : HomeIntent
    data class CheckInTextChanged(val goalId: String, val text: String) : HomeIntent
    data class SubmitCheckIn(val goalId: String) : HomeIntent

    data object DismissError : HomeIntent
}
