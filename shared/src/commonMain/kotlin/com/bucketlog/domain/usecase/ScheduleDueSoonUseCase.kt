package com.bucketlog.domain.usecase

import com.bucketlog.notification.NotificationBudget
import com.bucketlog.notification.NotificationSettingsKeys
import com.bucketlog.notification.SettingsStore
import com.bucketlog.platform.LocalNotification
import com.bucketlog.platform.NotificationType
import kotlin.time.Clock

/**
 * N-04 마감 임박. docs/NOTIFICATIONS.md §2 — 마감 7일 전 1회, 대상은 그중 마감이 가장 가까운 것 하나.
 *
 * 구현 노트: 문서는 "목표 저장 시점에 예약"이라고 적혀 있지만, dueDate가 몇 달 뒤인 목표라면 그 먼
 * 미래 시각을 [NotificationBudget]에 바로 넘기면 문제가 생긴다 — 예산은 "이번에 승인한
 * scheduledAt"을 기준으로 다음 알림과의 7일 간격을 재는 전역 슬롯 방식이라(§1), 승인된
 * scheduledAt이 몇 달 뒤면 그때까지 넛지·월간회고 같은 다른 알림이 전부 막혀버린다. 그래서 대신
 * 주 1회 평가 루프(EvaluateNotificationsUseCase)에 편입해 매번 "마감 7일 이내로 들어온 목표가
 * 있는가"를 다시 계산한다 — scheduledAt이 항상 가까운 미래(오늘~내일)라 다른 알림 슬롯을
 * 침범하지 않는다. 알림 id가 goalId 기반이라 재평가돼도 중복 발송되지 않는다(WorkManager unique
 * work REPLACE 정책, NotificationScheduler.android.kt 참고).
 */
class ScheduleDueSoonUseCase(
    private val pickDueSoonGoal: PickDueSoonGoalUseCase,
    private val notificationBudget: NotificationBudget,
    private val settings: SettingsStore,
    private val dueSoonBody: suspend (title: String) -> String,
) {
    suspend operator fun invoke(): Boolean {
        val now = Clock.System.now()
        val target = pickDueSoonGoal() ?: return false

        val preferredHour = settings.getLong(
            NotificationSettingsKeys.NOTIFICATION_HOUR,
            NotificationSettingsKeys.DEFAULT_NOTIFICATION_HOUR.toLong(),
        ).toInt()

        val notification = LocalNotification(
            id = "due_soon_${target.id}",
            type = NotificationType.DUE_SOON,
            title = target.title,
            body = dueSoonBody(target.title),
            scheduledAt = nextOccurrenceOfHour(now, preferredHour),
            deepLink = "bucketlog://goal/${target.id}?focus=checkin",
        )
        return notificationBudget.requestSend(notification)
    }
}
