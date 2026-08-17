package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.repository.GoalRepository
import kotlin.time.Clock

/** MVP-SCOPE.md G-07: 접어두기. "포기"가 아니라 정리 — 이유는 선택 입력. */
class ArchiveGoalUseCase(private val goalRepository: GoalRepository) {
    suspend operator fun invoke(goalId: String, reason: String?) {
        val goal = requireNotNull(goalRepository.getById(goalId)) { "goal not found: $goalId" }
        check(goal.status == GoalStatus.IN_PROGRESS) { "only IN_PROGRESS goals can be archived" }

        goalRepository.update(
            goal.copy(
                status = GoalStatus.ARCHIVED,
                archivedAt = Clock.System.now(),
                archiveReason = reason?.trim()?.ifBlank { null },
            ),
        )
    }
}
