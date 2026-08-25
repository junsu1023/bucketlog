package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.repository.GoalRepository

/**
 * G-12 연말 이월. docs/DATA-MODEL.md §6 — 자동으로 옮기지 않고 유저가 목표별로 고른다.
 * 기록은 절대 지우지 않는다 — bucketYear/status만 바꾸고 Entry는 그대로 따라간다.
 */
class RolloverGoalsUseCase(
    private val goalRepository: GoalRepository,
    private val archiveGoal: ArchiveGoalUseCase,
) {
    suspend operator fun invoke(year: Int, decisions: Map<String, RolloverDecision>) {
        decisions.forEach { (goalId, decision) ->
            if (decision == RolloverDecision.KEEP) return@forEach
            val goal = goalRepository.getById(goalId) ?: return@forEach
            if (goal.status != GoalStatus.IN_PROGRESS) return@forEach
            when (decision) {
                RolloverDecision.NEXT_YEAR -> goalRepository.update(goal.copy(bucketYear = year + 1))
                RolloverDecision.SOMEDAY -> goalRepository.update(goal.copy(bucketYear = null))
                RolloverDecision.ARCHIVE -> archiveGoal(goalId, reason = null)
                RolloverDecision.KEEP -> Unit
            }
        }
    }
}

/** "그대로 두기"는 결정을 안 한 것과 같다 — 지난 해 버킷에 그대로 남는다. */
enum class RolloverDecision { NEXT_YEAR, SOMEDAY, ARCHIVE, KEEP }
