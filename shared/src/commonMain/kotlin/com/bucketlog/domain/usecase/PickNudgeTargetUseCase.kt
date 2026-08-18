package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.repository.GoalRepository
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant

/**
 * N-02 스마트 넛지 대상 선정. docs/NOTIFICATIONS.md §2 알고리즘 그대로.
 * "언젠가"(bucketYear == null)는 유저가 의도적으로 기한을 안 둔 것이라 넛지하지 않는다.
 */
class PickNudgeTargetUseCase(
    private val goalRepository: GoalRepository,
    private val entryRepository: EntryRepository,
) {
    suspend operator fun invoke(now: Instant): Goal? {
        val goals = goalRepository.observeAll().first()
        val lastEntryAt = entryRepository.observeLastRecordedAt().first()

        return goals
            .filter { it.status == GoalStatus.IN_PROGRESS && it.bucketYear != null }
            .filter { it.nudgeSnoozedUntil == null || it.nudgeSnoozedUntil < now }
            // 기록이 하나도 없으면 목표 생성 시점을 마지막 활동으로 본다 — 만들어놓고
            // 30일 넘게 손 안 댄 목표도 "정체"이지 넛지 예외가 아니다.
            .map { goal -> goal to (lastEntryAt[goal.id] ?: goal.createdAt) }
            .filter { (_, last) -> now - last >= 30.days }
            .minByOrNull { (_, last) -> last }
            ?.first
    }
}
