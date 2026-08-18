package com.bucketlog.domain.usecase

import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.repository.GoalRepository
import com.bucketlog.notification.NotificationBudget
import com.bucketlog.notification.NotificationSettingsKeys
import com.bucketlog.notification.SettingsStore
import com.bucketlog.platform.LocalNotification
import com.bucketlog.platform.NotificationType
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * N-02 스마트 넛지 1건 평가·예약. docs/ARCHITECTURE.md §7 — 주 1회 백그라운드 작업 +
 * 앱 실행 시 1회(iOS 백그라운드 미보장 대응)에서 이 usecase를 호출한다.
 *
 * 문구는 도메인 계층에서 직접 만들지 않는다(domain은 compose-resources에 의존하면 안 됨,
 * docs/ARCHITECTURE.md §4) — [nudgeBody]로 주입받는다. 실제 문구는
 * strings.xml의 nudge_notification_body ("마지막 기록 후 N일이 지났어요. 요즘 어때요?").
 */
class ScheduleNudgeUseCase(
    private val pickNudgeTarget: PickNudgeTargetUseCase,
    private val entryRepository: EntryRepository,
    private val goalRepository: GoalRepository,
    private val notificationBudget: NotificationBudget,
    private val settings: SettingsStore,
    private val nudgeBody: suspend (daysSinceLastEntry: Int) -> String,
) {
    suspend operator fun invoke() {
        val now = Clock.System.now()
        val goal = pickNudgeTarget(now) ?: return

        // PickNudgeTargetUseCase와 동일한 규칙: 기록이 하나도 없으면 목표 생성 시점을 기준으로 삼는다.
        val lastEntryAt = entryRepository.observeLastRecordedAt().first()[goal.id] ?: goal.createdAt
        val daysSince = (now - lastEntryAt).inWholeDays.toInt()

        val preferredHour = settings.getLong(
            NotificationSettingsKeys.NOTIFICATION_HOUR,
            NotificationSettingsKeys.DEFAULT_NOTIFICATION_HOUR.toLong(),
        ).toInt()

        val notification = LocalNotification(
            id = "nudge_${goal.id}",
            type = NotificationType.NUDGE,
            title = goal.title,
            body = nudgeBody(daysSince),
            // N-06 "알림 받을 시각" 설정을 실제로 반영한다 — 지금 당장이 아니라 다음 그 시각에 보낸다.
            scheduledAt = nextOccurrenceOfHour(now, preferredHour),
            deepLink = "bucketlog://goal/${goal.id}?focus=checkin",
        )

        val sent = notificationBudget.requestSend(notification)
        if (sent) {
            // 2회 연속 무응답 시 4주 제외가 원칙(§2)이지만, 무응답 판정(알림을 열었는지)은
            // 아직 없어 이번 범위에선 "같은 목표 연속 넛지"만 우선 막는다.
            goalRepository.update(goal.copy(nudgeSnoozedUntil = now + 28.days))
        }
    }
}

private fun nextOccurrenceOfHour(now: Instant, hour: Int): Instant {
    val zone = TimeZone.currentSystemDefault()
    val today = now.toLocalDateTime(zone).date
    val candidate = LocalDateTime(today, LocalTime(hour, 0)).toInstant(zone)
    return if (candidate > now) candidate else candidate.plus(1, DateTimeUnit.DAY, zone)
}
