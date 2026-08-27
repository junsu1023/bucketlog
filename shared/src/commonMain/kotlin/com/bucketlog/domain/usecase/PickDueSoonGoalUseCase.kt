package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.repository.GoalRepository
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn

/**
 * 마감 7일 이내(안 지남)인 진행 중 목표 중 가장 임박한 것 하나를 고르는 순수 조회.
 * [ScheduleDueSoonUseCase]의 선정 로직을 분리해뒀다 — 그쪽은 NotificationBudget에 실제로 알림을
 * 예약하는 부수효과가 있어서, "마감 임박" 위젯처럼 화면에 그리기만 하고 싶을 땐 이 순수 버전을
 * 대신 쓴다(PickNudgeTargetUseCase/ScheduleNudgeUseCase와 같은 분리 패턴).
 */
class PickDueSoonGoalUseCase(private val goalRepository: GoalRepository) {
    suspend operator fun invoke(): Goal? {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return goalRepository.observeAll().first()
            .filter { it.status == GoalStatus.IN_PROGRESS && it.dueDate != null }
            .filter { goal -> today.daysUntil(goal.dueDate!!) in 0..7 }
            .minByOrNull { it.dueDate!! }
    }
}
