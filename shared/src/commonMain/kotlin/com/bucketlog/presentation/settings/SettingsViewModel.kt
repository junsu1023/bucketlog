package com.bucketlog.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bucketlog.notification.NotificationSettingsKeys
import com.bucketlog.notification.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** N-06 알림 설정. 이번 범위에 실제로 쓰이는 종류(전체/넛지)만 노출한다. */
class SettingsViewModel(private val settings: SettingsStore) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = SettingsUiState(
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
        }
    }

    private fun persistBoolean(key: String, value: Boolean) {
        viewModelScope.launch { settings.setBoolean(key, value) }
    }
}
