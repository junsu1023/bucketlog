package com.bucketlog.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.bucketlog.presentation.theme.BucketLogSpacing
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.archive_nav_button
import bucketlog.shared.generated.resources.cancel
import bucketlog.shared.generated.resources.check_in_placeholder
import bucketlog.shared.generated.resources.check_in_save
import bucketlog.shared.generated.resources.empty_in_progress
import bucketlog.shared.generated.resources.empty_state_preset_hint
import bucketlog.shared.generated.resources.error_generic
import bucketlog.shared.generated.resources.goal_bucket_someday
import bucketlog.shared.generated.resources.home_summary_someday
import bucketlog.shared.generated.resources.home_summary_year
import bucketlog.shared.generated.resources.home_title
import bucketlog.shared.generated.resources.last_recorded
import bucketlog.shared.generated.resources.progress_count
import bucketlog.shared.generated.resources.relative_days_ago
import bucketlog.shared.generated.resources.relative_today
import bucketlog.shared.generated.resources.relative_yesterday
import bucketlog.shared.generated.resources.settings_nav_button
import bucketlog.shared.generated.resources.throwback_month_ago
import bucketlog.shared.generated.resources.throwback_year_ago
import bucketlog.shared.generated.resources.year_chip
import coil3.compose.AsyncImage
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.model.GoalType
import com.bucketlog.domain.usecase.GoalOverview
import com.bucketlog.presentation.common.PresetGoal
import com.bucketlog.presentation.common.labelRes
import com.bucketlog.presentation.common.presetGoals
import com.bucketlog.presentation.theme.MonoLabel
import kotlin.time.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddGoalClick: () -> Unit,
    onGoalClick: (String) -> Unit,
    onArchiveClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    HomeContent(
        state = state,
        onIntent = viewModel::onIntent,
        onAddGoalClick = onAddGoalClick,
        onGoalClick = onGoalClick,
        onArchiveClick = onArchiveClick,
        onSettingsClick = onSettingsClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    onAddGoalClick: () -> Unit,
    onGoalClick: (String) -> Unit,
    onArchiveClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.home_title)) },
                actions = {
                    TextButton(onClick = onSettingsClick) { Text(stringResource(Res.string.settings_nav_button)) }
                    TextButton(onClick = onArchiveClick) { Text(stringResource(Res.string.archive_nav_button)) }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddGoalClick) {
                Text(text = "+", style = MaterialTheme.typography.headlineSmall)
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            state.throwback?.let { banner ->
                ThrowbackBannerCard(banner = banner, onClick = { onGoalClick(banner.goalId) })
            }
            YearFilterRow(
                selected = state.yearFilter,
                availableYears = state.availableYears,
                onSelect = { onIntent(HomeIntent.SelectYearFilter(it)) },
            )
            SummaryHeader(state.yearFilter, state.summaryTotal, state.summaryCompleted)

            if (state.overviews.isEmpty() && !state.isLoading) {
                EmptyState(existingTitles = state.existingTitles, onIntent = onIntent, modifier = Modifier.fillMaxSize())
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
private fun YearFilterRow(
    selected: BucketYearFilter,
    availableYears: List<Int>,
    onSelect: (BucketYearFilter) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        availableYears.forEach { year ->
            val filter = BucketYearFilter.Year(year)
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = { Text(stringResource(Res.string.year_chip, year)) },
                shape = RoundedCornerShape(BucketLogSpacing.ChipRadius),
                border = null,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
        FilterChip(
            selected = selected == BucketYearFilter.Someday,
            onClick = { onSelect(BucketYearFilter.Someday) },
            label = { Text(stringResource(Res.string.goal_bucket_someday)) },
            shape = RoundedCornerShape(BucketLogSpacing.ChipRadius),
            border = null,
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            ),
        )
    }
}

/** H-07 작년 오늘 — 유저가 아무것도 안 해도 앱이 먼저 과거 기록을 꺼내 보여준다. 달성률 언급 없음. */
@Composable
private fun ThrowbackBannerCard(banner: ThrowbackBanner, onClick: () -> Unit) {
    val text = when (banner.kind) {
        ThrowbackKind.YEAR_AGO -> stringResource(Res.string.throwback_year_ago, banner.goalTitle)
        ThrowbackKind.MONTH_AGO -> stringResource(Res.string.throwback_month_ago, banner.goalTitle)
    }
    Card(
        shape = RoundedCornerShape(BucketLogSpacing.CardRadius),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onClick),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
        )
    }
}

/** H-02 요약 헤더. "언젠가"는 완료 비율 대신 개수만 보여준다(연도처럼 마감 개념이 없어서). */
@Composable
private fun SummaryHeader(filter: BucketYearFilter, total: Int, completed: Int) {
    val text = when (filter) {
        is BucketYearFilter.Year -> stringResource(Res.string.home_summary_year, filter.year, total, completed)
        BucketYearFilter.Someday -> stringResource(Res.string.home_summary_someday, total)
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.merge(MonoLabel()),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmptyState(existingTitles: Set<String>, onIntent: (HomeIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(32.dp)) {
        Text(
            text = stringResource(Res.string.empty_in_progress),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(Res.string.empty_state_preset_hint),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presetGoals.distinctBy { it.category }.forEach { preset ->
                // 이미 추가한 프리셋은 다시 제안하지 않는다 — 중복 목표 생성 방지.
                PresetChip(preset = preset, existingTitles = existingTitles, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun PresetChip(preset: PresetGoal, existingTitles: Set<String>, onIntent: (HomeIntent) -> Unit) {
    val title = stringResource(preset.titleRes)
    if (title in existingTitles) return
    AssistChip(
        onClick = { onIntent(HomeIntent.AddPresetGoal(title, preset.category)) },
        label = { Text(title) },
        shape = RoundedCornerShape(BucketLogSpacing.ChipRadius),
        border = null,
        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    )
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
    Card(
        shape = RoundedCornerShape(BucketLogSpacing.CardRadius),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        if (overview.recentPhotoPaths.isNotEmpty()) {
            if (overview.recentPhotoPaths.size == 1) {
                AsyncImage(
                    model = overview.recentPhotoPaths.first(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop,
                )
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(overview.recentPhotoPaths) { path ->
                        AsyncImage(
                            model = path,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
        Column(modifier = Modifier.padding(BucketLogSpacing.lg)) {
            Text(text = goal.title, style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.padding(top = BucketLogSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(BucketLogSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(stringResource(goal.category.labelRes())) },
                    shape = RoundedCornerShape(BucketLogSpacing.ChipRadius),
                    border = null,
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
                overview.lastRecordedAt?.let {
                    Text(
                        text = stringResource(Res.string.last_recorded, relativeDayLabel(it)),
                        style = MaterialTheme.typography.bodySmall.merge(MonoLabel()),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (goal.type == GoalType.REPEATABLE && goal.targetCount != null) {
                    Text(
                        text = stringResource(Res.string.progress_count, overview.progressCount, goal.targetCount),
                        style = MaterialTheme.typography.bodySmall.merge(MonoLabel()),
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
