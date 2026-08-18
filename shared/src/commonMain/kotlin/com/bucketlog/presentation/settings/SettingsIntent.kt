package com.bucketlog.presentation.settings

sealed interface SettingsIntent {
    data class SetNotificationsEnabled(val enabled: Boolean) : SettingsIntent
    data class SetNudgeEnabled(val enabled: Boolean) : SettingsIntent
    data class SetNotificationHour(val hour: Int) : SettingsIntent
}
