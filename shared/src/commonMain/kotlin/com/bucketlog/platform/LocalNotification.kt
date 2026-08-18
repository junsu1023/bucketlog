package com.bucketlog.platform

import kotlinx.datetime.Instant

/** docs/NOTIFICATIONS.md §6. 알림 예약 1건. */
data class LocalNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val scheduledAt: Instant,
    /** bucketlog://goal/{id}?focus=checkin 형태. docs/ARCHITECTURE.md §6 딥링크. */
    val deepLink: String,
)

/**
 * 5종 다 정의해 두되 이번 범위에서 실제로 예약하는 건 [NUDGE]뿐이다 — 나중에 나머지를
 * 추가할 때 enum을 또 건드리지 않기 위함.
 */
enum class NotificationType { MONTHLY_RECAP, NUDGE, GOAL_REMINDER, DUE_SOON, YEAR_END_RECAP }
