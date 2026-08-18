package com.bucketlog.presentation.settings

import com.bucketlog.domain.usecase.BackupFile
import com.bucketlog.notification.NotificationSettingsKeys

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val nudgeEnabled: Boolean = true,
    val notificationHour: Int = NotificationSettingsKeys.DEFAULT_NOTIFICATION_HOUR,
    val isLoading: Boolean = true,
    val isBackupBusy: Boolean = false,
    val showRestoreConfirm: Boolean = false,
    // 화면이 소비하면 즉시 Intent로 지우는 1회성 트리거. ViewModel은 StateFlow<UiState>만 노출한다는
    // 컨벤션(CLAUDE.md §7) 때문에 별도 이벤트 채널 대신 UiState 필드로 피커 실행을 넘긴다.
    val pendingExport: BackupFile? = null,
    val pendingImportLaunch: Boolean = false,
    val backupResult: BackupResultMessage? = null,
)

sealed interface BackupResultMessage {
    data object ExportSuccess : BackupResultMessage
    data object ExportFailed : BackupResultMessage
    data class RestoreSuccess(val goalCount: Int, val entryCount: Int) : BackupResultMessage
    data object RestoreSchemaTooNew : BackupResultMessage
    data object RestoreFailed : BackupResultMessage
}
