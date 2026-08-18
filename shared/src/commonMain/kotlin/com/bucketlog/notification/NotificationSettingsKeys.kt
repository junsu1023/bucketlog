package com.bucketlog.notification

import com.bucketlog.platform.NotificationType

/** AppSettings에 저장되는 알림 관련 키. SettingsScreen과 NotificationBudget이 공유한다. */
object NotificationSettingsKeys {
    const val NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val NUDGE_ENABLED = "nudge_enabled"
    const val NOTIFICATION_HOUR = "notification_hour"
    const val BUDGET_LAST_SENT_AT = "budget_last_sent_at"
    const val BUDGET_LAST_SENT_TYPE = "budget_last_sent_type"

    const val DEFAULT_NOTIFICATION_HOUR = 20

    /** 종류별 on/off 키. 이번 범위엔 NUDGE만 실제로 쓰이지만 나머지도 미리 정의해 둔다. */
    fun typeEnabledKey(type: NotificationType): String = when (type) {
        NotificationType.NUDGE -> NUDGE_ENABLED
        NotificationType.MONTHLY_RECAP -> "monthly_recap_enabled"
        NotificationType.GOAL_REMINDER -> "goal_reminder_enabled"
        NotificationType.DUE_SOON -> "due_soon_enabled"
        NotificationType.YEAR_END_RECAP -> "year_end_recap_enabled"
    }
}
