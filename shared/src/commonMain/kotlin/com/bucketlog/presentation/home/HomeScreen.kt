package com.bucketlog.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.cancel
import bucketlog.shared.generated.resources.check_in_placeholder
import bucketlog.shared.generated.resources.check_in_save
import bucketlog.shared.generated.resources.empty_archived
import bucketlog.shared.generated.resources.empty_completed
import bucketlog.shared.generated.resources.empty_in_progress
import bucketlog.shared.generated.resources.error_generic
import bucketlog.shared.generated.resources.home_title
import bucketlog.shared.generated.resources.last_recorded
import bucketlog.shared.generated.resources.progress_count
import bucketlog.shared.generated.resources.relative_days_ago
import bucketlog.shared.generated.resources.relative_today
import bucketlog.shared.generated.resources.relative_yesterday
import coil3.compose.AsyncImage
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.model.GoalType
import com.bucketlog.domain.usecase.GoalOverview
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
fun HomeScreen(viewModel: HomeViewModel, onAddGoalClick: () -> Unit, onGoalClick: (String) -> Unit) {
    val state by viewModel.uiState.collectAsState()
    HomeContent(
        state = state,
        onIntent = viewModel::onIntent,
        onAddGoalClick = onAddGoalClick,
        onGoalClick = onGoalClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    onAddGoalClick: () -> Unit,
    onGoalClick: (String) -> Unit,
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
                            onClick = { onGoalClick(overview.goal.id) },
                        )
                    }
                }
            }
        }
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
    onClick: () -> Unit,
) {
    val goal = overview.goal
    OutlinedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
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

            if (goal.status == GoalStatus.IN_PROGRESS) {
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
private fun ErrorDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(stringResource(Res.string.error_generic)) },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) } },
    )
}
