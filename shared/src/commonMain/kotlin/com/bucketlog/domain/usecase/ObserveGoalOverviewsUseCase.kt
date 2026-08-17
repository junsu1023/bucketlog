package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Instant

/** 홈 카드(H-03)에 필요한 목표 + 진행 카운트 + 마지막 기록 시점 + 최근 사진들을 합쳐서 내려준다. */
class ObserveGoalOverviewsUseCase(
    private val goalRepository: GoalRepository,
    private val entryRepository: EntryRepository,
) {
    operator fun invoke(status: GoalStatus): Flow<List<GoalOverview>> = combine(
        goalRepository.observeByStatus(status),
        entryRepository.observeProgressTotals(),
        entryRepository.observeLastRecordedAt(),
        entryRepository.observeRecentPhotoPaths(),
    ) { goals, totals, lastRecorded, recentPhotos ->
        goals.map { goal ->
            GoalOverview(
                goal = goal,
                progressCount = totals[goal.id] ?: 0,
                lastRecordedAt = lastRecorded[goal.id],
                recentPhotoPaths = recentPhotos[goal.id].orEmpty(),
            )
        }
    }
}

data class GoalOverview(
    val goal: Goal,
    val progressCount: Int,
    val lastRecordedAt: Instant?,
    val recentPhotoPaths: List<String>,
)
