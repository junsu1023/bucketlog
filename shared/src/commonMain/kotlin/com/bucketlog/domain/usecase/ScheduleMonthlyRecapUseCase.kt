package com.bucketlog.domain.usecase

import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.notification.NotificationBudget
import com.bucketlog.notification.NotificationSettingsKeys
import com.bucketlog.notification.SettingsStore
import com.bucketlog.platform.LocalNotification
import com.bucketlog.platform.NotificationType
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * N-01 월간 회고. docs/NOTIFICATIONS.md §2 — 이번 달 말일에 "다음 1회분"을 다시 계산해 예약한다
 * (앱 실행 시 + 주 1회 백그라운드에서 이 usecase가 매번 재평가하므로 별도 스케줄러가 필요 없다).
 *
 * 문구는 도메인 계층에서 만들지 않는다(docs/ARCHITECTURE.md §4) — [recapTitle]/[recapBody]로 주입받는다.
 * [cancelNotification]은 NotificationBudget의 스케줄 람다와 같은 이유로 NotificationScheduler
 * 전체가 아니라 "취소 1건"만 좁혀 받는다 — commonTest에서 플랫폼 스케줄러 없이 검증할 수 있다.
 */
class ScheduleMonthlyRecapUseCase(
    private val entryRepository: EntryRepository,
    private val notificationBudget: NotificationBudget,
    private val settings: SettingsStore,
    private val cancelNotification: suspend (id: String) -> Unit,
    private val recapTitle: suspend () -> String,
    private val recapBody: suspend (month: Int) -> String,
) {
    suspend operator fun invoke(): Boolean {
        val now = Clock.System.now()
        val zone = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(zone).date
        val id = monthlyRecapId(today.year, today.monthNumber)

        // 기록이 0건인 달엔 보내지 않는다(§2 N-01) — 이전에 예약해둔 게 있으면 취소.
        val hasEntries = entryRepository.observeEntriesInMonth(today.year, today.monthNumber).first().isNotEmpty()
        if (!hasEntries) {
            cancelNotification(id)
            return false
        }

        val preferredHour = settings.getLong(
            NotificationSettingsKeys.NOTIFICATION_HOUR,
            NotificationSettingsKeys.DEFAULT_NOTIFICATION_HOUR.toLong(),
        ).toInt()

        val lastDay = lastDayOfMonth(today.year, today.monthNumber)
        val scheduledAt = LocalDateTime(lastDay, LocalTime(preferredHour, 0)).toInstant(zone)

        val notification = LocalNotification(
            id = id,
            type = NotificationType.MONTHLY_RECAP,
            title = recapTitle(),
            body = recapBody(today.monthNumber),
            scheduledAt = scheduledAt,
            deepLink = "bucketlog://archive?month=${monthKeyString(today.year, today.monthNumber)}",
        )
        return notificationBudget.requestSend(notification)
    }
}

internal fun monthlyRecapId(year: Int, month: Int): String = "monthly_recap_${monthKeyString(year, month)}"

internal fun monthKeyString(year: Int, month: Int): String = "$year-${month.toString().padStart(2, '0')}"

private fun lastDayOfMonth(year: Int, month: Int): LocalDate {
    val nextMonthFirstDay = if (month == 12) LocalDate(year + 1, 1, 1) else LocalDate(year, month + 1, 1)
    return nextMonthFirstDay.minus(1, DateTimeUnit.DAY)
}
