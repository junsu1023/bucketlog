package com.bucketlog.presentation.addgoal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.bucketlog.presentation.common.MonoMeta
import com.bucketlog.presentation.common.PillChip
import com.bucketlog.presentation.common.ScreenHeader
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.add_goal_title
import bucketlog.shared.generated.resources.cancel
import bucketlog.shared.generated.resources.edit_goal_title
import bucketlog.shared.generated.resources.error_generic
import bucketlog.shared.generated.resources.goal_bucket_someday
import bucketlog.shared.generated.resources.goal_bucket_this_year
import bucketlog.shared.generated.resources.goal_bucket_year_label
import bucketlog.shared.generated.resources.goal_category_label
import bucketlog.shared.generated.resources.goal_due_date_clear
import bucketlog.shared.generated.resources.goal_due_date_label
import bucketlog.shared.generated.resources.goal_due_date_unset
import bucketlog.shared.generated.resources.goal_note_label
import bucketlog.shared.generated.resources.goal_photo_label
import bucketlog.shared.generated.resources.goal_reminder_interval_biweekly
import bucketlog.shared.generated.resources.goal_reminder_interval_monthly
import bucketlog.shared.generated.resources.goal_reminder_interval_weekly
import bucketlog.shared.generated.resources.goal_reminder_label
import bucketlog.shared.generated.resources.goal_target_count_label
import bucketlog.shared.generated.resources.goal_title_label
import bucketlog.shared.generated.resources.goal_type_label
import bucketlog.shared.generated.resources.goal_type_one_time
import bucketlog.shared.generated.resources.goal_type_repeatable
import bucketlog.shared.generated.resources.notification_permission_allow
import bucketlog.shared.generated.resources.notification_permission_body
import bucketlog.shared.generated.resources.notification_permission_deny
import bucketlog.shared.generated.resources.notification_permission_title
import bucketlog.shared.generated.resources.save
import com.bucketlog.domain.model.Category
import com.bucketlog.domain.model.GoalType
import com.bucketlog.domain.model.ReminderInterval
import com.bucketlog.platform.rememberCameraCapture
import com.bucketlog.platform.rememberPhotoPicker
import com.bucketlog.presentation.common.PhotoAttachRow
import com.bucketlog.presentation.common.labelRes
import kotlin.time.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(viewModel: AddGoalViewModel, onSaved: () -> Unit, onCancel: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    val launchCamera = rememberCameraCapture { bytes ->
        if (bytes != null) viewModel.onIntent(AddGoalIntent.AddPhotos(listOf(bytes)))
    }
    val launchGallery = rememberPhotoPicker(maxItems = (5 - state.photoBytes.size).coerceAtLeast(1)) { photos ->
        viewModel.onIntent(AddGoalIntent.AddPhotos(photos))
    }

    val isEditMode = state.editingGoalId != null

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(modifier = Modifier.fillMaxWidth().padding(padding)) {
            ScreenHeader(
                title = stringResource(if (isEditMode) Res.string.edit_goal_title else Res.string.add_goal_title),
                onBack = onCancel,
                backLabel = stringResource(Res.string.cancel),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(state = rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.onIntent(AddGoalIntent.TitleChanged(it)) },
                label = { Text(stringResource(Res.string.goal_title_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )

            OutlinedTextField(
                value = state.note,
                onValueChange = { viewModel.onIntent(AddGoalIntent.NoteChanged(it)) },
                label = { Text(stringResource(Res.string.goal_note_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            )

            Column {
                MonoMeta(stringResource(Res.string.goal_category_label))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
                    items(Category.entries) { category ->
                        PillChip(
                            label = stringResource(category.labelRes()),
                            selected = state.category == category,
                            onClick = { viewModel.onIntent(AddGoalIntent.CategoryChanged(category)) },
                        )
                    }
                }
            }

            Column {
                MonoMeta(stringResource(Res.string.goal_type_label))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 10.dp),
                ) {
                    PillChip(
                        label = stringResource(Res.string.goal_type_one_time),
                        selected = state.type == GoalType.ONE_TIME,
                        onClick = { viewModel.onIntent(AddGoalIntent.TypeChanged(GoalType.ONE_TIME)) },
                    )
                    PillChip(
                        label = stringResource(Res.string.goal_type_repeatable),
                        selected = state.type == GoalType.REPEATABLE,
                        onClick = { viewModel.onIntent(AddGoalIntent.TypeChanged(GoalType.REPEATABLE)) },
                    )
                }
            }

            if (state.type == GoalType.REPEATABLE) {
                OutlinedTextField(
                    value = state.targetCountText,
                    onValueChange = { viewModel.onIntent(AddGoalIntent.TargetCountChanged(it)) },
                    label = { Text(stringResource(Res.string.goal_target_count_label)) },
                    modifier = Modifier.wrapContentWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }

            Column {
                MonoMeta(stringResource(Res.string.goal_bucket_year_label))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 10.dp),
                ) {
                    PillChip(
                        label = stringResource(Res.string.goal_bucket_this_year),
                        selected = state.bucketYear == viewModel.thisYear,
                        onClick = { viewModel.onIntent(AddGoalIntent.BucketYearChanged(viewModel.thisYear)) },
                    )
                    PillChip(
                        label = stringResource(Res.string.goal_bucket_someday),
                        selected = state.bucketYear == null,
                        onClick = { viewModel.onIntent(AddGoalIntent.BucketYearChanged(null)) },
                    )
                }
            }

            // G-09: 선택 입력. "계약이 아니라 참고선"(docs/NOTIFICATIONS.md) — 안 정해도 저장 가능.
            Column {
                MonoMeta(stringResource(Res.string.goal_due_date_label))
                var showDueDatePicker by remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    TextButton(onClick = { showDueDatePicker = true }) {
                        Text(state.dueDate?.toString() ?: stringResource(Res.string.goal_due_date_unset))
                    }
                    if (state.dueDate != null) {
                        TextButton(onClick = { viewModel.onIntent(AddGoalIntent.DueDateChanged(null)) }) {
                            Text(stringResource(Res.string.goal_due_date_clear))
                        }
                    }
                }
                if (showDueDatePicker) {
                    // Material3 DatePicker의 selectedDateMillis는 UTC 자정 기준이다 — 기기 로컬 타임존으로
                    // 변환하면 UTC보다 뒤쪽 타임존에서 하루 밀려 보인다(오늘이 아니라 어제가 선택된 것처럼
                    // 보이는 버그였음). 초기값도 저장 시 되돌리는 방식과 똑같이 UTC 자정으로 맞춘다.
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = (state.dueDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault()))
                            .atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
                    )
                    DatePickerDialog(
                        onDismissRequest = { showDueDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val date = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
                                        viewModel.onIntent(AddGoalIntent.DueDateChanged(date))
                                    }
                                    showDueDatePicker = false
                                },
                            ) { Text(stringResource(Res.string.save)) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDueDatePicker = false }) { Text(stringResource(Res.string.cancel)) }
                        },
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
            }

            // 사진은 Goal이 아니라 Entry에 붙는 개념이라(docs/DATA-MODEL.md) 수정 모드에서는 안 보여준다.
            if (!isEditMode) {
                Column {
                    MonoMeta(stringResource(Res.string.goal_photo_label))
                    PhotoAttachRow(
                        photoCount = state.photoBytes.size,
                        onCameraClick = launchCamera,
                        onGalleryClick = launchGallery,
                        onClearClick = { viewModel.onIntent(AddGoalIntent.ClearPhotos) },
                    )
                }
            }

            // N-03 목표별 리마인더 — 생성 시점엔 컨텍스트가 약해 수정 모드에서만 노출한다.
            if (isEditMode) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MonoMeta(stringResource(Res.string.goal_reminder_label))
                        Switch(
                            checked = state.reminderEnabled,
                            onCheckedChange = { viewModel.onIntent(AddGoalIntent.ReminderEnabledChanged(it)) },
                        )
                    }
                    if (state.reminderEnabled) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 10.dp),
                        ) {
                            PillChip(
                                label = stringResource(Res.string.goal_reminder_interval_weekly),
                                selected = state.reminderInterval == ReminderInterval.WEEKLY,
                                onClick = { viewModel.onIntent(AddGoalIntent.ReminderIntervalChanged(ReminderInterval.WEEKLY)) },
                            )
                            PillChip(
                                label = stringResource(Res.string.goal_reminder_interval_biweekly),
                                selected = state.reminderInterval == ReminderInterval.BIWEEKLY,
                                onClick = { viewModel.onIntent(AddGoalIntent.ReminderIntervalChanged(ReminderInterval.BIWEEKLY)) },
                            )
                            PillChip(
                                label = stringResource(Res.string.goal_reminder_interval_monthly),
                                selected = state.reminderInterval == ReminderInterval.MONTHLY,
                                onClick = { viewModel.onIntent(AddGoalIntent.ReminderIntervalChanged(ReminderInterval.MONTHLY)) },
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { viewModel.onIntent(AddGoalIntent.Save) },
                enabled = state.canSave && !state.isSaving,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Text(stringResource(Res.string.save))
            }
            }
        }
    }

    if (state.hasError) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(AddGoalIntent.DismissError) },
            text = { Text(stringResource(Res.string.error_generic)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(AddGoalIntent.DismissError) }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }

    // O-03: 첫 목표 등록 직후에만 알림 권한을 묻는다. 거절해도 앱은 그대로 동작하고 재요청은 없다.
    if (state.showNotificationPermissionPrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(AddGoalIntent.SkipNotificationPermission) },
            title = { Text(stringResource(Res.string.notification_permission_title, state.savedGoalTitle)) },
            text = { Text(stringResource(Res.string.notification_permission_body)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(AddGoalIntent.RequestNotificationPermission) }) {
                    Text(stringResource(Res.string.notification_permission_allow))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(AddGoalIntent.SkipNotificationPermission) }) {
                    Text(stringResource(Res.string.notification_permission_deny))
                }
            },
        )
    }
}
