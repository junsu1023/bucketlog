package com.bucketlog.domain.usecase

import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * 검색 — 상태(진행 중/완료/접어둠) 무관 전체 목표를 제목으로 필터링한다. 별도 인덱스나 저장소
 * 없이 홈 카드가 이미 쓰는 관찰 흐름(ObserveGoalOverviewsUseCase)과 같은 조합을 재사용한다.
 */
class SearchGoalsUseCase(
    private val goalRepository: GoalRepository,
    private val entryRepository: EntryRepository,
) {
    operator fun invoke(query: String): Flow<List<GoalOverview>> = combine(
        goalRepository.observeAll(),
        entryRepository.observeProgressTotals(),
        entryRepository.observeLastRecordedAt(),
        entryRepository.observeRecentPhotoPaths(),
    ) { goals, totals, lastRecorded, recentPhotos ->
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            emptyList()
        } else {
            goals.filter { it.title.contains(trimmed, ignoreCase = true) }
                .map { goal ->
                    GoalOverview(
                        goal = goal,
                        progressCount = totals[goal.id] ?: 0,
                        lastRecordedAt = lastRecorded[goal.id],
                        recentPhotoPaths = recentPhotos[goal.id].orEmpty(),
                    )
                }
        }
    }
}
