package com.bucketlog.presentation.addgoal

import com.bucketlog.domain.model.Category
import com.bucketlog.domain.model.GoalType

sealed interface AddGoalIntent {
    data class TitleChanged(val value: String) : AddGoalIntent
    data class NoteChanged(val value: String) : AddGoalIntent
    data class CategoryChanged(val value: Category) : AddGoalIntent
    data class TypeChanged(val value: GoalType) : AddGoalIntent
    data class TargetCountChanged(val value: String) : AddGoalIntent
    data class BucketYearChanged(val value: Int?) : AddGoalIntent
    data class AddPhotos(val photoBytes: List<ByteArray>) : AddGoalIntent
    data object ClearPhotos : AddGoalIntent
    data object Save : AddGoalIntent
    data object DismissError : AddGoalIntent

    data object RequestNotificationPermission : AddGoalIntent
    data object SkipNotificationPermission : AddGoalIntent
}
