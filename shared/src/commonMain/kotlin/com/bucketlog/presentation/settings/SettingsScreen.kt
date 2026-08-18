package com.bucketlog.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.back
import bucketlog.shared.generated.resources.settings_all_notifications
import bucketlog.shared.generated.resources.settings_hour_format
import bucketlog.shared.generated.resources.settings_notification_hour
import bucketlog.shared.generated.resources.settings_nudge
import bucketlog.shared.generated.resources.settings_title
import org.jetbrains.compose.resources.stringResource

private val HOUR_OPTIONS = listOf(9, 12, 18, 20)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(Res.string.back)) } },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            SettingsRow(
                label = stringResource(Res.string.settings_all_notifications),
                checked = state.notificationsEnabled,
                onCheckedChange = { viewModel.onIntent(SettingsIntent.SetNotificationsEnabled(it)) },
            )
            SettingsRow(
                label = stringResource(Res.string.settings_nudge),
                checked = state.nudgeEnabled,
                enabled = state.notificationsEnabled,
                onCheckedChange = { viewModel.onIntent(SettingsIntent.SetNudgeEnabled(it)) },
            )

            Text(
                text = stringResource(Res.string.settings_notification_hour),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HOUR_OPTIONS.forEach { hour ->
                    FilterChip(
                        selected = state.notificationHour == hour,
                        enabled = state.notificationsEnabled,
                        onClick = { viewModel.onIntent(SettingsIntent.SetNotificationHour(hour)) },
                        label = { Text(stringResource(Res.string.settings_hour_format, hour)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}
