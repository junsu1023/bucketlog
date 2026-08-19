package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.Category
import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.model.GoalType
import com.bucketlog.domain.repository.GoalRepository
import com.bucketlog.domain.util.newId
import kotlin.time.Clock

/** MVP-SCOPE.md G-01~G-04: 제목 + 유형 + 카테고리 + (반복형이면) 목표 횟수 + 버킷 연도. */
class AddGoalUseCase(private val goalRepository: GoalRepository) {
    suspend operator fun invoke(
        title: String,
        note: String?,
        category: Category,
        type: GoalType,
        targetCount: Int?,
        bucketYear: Int?,
    ): Goal {
        require(title.isNotBlank()) { "title must not be blank" }
        require(type == GoalType.ONE_TIME || (targetCount != null && targetCount > 0)) {
            "REPEATABLE goal requires a positive targetCount"
        }
        val goal = Goal(
            id = newId(),
            title = title.trim(),
            note = note?.trim()?.ifBlank { null },
            category = category,
            type = type,
            targetCount = if (type == GoalType.REPEATABLE) targetCount else null,
            status = GoalStatus.IN_PROGRESS,
            bucketYear = bucketYear,
            dueDate = null,
            coverEntryId = null,
            reminderRule = null,
            createdAt = Clock.System.now(),
            completedAt = null,
            retrospect = null,
            archivedAt = null,
            archiveReason = null,
            nudgeSnoozedUntil = null,
            reminderLastSentAt = null,
        )
        goalRepository.add(goal)
        return goal
    }
}
