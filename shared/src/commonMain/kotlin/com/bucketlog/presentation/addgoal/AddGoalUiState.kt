package com.bucketlog.presentation.addgoal

import com.bucketlog.domain.model.Category
import com.bucketlog.domain.model.GoalType
import com.bucketlog.domain.model.ReminderInterval

data class AddGoalUiState(
    val title: String = "",
    val note: String = "",
    val category: Category = Category.TRAVEL,
    val type: GoalType = GoalType.ONE_TIME,
    val targetCountText: String = "",
    val bucketYear: Int? = null,
    val photoBytes: List<ByteArray> = emptyList(),
    val isSaving: Boolean = false,
    val hasError: Boolean = false,
    val saved: Boolean = false,
    /** O-03: 유저의 첫 목표를 저장한 직후에만 켜진다. docs/NOTIFICATIONS.md §4. */
    val showNotificationPermissionPrompt: Boolean = false,
    val savedGoalTitle: String = "",
    /** null이면 생성 모드, 아니면 이 id의 목표를 수정하는 중(G-05). */
    val editingGoalId: String? = null,
    /** N-03 목표별 리마인더. 수정 모드에서만 노출 — "매일" 옵션은 만들지 않는다(docs/NOTIFICATIONS.md §2). */
    val reminderEnabled: Boolean = false,
    val reminderInterval: ReminderInterval = ReminderInterval.WEEKLY,
) {
    val canSave: Boolean
        get() = title.isNotBlank() && (type == GoalType.ONE_TIME || targetCountText.toIntOrNull()?.let { it > 0 } == true)
}
