package com.bucketlog.presentation.home

import com.bucketlog.domain.model.GoalStatus

sealed interface HomeIntent {
    data class SelectStatusFilter(val status: GoalStatus) : HomeIntent
    data class CheckInTextChanged(val goalId: String, val text: String) : HomeIntent
    data class SubmitCheckIn(val goalId: String) : HomeIntent

    data class RequestComplete(val goalId: String, val title: String) : HomeIntent
    data class ConfirmComplete(val retrospect: String?, val photoBytes: List<ByteArray>) : HomeIntent

    data class RequestArchive(val goalId: String, val title: String) : HomeIntent
    data class ConfirmArchive(val reason: String?) : HomeIntent

    data class RequestDelete(val goalId: String, val title: String) : HomeIntent
    data object ConfirmDelete : HomeIntent

    data class RequestAddProgress(val goalId: String, val title: String, val isRepeatable: Boolean) : HomeIntent
    data class ConfirmAddProgress(
        val memo: String?,
        val photoBytes: List<ByteArray>,
        val incrementCount: Boolean,
    ) : HomeIntent

    data class Restore(val goalId: String) : HomeIntent

    data object DismissDialog : HomeIntent
    data object DismissError : HomeIntent
}
