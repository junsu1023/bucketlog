package com.bucketlog.presentation.settings

import com.bucketlog.presentation.theme.ThemeMode

sealed interface SettingsIntent {
    data class SetThemeMode(val mode: ThemeMode) : SettingsIntent
    data class SetNotificationsEnabled(val enabled: Boolean) : SettingsIntent
    data class SetNudgeEnabled(val enabled: Boolean) : SettingsIntent
    data class SetNotificationHour(val hour: Int) : SettingsIntent

    // 백업/복원(M-02)
    data object ExportBackup : SettingsIntent
    data object ExportLaunched : SettingsIntent
    data class ExportFinished(val success: Boolean) : SettingsIntent
    data object RequestRestore : SettingsIntent
    data object ConfirmRestore : SettingsIntent
    data object CancelRestore : SettingsIntent
    data object ImportLaunched : SettingsIntent
    data class RestoreBackup(val bytes: ByteArray?) : SettingsIntent
    data object DismissBackupResult : SettingsIntent

    // 데이터 초기화(M-03)
    data object RequestReset : SettingsIntent
    data object ConfirmReset : SettingsIntent
    data object CancelReset : SettingsIntent
    data object DismissResetResult : SettingsIntent
}
