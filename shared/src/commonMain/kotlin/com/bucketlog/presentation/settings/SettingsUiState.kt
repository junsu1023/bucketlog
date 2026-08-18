package com.bucketlog.presentation.settings

import com.bucketlog.notification.NotificationSettingsKeys

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val nudgeEnabled: Boolean = true,
    val notificationHour: Int = NotificationSettingsKeys.DEFAULT_NOTIFICATION_HOUR,
    val isLoading: Boolean = true,
)
