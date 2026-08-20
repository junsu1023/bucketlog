package com.bucketlog.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bucketlog.domain.usecase.ExportBackupUseCase
import com.bucketlog.domain.usecase.RestoreBackupUseCase
import com.bucketlog.domain.usecase.RestoreResult
import com.bucketlog.notification.NotificationSettingsKeys
import com.bucketlog.notification.SettingsStore
import com.bucketlog.presentation.theme.ThemeModeStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** M-01 다크모드 + N-06 알림 설정 + M-02 백업/복원. */
class SettingsViewModel(
    private val settings: SettingsStore,
    private val themeModeStore: ThemeModeStore,
    private val exportBackup: ExportBackupUseCase,
    private val restoreBackup: RestoreBackupUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = SettingsUiState(
                themeMode = themeModeStore.mode.value,
                notificationsEnabled = settings.getBoolean(NotificationSettingsKeys.NOTIFICATIONS_ENABLED, true),
                nudgeEnabled = settings.getBoolean(NotificationSettingsKeys.NUDGE_ENABLED, true),
                notificationHour = settings.getLong(
                    NotificationSettingsKeys.NOTIFICATION_HOUR,
                    NotificationSettingsKeys.DEFAULT_NOTIFICATION_HOUR.toLong(),
                ).toInt(),
                isLoading = false,
            )
        }
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SetThemeMode -> {
                _uiState.update { it.copy(themeMode = intent.mode) }
                themeModeStore.setMode(intent.mode)
            }
            is SettingsIntent.SetNotificationsEnabled -> {
                _uiState.update { it.copy(notificationsEnabled = intent.enabled) }
                persistBoolean(NotificationSettingsKeys.NOTIFICATIONS_ENABLED, intent.enabled)
            }
            is SettingsIntent.SetNudgeEnabled -> {
                _uiState.update { it.copy(nudgeEnabled = intent.enabled) }
                persistBoolean(NotificationSettingsKeys.NUDGE_ENABLED, intent.enabled)
            }
            is SettingsIntent.SetNotificationHour -> {
                _uiState.update { it.copy(notificationHour = intent.hour) }
                viewModelScope.launch {
                    settings.setLong(NotificationSettingsKeys.NOTIFICATION_HOUR, intent.hour.toLong())
                }
            }

            SettingsIntent.ExportBackup -> {
                _uiState.update { it.copy(isBackupBusy = true) }
                viewModelScope.launch {
                    val file = exportBackup()
                    _uiState.update { it.copy(isBackupBusy = false, pendingExport = file) }
                }
            }
            SettingsIntent.ExportLaunched -> _uiState.update { it.copy(pendingExport = null) }
            is SettingsIntent.ExportFinished -> _uiState.update {
                it.copy(backupResult = if (intent.success) BackupResultMessage.ExportSuccess else BackupResultMessage.ExportFailed)
            }

            SettingsIntent.RequestRestore -> _uiState.update { it.copy(showRestoreConfirm = true) }
            SettingsIntent.CancelRestore -> _uiState.update { it.copy(showRestoreConfirm = false) }
            SettingsIntent.ConfirmRestore -> _uiState.update {
                it.copy(showRestoreConfirm = false, pendingImportLaunch = true)
            }
            SettingsIntent.ImportLaunched -> _uiState.update { it.copy(pendingImportLaunch = false) }
            is SettingsIntent.RestoreBackup -> {
                val bytes = intent.bytes
                if (bytes == null) return
                _uiState.update { it.copy(isBackupBusy = true) }
                viewModelScope.launch {
                    val result = restoreBackup(bytes)
                    val message = when (result) {
                        is RestoreResult.Success -> BackupResultMessage.RestoreSuccess(result.goalCount, result.entryCount)
                        RestoreResult.SchemaTooNew -> BackupResultMessage.RestoreSchemaTooNew
                        RestoreResult.CorruptFile -> BackupResultMessage.RestoreFailed
                    }
                    _uiState.update { it.copy(isBackupBusy = false, backupResult = message) }
                }
            }
            SettingsIntent.DismissBackupResult -> _uiState.update { it.copy(backupResult = null) }
        }
    }

    private fun persistBoolean(key: String, value: Boolean) {
        viewModelScope.launch { settings.setBoolean(key, value) }
    }
}
