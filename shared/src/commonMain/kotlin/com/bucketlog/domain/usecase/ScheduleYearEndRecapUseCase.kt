package com.bucketlog.domain.usecase

import com.bucketlog.notification.NotificationBudget
import com.bucketlog.notification.NotificationSettingsKeys
import com.bucketlog.notification.SettingsStore
import com.bucketlog.platform.LocalNotification
import com.bucketlog.platform.NotificationType
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * N-05 연말 회고. docs/NOTIFICATIONS.md §2 — 12월 중순 1회 + 12월 31일 1회, 탭하면 G-12 이월
 * 플로우로 들어간다. 월간 회고(N-01)와 같은 "고정 일정, 앱 실행/주간 평가마다 재계산" 패턴이다.
 */
class ScheduleYearEndRecapUseCase(
    private val notificationBudget: NotificationBudget,
    private val settings: SettingsStore,
    private val recapTitle: suspend () -> String,
    private val midMonthBody: suspend (year: Int) -> String,
    private val yearEndBody: suspend (year: Int) -> String,
) {
    suspend operator fun invoke(): Boolean {
        val zone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val today = now.toLocalDateTime(zone).date
        if (today.monthNumber != 12) return false

        val preferredHour = settings.getLong(
            NotificationSettingsKeys.NOTIFICATION_HOUR,
            NotificationSettingsKeys.DEFAULT_NOTIFICATION_HOUR.toLong(),
        ).toInt()

        val isMidMonth = today.dayOfMonth <= 15
        val id = if (isMidMonth) "year_end_recap_${today.year}_mid" else "year_end_recap_${today.year}_end"
        val day = if (isMidMonth) 15 else 31
        val body = if (isMidMonth) midMonthBody(today.year) else yearEndBody(today.year)

        val notification = LocalNotification(
            id = id,
            type = NotificationType.YEAR_END_RECAP,
            title = recapTitle(),
            body = body,
            scheduledAt = LocalDateTime(LocalDate(today.year, 12, day), LocalTime(preferredHour, 0)).toInstant(zone),
            deepLink = "bucketlog://retrospect/${today.year}",
        )
        return notificationBudget.requestSend(notification)
    }
}
