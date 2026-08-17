package com.bucketlog.domain.usecase

import com.bucketlog.domain.repository.GoalRepository

/** MVP-SCOPE.md G-05: 삭제. 확인 다이얼로그는 UI 책임. entries/photos는 FK CASCADE로 함께 삭제된다. */
class DeleteGoalUseCase(private val goalRepository: GoalRepository) {
    suspend operator fun invoke(goalId: String) = goalRepository.delete(goalId)
}
