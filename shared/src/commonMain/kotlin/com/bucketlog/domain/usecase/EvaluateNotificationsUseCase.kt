package com.bucketlog.domain.usecase

/**
 * docs/NOTIFICATIONS.md §1 우선순위 중재. 같은 주에 여러 알림이 경합하면 하나만 보낸다 —
 * 순서는 연말회고 > 마감임박 > 월간회고 > 목표별리마인더 > 스마트넛지.
 * 앞선 후보가 NotificationBudget을 통과해 실제로 예약되면(true) 뒤 후보는 시도하지 않는다 —
 * 어차피 예산이 이번 주 몫을 다 썼으니 뒤 usecase를 불러도 항상 false가 나온다.
 */
class EvaluateNotificationsUseCase(
    private val scheduleYearEndRecap: ScheduleYearEndRecapUseCase,
    private val scheduleDueSoon: ScheduleDueSoonUseCase,
    private val scheduleMonthlyRecap: ScheduleMonthlyRecapUseCase,
    private val scheduleGoalReminders: ScheduleGoalRemindersUseCase,
    private val scheduleNudge: ScheduleNudgeUseCase,
) {
    suspend operator fun invoke() {
        if (scheduleYearEndRecap()) return
        if (scheduleDueSoon()) return
        if (scheduleMonthlyRecap()) return
        if (scheduleGoalReminders()) return
        scheduleNudge()
    }
}
