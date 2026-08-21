package com.bucketlog.presentation.goaldetail

import kotlinx.datetime.Instant

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

    // E-04/E-05: 기록(체크인/진행 기록) 날짜 소급 수정 + 삭제.
    data class RequestEditEntry(val entryId: String) : GoalDetailIntent
    data class ConfirmEditEntry(val entryId: String, val memo: String?, val recordedAt: Instant) : GoalDetailIntent
    data class RequestDeleteEntry(val entryId: String) : GoalDetailIntent
    data class ConfirmDeleteEntry(val entryId: String) : GoalDetailIntent

    data object DismissDialog : GoalDetailIntent
    data object DismissError : GoalDetailIntent
}
