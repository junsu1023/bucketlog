package com.bucketlog.presentation.addgoal

import com.bucketlog.domain.model.Category
import com.bucketlog.domain.model.GoalType

data class AddGoalUiState(
    val title: String = "",
    val note: String = "",
    val category: Category = Category.TRAVEL,
    val type: GoalType = GoalType.ONE_TIME,
    val targetCountText: String = "",
    val bucketYear: Int? = null,
    val isSaving: Boolean = false,
    val hasError: Boolean = false,
    val saved: Boolean = false,
) {
    val canSave: Boolean
        get() = title.isNotBlank() && (type == GoalType.ONE_TIME || targetCountText.toIntOrNull()?.let { it > 0 } == true)
}
