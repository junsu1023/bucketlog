package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.model.ReminderInterval
import com.bucketlog.domain.repository.GoalRepository
import com.bucketlog.notification.NotificationBudget
import com.bucketlog.notification.NotificationSettingsKeys
import com.bucketlog.notification.SettingsStore
import com.bucketlog.platform.LocalNotification
import com.bucketlog.platform.NotificationType
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant

/**
 * N-03 목표별 리마인더 1건 평가·예약. docs/NOTIFICATIONS.md §2 — 유저가 목표별로 직접 켠
 * 목표 중 주기가 됐고 가장 오래 밀린 것 하나만 고른다. 같은 주에 여러 개가 밀려도
 * NotificationBudget이 주 1회 상한을 지켜준다(§1) — 나머지는 다음 주로 이월하지 않고 스킵한다.
 *
 * 문구는 도메인 계층에서 만들지 않는다(docs/ARCHITECTURE.md §4) — [reminderBody]로 주입받는다.
 */
class ScheduleGoalRemindersUseCase(
    private val goalRepository: GoalRepository,
    private val notificationBudget: NotificationBudget,
    private val settings: SettingsStore,
    private val reminderBody: suspend () -> String,
) {
    suspend operator fun invoke(): Boolean {
        val now = Clock.System.now()
        val target = goalRepository.observeAll().first()
            .filter { it.status == GoalStatus.IN_PROGRESS && it.reminderRule?.enabled == true }
            .map { goal -> goal to nextDueInstant(goal) }
            .filter { (_, due) -> now >= due }
            .minByOrNull { (_, due) -> due }
            ?.first ?: return false

        val preferredHour = settings.getLong(
            NotificationSettingsKeys.NOTIFICATION_HOUR,
            NotificationSettingsKeys.DEFAULT_NOTIFICATION_HOUR.toLong(),
        ).toInt()

        val notification = LocalNotification(
            id = "goal_reminder_${target.id}",
            type = NotificationType.GOAL_REMINDER,
            title = target.title,
            body = reminderBody(),
            scheduledAt = nextOccurrenceOfHour(now, preferredHour),
            deepLink = "bucketlog://goal/${target.id}?focus=checkin",
        )

        val sent = notificationBudget.requestSend(notification)
        if (sent) goalRepository.update(target.copy(reminderLastSentAt = now))
        return sent
    }
}

private fun nextDueInstant(goal: Goal): Instant {
    val last = goal.reminderLastSentAt ?: goal.createdAt
    val intervalDays = when (goal.reminderRule?.interval) {
        ReminderInterval.WEEKLY -> 7
        ReminderInterval.BIWEEKLY -> 14
        ReminderInterval.MONTHLY -> 30
        null -> return Instant.DISTANT_FUTURE
    }
    return last + intervalDays.days
}
