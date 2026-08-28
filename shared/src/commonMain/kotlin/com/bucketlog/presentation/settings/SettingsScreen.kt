package com.bucketlog.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.bucketlog.presentation.common.Hairline
import com.bucketlog.presentation.common.MonoMeta
import com.bucketlog.presentation.common.PillChip
import com.bucketlog.presentation.common.ScreenHeader
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import bucketlog.shared.generated.resources.reset_confirm_action
import bucketlog.shared.generated.resources.reset_confirm_body
import bucketlog.shared.generated.resources.reset_confirm_title
import bucketlog.shared.generated.resources.reset_result_message
import bucketlog.shared.generated.resources.settings_all_notifications
import bucketlog.shared.generated.resources.settings_notifications_section
import bucketlog.shared.generated.resources.settings_backup_export
import bucketlog.shared.generated.resources.settings_backup_restore
import bucketlog.shared.generated.resources.settings_backup_section
import bucketlog.shared.generated.resources.settings_data_section
import bucketlog.shared.generated.resources.settings_due_soon
import bucketlog.shared.generated.resources.settings_hour_format
import bucketlog.shared.generated.resources.settings_notification_hour
import bucketlog.shared.generated.resources.settings_nudge
import bucketlog.shared.generated.resources.settings_reset_all
import bucketlog.shared.generated.resources.settings_rollover
import bucketlog.shared.generated.resources.settings_widget_section
import bucketlog.shared.generated.resources.settings_widget_small_step
import bucketlog.shared.generated.resources.settings_widget_today_memory
import bucketlog.shared.generated.resources.settings_widget_year_progress
import bucketlog.shared.generated.resources.settings_year_end_recap
import com.bucketlog.platform.WidgetKind
import com.bucketlog.platform.rememberWidgetPinner
import bucketlog.shared.generated.resources.settings_theme_dark
import bucketlog.shared.generated.resources.settings_theme_light
import bucketlog.shared.generated.resources.settings_theme_section
import bucketlog.shared.generated.resources.settings_theme_system
import bucketlog.shared.generated.resources.settings_title
import com.bucketlog.platform.rememberBackupExporter
import com.bucketlog.platform.rememberBackupImporter
import com.bucketlog.presentation.theme.ThemeMode
import org.jetbrains.compose.resources.stringResource

private val HOUR_OPTIONS = listOf(9, 12, 18, 20)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: (() -> Unit)? = null, onRolloverClick: () -> Unit = {}) {
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

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
        ScreenHeader(
            title = stringResource(Res.string.settings_title),
            onBack = onBack,
            backLabel = stringResource(Res.string.back),
        )
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 40.dp)) {
            SectionHeader(icon = Icons.Outlined.DarkMode, label = stringResource(Res.string.settings_theme_section))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PillChip(
                    label = stringResource(Res.string.settings_theme_system),
                    selected = state.themeMode == ThemeMode.SYSTEM,
                    onClick = { viewModel.onIntent(SettingsIntent.SetThemeMode(ThemeMode.SYSTEM)) },
                )
                PillChip(
                    label = stringResource(Res.string.settings_theme_light),
                    selected = state.themeMode == ThemeMode.LIGHT,
                    onClick = { viewModel.onIntent(SettingsIntent.SetThemeMode(ThemeMode.LIGHT)) },
                )
                PillChip(
                    label = stringResource(Res.string.settings_theme_dark),
                    selected = state.themeMode == ThemeMode.DARK,
                    onClick = { viewModel.onIntent(SettingsIntent.SetThemeMode(ThemeMode.DARK)) },
                )
            }

            Hairline(modifier = Modifier.padding(vertical = 24.dp))

            SectionHeader(icon = Icons.Outlined.Notifications, label = stringResource(Res.string.settings_notifications_section))
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
            SettingsRow(
                label = stringResource(Res.string.settings_due_soon),
                checked = state.dueSoonEnabled,
                enabled = state.notificationsEnabled,
                onCheckedChange = { viewModel.onIntent(SettingsIntent.SetDueSoonEnabled(it)) },
            )
            SettingsRow(
                label = stringResource(Res.string.settings_year_end_recap),
                checked = state.yearEndRecapEnabled,
                enabled = state.notificationsEnabled,
                onCheckedChange = { viewModel.onIntent(SettingsIntent.SetYearEndRecapEnabled(it)) },
            )

            MonoMeta(
                text = stringResource(Res.string.settings_notification_hour),
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HOUR_OPTIONS.forEach { hour ->
                    PillChip(
                        label = stringResource(Res.string.settings_hour_format, hour),
                        selected = state.notificationHour == hour,
                        enabled = state.notificationsEnabled,
                        onClick = { viewModel.onIntent(SettingsIntent.SetNotificationHour(hour)) },
                    )
                }
            }

            Hairline(modifier = Modifier.padding(vertical = 24.dp))

            SectionHeader(icon = Icons.Outlined.Widgets, label = stringResource(Res.string.settings_widget_section))
            val pinWidget = rememberWidgetPinner()
            OutlinedButton(
                onClick = { pinWidget(WidgetKind.SMALL_STEP) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Text(stringResource(Res.string.settings_widget_small_step))
            }
            OutlinedButton(
                onClick = { pinWidget(WidgetKind.YEAR_PROGRESS) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Text(stringResource(Res.string.settings_widget_year_progress))
            }
            OutlinedButton(
                onClick = { pinWidget(WidgetKind.TODAY_MEMORY) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Text(stringResource(Res.string.settings_widget_today_memory))
            }

            Hairline(modifier = Modifier.padding(vertical = 24.dp))

            SectionHeader(
                icon = Icons.Outlined.CloudUpload,
                label = stringResource(Res.string.settings_backup_section),
                modifier = Modifier.padding(bottom = 4.dp),
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

            Hairline(modifier = Modifier.padding(vertical = 24.dp))

            SectionHeader(
                icon = Icons.Outlined.Storage,
                label = stringResource(Res.string.settings_data_section),
                modifier = Modifier.padding(bottom = 4.dp),
            )
            OutlinedButton(
                onClick = onRolloverClick,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            ) {
                Text(stringResource(Res.string.settings_rollover))
            }
            OutlinedButton(
                onClick = { viewModel.onIntent(SettingsIntent.RequestReset) },
                enabled = !state.isResetting,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.settings_reset_all))
            }
            if (state.isResetting) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
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

    if (state.showResetConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(SettingsIntent.CancelReset) },
            title = { Text(stringResource(Res.string.reset_confirm_title)) },
            text = { Text(stringResource(Res.string.reset_confirm_body)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(SettingsIntent.ConfirmReset) }) {
                    Text(stringResource(Res.string.reset_confirm_action), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(SettingsIntent.CancelReset) }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }

    if (state.resetDone) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(SettingsIntent.DismissResetResult) },
            text = { Text(stringResource(Res.string.reset_result_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(SettingsIntent.DismissResetResult) }) {
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

/** docs/DESIGN.md — 설정 각 섹션 제목 앞에 아이콘을 둔 리스트형 헤더. */
@Composable
private fun SectionHeader(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        MonoMeta(text = label)
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
