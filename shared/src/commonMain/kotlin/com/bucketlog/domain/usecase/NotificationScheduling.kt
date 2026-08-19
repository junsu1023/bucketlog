package com.bucketlog.domain.usecase

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/** N-06 "알림 받을 시각" 설정을 반영 — [now] 이후 가장 가까운 [hour]시 정각. */
internal fun nextOccurrenceOfHour(now: Instant, hour: Int): Instant {
    val zone = TimeZone.currentSystemDefault()
    val today = now.toLocalDateTime(zone).date
    val candidate = LocalDateTime(today, LocalTime(hour, 0)).toInstant(zone)
    return if (candidate > now) candidate else candidate.plus(1, DateTimeUnit.DAY, zone)
}
