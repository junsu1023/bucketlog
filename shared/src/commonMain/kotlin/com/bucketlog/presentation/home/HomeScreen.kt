package com.bucketlog.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
import bucketlog.shared.generated.resources.cancel
import bucketlog.shared.generated.resources.check_in_placeholder
import bucketlog.shared.generated.resources.check_in_save
import bucketlog.shared.generated.resources.complete_dialog_body
import bucketlog.shared.generated.resources.delete_dialog_body
import bucketlog.shared.generated.resources.delete_dialog_title
import bucketlog.shared.generated.resources.empty_archived
import bucketlog.shared.generated.resources.empty_completed
import bucketlog.shared.generated.resources.empty_in_progress
import bucketlog.shared.generated.resources.error_generic
import bucketlog.shared.generated.resources.home_title
import bucketlog.shared.generated.resources.last_recorded
import bucketlog.shared.generated.resources.progress_count
import bucketlog.shared.generated.resources.progress_dialog_title
import bucketlog.shared.generated.resources.progress_increment_count
import bucketlog.shared.generated.resources.progress_memo_placeholder
import bucketlog.shared.generated.resources.progress_save
import bucketlog.shared.generated.resources.relative_days_ago
import bucketlog.shared.generated.resources.relative_today
import bucketlog.shared.generated.resources.relative_yesterday
import coil3.compose.AsyncImage
import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.model.GoalType
import com.bucketlog.domain.usecase.GoalOverview
import com.bucketlog.platform.rememberCameraCapture
import com.bucketlog.platform.rememberPhotoPicker
import com.bucketlog.presentation.common.PhotoAttachRow
import com.bucketlog.presentation.common.filterLabelRes
import com.bucketlog.presentation.common.labelRes
import com.bucketlog.presentation.theme.LocalExtraColors
import com.bucketlog.presentation.theme.MonoLabel
import kotlin.time.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel, onAddGoalClick: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    HomeContent(
        state = state,
        onIntent = viewModel::onIntent,
        onAddGoalClick = onAddGoalClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    onAddGoalClick: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(Res.string.home_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddGoalClick) {
                Text(text = "+", style = MaterialTheme.typography.headlineSmall)
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            StatusFilterRow(selected = state.statusFilter, onSelect = { onIntent(HomeIntent.SelectStatusFilter(it)) })

            if (state.overviews.isEmpty() && !state.isLoading) {
                EmptyState(status = state.statusFilter, modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.overviews, key = { it.goal.id }) { overview ->
                        GoalCard(
                            overview = overview,
                            draftText = state.checkInDrafts[overview.goal.id].orEmpty(),
                            onIntent = onIntent,
                        )
                    }
                }
            }
        }
    }

    state.pendingAction?.let { pending ->
        ActionDialog(pending = pending, onIntent = onIntent)
    }

    if (state.hasError) {
        ErrorDialog(onDismiss = { onIntent(HomeIntent.DismissError) })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusFilterRow(selected: GoalStatus, onSelect: (GoalStatus) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val extraColors = LocalExtraColors.current
        GoalStatus.entries.forEach { status ->
            // docs/DESIGN.md §4: 완료=올리브, 접어둠=중성 회색(빨강 금지). 진행중은 기본 강조색(앰버)을 쓴다.
            val selectedColor = when (status) {
                GoalStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
                GoalStatus.ARCHIVED -> extraColors.archived
                GoalStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
            }
            FilterChip(
                selected = status == selected,
                onClick = { onSelect(status) },
                label = { Text(stringResource(status.filterLabelRes())) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = selectedColor,
                    selectedLabelColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    }
}

@Composable
private fun EmptyState(status: GoalStatus, modifier: Modifier = Modifier) {
    val textRes = when (status) {
        GoalStatus.IN_PROGRESS -> Res.string.empty_in_progress
        GoalStatus.COMPLETED -> Res.string.empty_completed
        GoalStatus.ARCHIVED -> Res.string.empty_archived
    }
    Box(modifier = modifier.padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalCard(
    overview: GoalOverview,
    draftText: String,
    onIntent: (HomeIntent) -> Unit,
) {
    val goal = overview.goal
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        if (overview.recentPhotoPaths.isNotEmpty()) {
            if (overview.recentPhotoPaths.size == 1) {
                AsyncImage(
                    model = overview.recentPhotoPaths.first(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    contentScale = ContentScale.Crop,
                )
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(overview.recentPhotoPaths) { path ->
                        AsyncImage(
                            model = path,
                            contentDescription = null,
                            modifier = Modifier.size(160.dp),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = goal.title, style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AssistChip(onClick = {}, enabled = false, label = { Text(stringResource(goal.category.labelRes())) })
                overview.lastRecordedAt?.let {
                    Text(
                        text = stringResource(Res.string.last_recorded, relativeDayLabel(it)),
                        style = MaterialTheme.typography.bodySmall.merge(MonoLabel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (goal.type == GoalType.REPEATABLE && goal.targetCount != null) {
                    Text(
                        text = stringResource(Res.string.progress_count, overview.progressCount, goal.targetCount),
                        style = MaterialTheme.typography.bodySmall.merge(MonoLabel),
                    )
                }
            }

            when (goal.status) {
                GoalStatus.IN_PROGRESS -> InProgressActions(goal, draftText, onIntent)
                GoalStatus.COMPLETED, GoalStatus.ARCHIVED -> RestoreActions(goal, onIntent)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InProgressActions(goal: Goal, draftText: String, onIntent: (HomeIntent) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = draftText,
            onValueChange = { onIntent(HomeIntent.CheckInTextChanged(goal.id, it)) },
            placeholder = { Text(stringResource(Res.string.check_in_placeholder)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
        TextButton(onClick = { onIntent(HomeIntent.SubmitCheckIn(goal.id)) }) {
            Text(stringResource(Res.string.check_in_save))
        }
    }
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        TextButton(
            onClick = {
                onIntent(HomeIntent.RequestAddProgress(goal.id, goal.title, goal.type == GoalType.REPEATABLE))
            },
        ) {
            Text(stringResource(Res.string.action_add_progress))
        }
        TextButton(onClick = { onIntent(HomeIntent.RequestComplete(goal.id, goal.title)) }) {
            Text(stringResource(Res.string.action_complete))
        }
        TextButton(onClick = { onIntent(HomeIntent.RequestArchive(goal.id, goal.title)) }) {
            Text(stringResource(Res.string.action_archive))
        }
        TextButton(onClick = { onIntent(HomeIntent.RequestDelete(goal.id, goal.title)) }) {
            Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun RestoreActions(goal: Goal, onIntent: (HomeIntent) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = { onIntent(HomeIntent.Restore(goal.id)) }) {
            Text(stringResource(Res.string.action_restore))
        }
        TextButton(onClick = { onIntent(HomeIntent.RequestDelete(goal.id, goal.title)) }) {
            Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error)
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
private fun ActionDialog(pending: PendingAction, onIntent: (HomeIntent) -> Unit) {
    when (pending) {
        is PendingAction.ConfirmComplete -> {
            var retrospect by remember { mutableStateOf("") }
            var photos by remember { mutableStateOf(listOf<ByteArray>()) }
            val launchCamera = rememberCameraCapture { bytes -> if (bytes != null) photos = (photos + bytes).take(5) }
            val launchGallery = rememberPhotoPicker(maxItems = (5 - photos.size).coerceAtLeast(1)) { newPhotos ->
                photos = (photos + newPhotos).take(5)
            }
            AlertDialog(
                onDismissRequest = { onIntent(HomeIntent.DismissDialog) },
                title = { Text(pending.title) },
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
                        onClick = { onIntent(HomeIntent.ConfirmComplete(retrospect.ifBlank { null }, photos)) },
                    ) {
                        Text(stringResource(Res.string.action_complete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(HomeIntent.DismissDialog) }) { Text(stringResource(Res.string.cancel)) }
                },
            )
        }
        is PendingAction.ConfirmArchive -> {
            var reason by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { onIntent(HomeIntent.DismissDialog) },
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
                    TextButton(onClick = { onIntent(HomeIntent.ConfirmArchive(reason.ifBlank { null })) }) {
                        Text(stringResource(Res.string.action_archive))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(HomeIntent.DismissDialog) }) { Text(stringResource(Res.string.cancel)) }
                },
            )
        }
        is PendingAction.ConfirmDelete -> {
            AlertDialog(
                onDismissRequest = { onIntent(HomeIntent.DismissDialog) },
                title = { Text(stringResource(Res.string.delete_dialog_title)) },
                text = { Text(stringResource(Res.string.delete_dialog_body)) },
                confirmButton = {
                    TextButton(onClick = { onIntent(HomeIntent.ConfirmDelete) }) {
                        Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(HomeIntent.DismissDialog) }) { Text(stringResource(Res.string.cancel)) }
                },
            )
        }
        is PendingAction.AddProgress -> {
            var memo by remember { mutableStateOf("") }
            var photos by remember { mutableStateOf(listOf<ByteArray>()) }
            var incrementCount by remember { mutableStateOf(pending.isRepeatable) }
            val launchCamera = rememberCameraCapture { bytes -> if (bytes != null) photos = (photos + bytes).take(5) }
            val launchGallery = rememberPhotoPicker(maxItems = (5 - photos.size).coerceAtLeast(1)) { newPhotos ->
                photos = (photos + newPhotos).take(5)
            }
            AlertDialog(
                onDismissRequest = { onIntent(HomeIntent.DismissDialog) },
                title = { Text(stringResource(Res.string.progress_dialog_title)) },
                text = {
                    Column {
                        Text(pending.title, style = MaterialTheme.typography.bodyMedium)
                        OutlinedTextField(
                            value = memo,
                            onValueChange = { memo = it },
                            placeholder = { Text(stringResource(Res.string.progress_memo_placeholder)) },
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        PhotoAttachRow(
                            photoCount = photos.size,
                            onCameraClick = launchCamera,
                            onGalleryClick = launchGallery,
                            onClearClick = { photos = emptyList() },
                        )
                        if (pending.isRepeatable) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Checkbox(checked = incrementCount, onCheckedChange = { incrementCount = it })
                                Text(stringResource(Res.string.progress_increment_count), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onIntent(HomeIntent.ConfirmAddProgress(memo.ifBlank { null }, photos, incrementCount))
                        },
                        enabled = memo.isNotBlank() || photos.isNotEmpty(),
                    ) {
                        Text(stringResource(Res.string.progress_save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(HomeIntent.DismissDialog) }) { Text(stringResource(Res.string.cancel)) }
                },
            )
        }
    }
}

@Composable
private fun ErrorDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(stringResource(Res.string.error_generic)) },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) } },
    )
}
