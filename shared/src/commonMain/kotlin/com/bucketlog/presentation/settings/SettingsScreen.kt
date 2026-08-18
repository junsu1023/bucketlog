package com.bucketlog.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.back
import bucketlog.shared.generated.resources.backup_result_confirm
import bucketlog.shared.generated.resources.backup_result_export_failed
import bucketlog.shared.generated.resources.backup_result_export_success
import bucketlog.shared.generated.resources.backup_result_restore_failed
import bucketlog.shared.generated.resources.backup_result_restore_schema_too_new
import bucketlog.shared.generated.resources.backup_result_restore_success
import bucketlog.shared.generated.resources.backup_restore_confirm_action
import bucketlog.shared.generated.resources.backup_restore_confirm_body
import bucketlog.shared.generated.resources.backup_restore_confirm_title
import bucketlog.shared.generated.resources.cancel
import bucketlog.shared.generated.resources.settings_all_notifications
import bucketlog.shared.generated.resources.settings_backup_export
import bucketlog.shared.generated.resources.settings_backup_restore
import bucketlog.shared.generated.resources.settings_backup_section
import bucketlog.shared.generated.resources.settings_hour_format
import bucketlog.shared.generated.resources.settings_notification_hour
import bucketlog.shared.generated.resources.settings_nudge
import bucketlog.shared.generated.resources.settings_title
import com.bucketlog.platform.rememberBackupExporter
import com.bucketlog.platform.rememberBackupImporter
import org.jetbrains.compose.resources.stringResource

private val HOUR_OPTIONS = listOf(9, 12, 18, 20)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    val exportLauncher = rememberBackupExporter { success ->
        viewModel.onIntent(SettingsIntent.ExportFinished(success))
    }
    val importLauncher = rememberBackupImporter { bytes ->
        viewModel.onIntent(SettingsIntent.RestoreBackup(bytes))
    }

    LaunchedEffect(state.pendingExport) {
        state.pendingExport?.let { file ->
            exportLauncher(file.fileName, file.bytes)
            viewModel.onIntent(SettingsIntent.ExportLaunched)
        }
    }
    LaunchedEffect(state.pendingImportLaunch) {
        if (state.pendingImportLaunch) {
            importLauncher()
            viewModel.onIntent(SettingsIntent.ImportLaunched)
        }
    }

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

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            Text(
                text = stringResource(Res.string.settings_backup_section),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            Button(
                onClick = { viewModel.onIntent(SettingsIntent.ExportBackup) },
                enabled = !state.isBackupBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.settings_backup_export))
            }
            OutlinedButton(
                onClick = { viewModel.onIntent(SettingsIntent.RequestRestore) },
                enabled = !state.isBackupBusy,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Text(stringResource(Res.string.settings_backup_restore))
            }
            if (state.isBackupBusy) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (state.showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(SettingsIntent.CancelRestore) },
            title = { Text(stringResource(Res.string.backup_restore_confirm_title)) },
            text = { Text(stringResource(Res.string.backup_restore_confirm_body)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(SettingsIntent.ConfirmRestore) }) {
                    Text(stringResource(Res.string.backup_restore_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(SettingsIntent.CancelRestore) }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }

    state.backupResult?.let { result ->
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(SettingsIntent.DismissBackupResult) },
            text = { Text(backupResultText(result)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(SettingsIntent.DismissBackupResult) }) {
                    Text(stringResource(Res.string.backup_result_confirm))
                }
            },
        )
    }
}

@Composable
private fun backupResultText(result: BackupResultMessage): String = when (result) {
    BackupResultMessage.ExportSuccess -> stringResource(Res.string.backup_result_export_success)
    BackupResultMessage.ExportFailed -> stringResource(Res.string.backup_result_export_failed)
    is BackupResultMessage.RestoreSuccess ->
        stringResource(Res.string.backup_result_restore_success, result.goalCount, result.entryCount)
    BackupResultMessage.RestoreSchemaTooNew -> stringResource(Res.string.backup_result_restore_schema_too_new)
    BackupResultMessage.RestoreFailed -> stringResource(Res.string.backup_result_restore_failed)
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
