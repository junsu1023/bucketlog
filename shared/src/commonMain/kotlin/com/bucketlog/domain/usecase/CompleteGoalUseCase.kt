package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.Entry
import com.bucketlog.domain.model.EntryKind
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.repository.GoalRepository
import com.bucketlog.domain.util.newId
import kotlin.time.Clock

/**
 * MVP-SCOPE.md G-06/E-07: 완료 처리. 사진 파이프라인(2주차) 전이므로 이번 주차는 회고 텍스트만 받는다.
 * docs/DATA-MODEL.md §5: COMPLETION Entry 생성 + completedAt/retrospect 기록.
 */
class CompleteGoalUseCase(
    private val goalRepository: GoalRepository,
    private val entryRepository: EntryRepository,
) {
    suspend operator fun invoke(goalId: String, retrospect: String?) {
        val goal = requireNotNull(goalRepository.getById(goalId)) { "goal not found: $goalId" }
        check(goal.status == GoalStatus.IN_PROGRESS) { "only IN_PROGRESS goals can be completed" }

        val now = Clock.System.now()
        val trimmedRetrospect = retrospect?.trim()?.ifBlank { null }

        entryRepository.add(
            Entry(
                id = newId(),
                goalId = goalId,
                kind = EntryKind.COMPLETION,
                memo = trimmedRetrospect,
                photos = emptyList(),
                countDelta = 0,
                recordedAt = now,
                createdAt = now,
            ),
        )
        goalRepository.update(
            goal.copy(
                status = GoalStatus.COMPLETED,
                completedAt = now,
                retrospect = trimmedRetrospect,
            ),
        )
    }
}
