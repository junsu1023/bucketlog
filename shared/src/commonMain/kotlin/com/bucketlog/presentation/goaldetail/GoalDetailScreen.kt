package com.bucketlog.presentation.goaldetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.action_add_progress
import bucketlog.shared.generated.resources.action_archive
import bucketlog.shared.generated.resources.action_complete
import bucketlog.shared.generated.resources.action_delete
import bucketlog.shared.generated.resources.action_restore
import bucketlog.shared.generated.resources.archive_dialog_body
import bucketlog.shared.generated.resources.archive_dialog_title
import bucketlog.shared.generated.resources.archive_reason_placeholder
import bucketlog.shared.generated.resources.back
import bucketlog.shared.generated.resources.cancel
import bucketlog.shared.generated.resources.check_in_placeholder
import bucketlog.shared.generated.resources.check_in_save
import bucketlog.shared.generated.resources.complete_dialog_body
import bucketlog.shared.generated.resources.delete_dialog_body
import bucketlog.shared.generated.resources.delete_dialog_title
import bucketlog.shared.generated.resources.entry_count
import bucketlog.shared.generated.resources.error_generic
import bucketlog.shared.generated.resources.progress_count
import bucketlog.shared.generated.resources.progress_dialog_title
import bucketlog.shared.generated.resources.progress_increment_count
import bucketlog.shared.generated.resources.progress_memo_placeholder
import bucketlog.shared.generated.resources.progress_save
import bucketlog.shared.generated.resources.relative_days_ago
import bucketlog.shared.generated.resources.relative_today
import bucketlog.shared.generated.resources.relative_yesterday
import bucketlog.shared.generated.resources.retrospect_label
import bucketlog.shared.generated.resources.timeline_empty
import bucketlog.shared.generated.resources.timeline_goal_created
import coil3.compose.AsyncImage
import com.bucketlog.domain.model.EntryKind
import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.model.GoalType
import com.bucketlog.platform.rememberCameraCapture
import com.bucketlog.platform.rememberPhotoPicker
import com.bucketlog.presentation.common.PhotoAttachRow
import com.bucketlog.presentation.theme.MonoLabel
import kotlin.time.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(viewModel: GoalDetailViewModel, onBack: () -> Unit, focusCheckIn: Boolean = false) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.deleted) {
        if (state.deleted) onBack()
    }

    GoalDetailContent(state = state, onIntent = viewModel::onIntent, onBack = onBack, focusCheckIn = focusCheckIn)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun GoalDetailContent(
    state: GoalDetailUiState,
    onIntent: (GoalDetailIntent) -> Unit,
    onBack: () -> Unit,
    focusCheckIn: Boolean,
) {
    val goal = state.goal

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(goal?.title.orEmpty(), maxLines = 1) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(stringResource(Res.string.back)) }
                },
            )
        },
        bottomBar = {
            if (goal != null) {
                GoalDetailBottomBar(goal = goal, state = state, onIntent = onIntent, focusCheckIn = focusCheckIn)
            }
        },
    ) { padding ->
        if (goal == null) return@Scaffold

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // D-04: 반복형은 목표치 대비 진행량(3/12), 한 번 하기는 누적 기록 수를 보여준다.
            if (goal.type == GoalType.REPEATABLE && goal.targetCount != null) {
                Text(
                    text = stringResource(Res.string.progress_count, state.progressCount, goal.targetCount),
                    style = MaterialTheme.typography.bodyMedium.merge(MonoLabel),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            } else if (state.timeline.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.entry_count, state.timeline.size),
                    style = MaterialTheme.typography.bodyMedium.merge(MonoLabel),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            if (state.timeline.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(Res.string.timeline_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    items(state.timeline, key = { it.entry.id }) { timelineEntry ->
                        TimelineNode(timelineEntry = timelineEntry, retrospect = goal.retrospect)
                    }
                    item(key = "goal-created") {
                        TimelineCreatedNode(createdAt = goal.createdAt, isLast = true)
                    }
                }
            }
        }
    }

    state.pendingAction?.let { pending ->
        ActionDialog(pending = pending, goalTitle = goal?.title.orEmpty(), onIntent = onIntent)
    }

    if (state.hasError) {
        AlertDialog(
            onDismissRequest = { onIntent(GoalDetailIntent.DismissError) },
            text = { Text(stringResource(Res.string.error_generic)) },
            confirmButton = {
                TextButton(onClick = { onIntent(GoalDetailIntent.DismissError) }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoalDetailBottomBar(
    goal: Goal,
    state: GoalDetailUiState,
    onIntent: (GoalDetailIntent) -> Unit,
    focusCheckIn: Boolean,
) {
    val checkInFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // N-02 스마트 넛지 딥링크(focus=checkin) — "탭 후 경험이 이 기능의 전부"(docs/NOTIFICATIONS.md §2).
    LaunchedEffect(focusCheckIn, goal.status) {
        if (focusCheckIn && goal.status == GoalStatus.IN_PROGRESS) {
            checkInFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Surface(tonalElevation = 3.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            when (goal.status) {
                GoalStatus.IN_PROGRESS -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = state.checkInDraft,
                            onValueChange = { onIntent(GoalDetailIntent.CheckInTextChanged(it)) },
                            placeholder = { Text(stringResource(Res.string.check_in_placeholder)) },
                            modifier = Modifier.weight(1f).focusRequester(checkInFocusRequester),
                            singleLine = true,
                        )
                        TextButton(onClick = { onIntent(GoalDetailIntent.SubmitCheckIn) }) {
                            Text(stringResource(Res.string.check_in_save))
                        }
                    }
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { onIntent(GoalDetailIntent.RequestAddProgress) }) {
                            Text(stringResource(Res.string.action_add_progress))
                        }
                        TextButton(onClick = { onIntent(GoalDetailIntent.RequestComplete) }) {
                            Text(stringResource(Res.string.action_complete))
                        }
                        TextButton(onClick = { onIntent(GoalDetailIntent.RequestArchive) }) {
                            Text(stringResource(Res.string.action_archive))
                        }
                        TextButton(onClick = { onIntent(GoalDetailIntent.RequestDelete) }) {
                            Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                GoalStatus.COMPLETED, GoalStatus.ARCHIVED -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { onIntent(GoalDetailIntent.Restore) }) {
                            Text(stringResource(Res.string.action_restore))
                        }
                        TextButton(onClick = { onIntent(GoalDetailIntent.RequestDelete) }) {
                            Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

/** docs/DESIGN.md §4 타임라인: 퀵 체크인=작은 점, 진행 기록=큰 점, 완료=채워진 원+체크 */
@Composable
private fun TimelineNode(timelineEntry: TimelineEntry, retrospect: String?) {
    val entry = timelineEntry.entry

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        TimelineRail(
            nodeSize = when (entry.kind) {
                EntryKind.CHECK_IN -> 8.dp
                EntryKind.PROGRESS -> 14.dp
                EntryKind.COMPLETION -> 18.dp
            },
            filled = true,
            color = when (entry.kind) {
                EntryKind.COMPLETION -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
            },
            showCheck = entry.kind == EntryKind.COMPLETION,
            checkColor = MaterialTheme.colorScheme.onTertiary,
        )

        Column(modifier = Modifier.padding(start = 12.dp, bottom = 20.dp)) {
            Text(
                text = relativeDayLabel(entry.recordedAt),
                style = MaterialTheme.typography.labelMedium.merge(MonoLabel),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            entry.memo?.takeIf { it.isNotBlank() }?.let { memo ->
                Text(text = memo, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 2.dp))
            }
            if (entry.kind == EntryKind.COMPLETION && !retrospect.isNullOrBlank()) {
                Text(
                    text = "${stringResource(Res.string.retrospect_label)} $retrospect",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            if (timelineEntry.photoPaths.isNotEmpty()) {
                // LazyRow는 IntrinsicSize.Min 부모(TimelineRail 세로선) 안에서 측정할 수 없어 일반 Row로 스크롤한다.
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    timelineEntry.photoPaths.forEach { path ->
                        AsyncImage(
                            model = path,
                            contentDescription = entry.memo,
                            modifier = Modifier.size(96.dp).clip(MaterialTheme.shapes.small),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineCreatedNode(createdAt: Instant, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        TimelineRail(nodeSize = 10.dp, filled = false, color = MaterialTheme.colorScheme.outline, isLast = isLast)
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = relativeDayLabel(createdAt),
                style = MaterialTheme.typography.labelMedium.merge(MonoLabel),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(Res.string.timeline_goal_created),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TimelineRail(
    nodeSize: Dp,
    filled: Boolean,
    color: Color,
    showCheck: Boolean = false,
    checkColor: Color = MaterialTheme.colorScheme.onTertiary,
    isLast: Boolean = false,
) {
    Box(modifier = Modifier.width(28.dp).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
        if (!isLast) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
        }
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(nodeSize)
                .background(if (filled) color else Color.Transparent, CircleShape)
                .then(if (!filled) Modifier.border(1.5.dp, color, CircleShape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            if (showCheck) {
                Text(
                    text = "✓",
                    color = checkColor,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun relativeDayLabel(instant: Instant): String {
    val zone = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(zone).date
    val recordedDay = instant.toLocalDateTime(zone).date
    val days = (today.toEpochDays() - recordedDay.toEpochDays()).toInt()
    return when {
        days <= 0 -> stringResource(Res.string.relative_today)
        days == 1 -> stringResource(Res.string.relative_yesterday)
        else -> stringResource(Res.string.relative_days_ago, days)
    }
}

@Composable
private fun ActionDialog(pending: PendingAction, goalTitle: String, onIntent: (GoalDetailIntent) -> Unit) {
    when (pending) {
        PendingAction.ConfirmComplete -> {
            var retrospect by remember { mutableStateOf("") }
            var photos by remember { mutableStateOf(listOf<ByteArray>()) }
            val launchCamera = rememberCameraCapture { bytes -> if (bytes != null) photos = (photos + bytes).take(5) }
            val launchGallery = rememberPhotoPicker(maxItems = (5 - photos.size).coerceAtLeast(1)) { newPhotos ->
                photos = (photos + newPhotos).take(5)
            }
            AlertDialog(
                onDismissRequest = { onIntent(GoalDetailIntent.DismissDialog) },
                title = { Text(goalTitle) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = retrospect,
                            onValueChange = { retrospect = it },
                            placeholder = { Text(stringResource(Res.string.complete_dialog_body)) },
                        )
                        PhotoAttachRow(
                            photoCount = photos.size,
                            onCameraClick = launchCamera,
                            onGalleryClick = launchGallery,
                            onClearClick = { photos = emptyList() },
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { onIntent(GoalDetailIntent.ConfirmComplete(retrospect.ifBlank { null }, photos)) },
                    ) {
                        Text(stringResource(Res.string.action_complete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(GoalDetailIntent.DismissDialog) }) { Text(stringResource(Res.string.cancel)) }
                },
            )
        }
        PendingAction.ConfirmArchive -> {
            var reason by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { onIntent(GoalDetailIntent.DismissDialog) },
                title = { Text(stringResource(Res.string.archive_dialog_title)) },
                text = {
                    Column {
                        Text(stringResource(Res.string.archive_dialog_body))
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            placeholder = { Text(stringResource(Res.string.archive_reason_placeholder)) },
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { onIntent(GoalDetailIntent.ConfirmArchive(reason.ifBlank { null })) }) {
                        Text(stringResource(Res.string.action_archive))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(GoalDetailIntent.DismissDialog) }) { Text(stringResource(Res.string.cancel)) }
                },
            )
        }
        PendingAction.ConfirmDelete -> {
            AlertDialog(
                onDismissRequest = { onIntent(GoalDetailIntent.DismissDialog) },
                title = { Text(stringResource(Res.string.delete_dialog_title)) },
                text = { Text(stringResource(Res.string.delete_dialog_body)) },
                confirmButton = {
                    TextButton(onClick = { onIntent(GoalDetailIntent.ConfirmDelete) }) {
                        Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(GoalDetailIntent.DismissDialog) }) { Text(stringResource(Res.string.cancel)) }
                },
            )
        }
        PendingAction.AddProgress -> {
            var memo by remember { mutableStateOf("") }
            var photos by remember { mutableStateOf(listOf<ByteArray>()) }
            var incrementCount by remember { mutableStateOf(true) }
            val launchCamera = rememberCameraCapture { bytes -> if (bytes != null) photos = (photos + bytes).take(5) }
            val launchGallery = rememberPhotoPicker(maxItems = (5 - photos.size).coerceAtLeast(1)) { newPhotos ->
                photos = (photos + newPhotos).take(5)
            }
            AlertDialog(
                onDismissRequest = { onIntent(GoalDetailIntent.DismissDialog) },
                title = { Text(stringResource(Res.string.progress_dialog_title)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = memo,
                            onValueChange = { memo = it },
                            placeholder = { Text(stringResource(Res.string.progress_memo_placeholder)) },
                        )
                        PhotoAttachRow(
                            photoCount = photos.size,
                            onCameraClick = launchCamera,
                            onGalleryClick = launchGallery,
                            onClearClick = { photos = emptyList() },
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Checkbox(checked = incrementCount, onCheckedChange = { incrementCount = it })
                            Text(stringResource(Res.string.progress_increment_count), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onIntent(GoalDetailIntent.ConfirmAddProgress(memo.ifBlank { null }, photos, incrementCount))
                        },
                        enabled = memo.isNotBlank() || photos.isNotEmpty(),
                    ) {
                        Text(stringResource(Res.string.progress_save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(GoalDetailIntent.DismissDialog) }) { Text(stringResource(Res.string.cancel)) }
                },
            )
        }
    }
}
