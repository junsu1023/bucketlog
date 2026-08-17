package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.repository.GoalRepository

/**
 * MVP-SCOPE.md G-08: 되돌리기. docs/DATA-MODEL.md §5 전이 규칙.
 * - COMPLETED → IN_PROGRESS: COMPLETION Entry를 삭제하지 않고 PROGRESS로 강등(사진/메모 보존)
 * - ARCHIVED → IN_PROGRESS: archivedAt/archiveReason만 지움(이유 텍스트는 보존하지 않음)
 */
class RestoreGoalUseCase(
    private val goalRepository: GoalRepository,
    private val entryRepository: EntryRepository,
) {
    suspend operator fun invoke(goalId: String) {
        val goal = requireNotNull(goalRepository.getById(goalId)) { "goal not found: $goalId" }
        when (goal.status) {
            GoalStatus.COMPLETED -> {
                entryRepository.demoteCompletionEntry(goalId)
                goalRepository.update(
                    goal.copy(status = GoalStatus.IN_PROGRESS, completedAt = null, retrospect = null),
                )
            }
            GoalStatus.ARCHIVED -> {
                goalRepository.update(
                    goal.copy(status = GoalStatus.IN_PROGRESS, archivedAt = null, archiveReason = null),
                )
            }
            GoalStatus.IN_PROGRESS -> Unit
        }
    }
}
