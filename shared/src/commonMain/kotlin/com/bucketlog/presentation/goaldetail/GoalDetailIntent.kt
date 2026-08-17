package com.bucketlog.presentation.goaldetail

sealed interface GoalDetailIntent {
    data class CheckInTextChanged(val text: String) : GoalDetailIntent
    data object SubmitCheckIn : GoalDetailIntent

    data object RequestComplete : GoalDetailIntent
    data class ConfirmComplete(val retrospect: String?, val photoBytes: List<ByteArray>) : GoalDetailIntent

    data object RequestArchive : GoalDetailIntent
    data class ConfirmArchive(val reason: String?) : GoalDetailIntent

    data object RequestDelete : GoalDetailIntent
    data object ConfirmDelete : GoalDetailIntent

    data object RequestAddProgress : GoalDetailIntent
    data class ConfirmAddProgress(
        val memo: String?,
        val photoBytes: List<ByteArray>,
        val incrementCount: Boolean,
    ) : GoalDetailIntent

    data object Restore : GoalDetailIntent

    data object DismissDialog : GoalDetailIntent
    data object DismissError : GoalDetailIntent
}
