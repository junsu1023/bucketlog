package com.bucketlog.notification

import com.bucketlog.platform.LocalNotification
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private const val QUIET_HOURS_START = 21
private const val QUIET_HOURS_END = 9

/**
 * docs/NOTIFICATIONS.md §1 — 모든 알림은 반드시 이걸 거친다. 개별 기능이
 * [NotificationScheduler]를 직접 호출하지 않는다. 그래야 "전체 알림 합계는 주 1회"가
 * 실제로 지켜진다.
 */
class NotificationBudget(
    private val settings: SettingsStore,
    /** com.bucketlog.platform.NotificationScheduler::schedule를 넘겨받는다 — 이 클래스는
     *  스케줄러 전체가 아니라 "예약 1건 보내기" 하나만 필요해서 함수 타입으로 좁혔다.
     *  덕분에 유닛 테스트에서 플랫폼 스케줄러 없이 람다만으로 검증할 수 있다. */
    private val scheduleNotification: suspend (LocalNotification) -> Unit,
) {
    suspend fun requestSend(notification: LocalNotification): Boolean {
        if (!settings.getBoolean(NotificationSettingsKeys.NOTIFICATIONS_ENABLED, true)) return false
        if (!settings.getBoolean(NotificationSettingsKeys.typeEnabledKey(notification.type), true)) return false

        val lastSentAt = settings.getLong(NotificationSettingsKeys.BUDGET_LAST_SENT_AT, 0L)
        if (lastSentAt != 0L) {
            val elapsedMillis = notification.scheduledAt.toEpochMilliseconds() - lastSentAt
            if (elapsedMillis < 7.days.inWholeMilliseconds) return false
        }

        val adjusted = adjustForQuietHours(notification)
        scheduleNotification(adjusted)
        settings.setLong(NotificationSettingsKeys.BUDGET_LAST_SENT_AT, adjusted.scheduledAt.toEpochMilliseconds())
        settings.setString(NotificationSettingsKeys.BUDGET_LAST_SENT_TYPE, notification.type.name)
        return true
    }

    /** 21:00~09:00엔 발송하지 않는다 — 취소가 아니라 다음 허용 시각(09:00)으로 미룬다. §3 */
    private fun adjustForQuietHours(notification: LocalNotification): LocalNotification {
        val zone = TimeZone.currentSystemDefault()
        val local = notification.scheduledAt.toLocalDateTime(zone)
        if (local.hour in QUIET_HOURS_END until QUIET_HOURS_START) return notification

        val nextAllowedDate = if (local.hour >= QUIET_HOURS_START) {
            local.date.plus(DatePeriod(days = 1))
        } else {
            local.date
        }
        val nextAllowed = LocalDateTime(nextAllowedDate, LocalTime(QUIET_HOURS_END, 0)).toInstant(zone)
        return notification.copy(scheduledAt = nextAllowed)
    }
}
