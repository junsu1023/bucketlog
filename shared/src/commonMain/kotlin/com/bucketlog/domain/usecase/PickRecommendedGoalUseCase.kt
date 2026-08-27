package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.repository.GoalRepository
import kotlinx.coroutines.flow.first

/**
 * 위젯("오늘의 한 걸음"/"2026년의 나")이 보여줄 "다음으로 해볼까요" 목표 하나를 고른다.
 * [PickNudgeTargetUseCase]와 정렬 기준(가장 오래 방치된 것)은 같지만, "30일 이상 방치"라는
 * 알림 발송 조건은 빼서 항상 하나를 추천한다 — 위젯은 "정체됨을 경고"하는 게 아니라 "오늘 뭐
 * 해볼까"를 옆에서 물어보는 역할이라 조건을 걸면 안 된다(docs/NOTIFICATIONS.md §2 톤 참고).
 * "언젠가" 버킷은 넛지와 같은 이유로 제외한다.
 */
class PickRecommendedGoalUseCase(
    private val goalRepository: GoalRepository,
    private val entryRepository: EntryRepository,
) {
    suspend operator fun invoke(): Goal? {
        val goals = goalRepository.observeAll().first()
        val lastEntryAt = entryRepository.observeLastRecordedAt().first()

        return goals
            .filter { it.status == GoalStatus.IN_PROGRESS && it.bucketYear != null }
            .minByOrNull { goal -> lastEntryAt[goal.id] ?: goal.createdAt }
    }
}
