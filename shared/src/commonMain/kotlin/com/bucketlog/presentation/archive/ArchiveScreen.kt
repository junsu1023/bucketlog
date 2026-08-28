package com.bucketlog.presentation.archive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.bucketlog.presentation.common.Hairline
import com.bucketlog.presentation.common.MonoMeta
import com.bucketlog.presentation.common.PillChip
import com.bucketlog.presentation.common.ScreenHeader
import com.bucketlog.presentation.common.photoFallbackBrush
import com.bucketlog.presentation.theme.BucketLogSpacing
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.archive_reason_label
import bucketlog.shared.generated.resources.archived_date_label
import bucketlog.shared.generated.resources.archived_view_button
import bucketlog.shared.generated.resources.completed_grid_caption
import bucketlog.shared.generated.resources.archive_title
import bucketlog.shared.generated.resources.back
import bucketlog.shared.generated.resources.empty_archived
import bucketlog.shared.generated.resources.empty_completed
import bucketlog.shared.generated.resources.empty_monthly
import bucketlog.shared.generated.resources.empty_timeline
import bucketlog.shared.generated.resources.filter_all
import bucketlog.shared.generated.resources.filter_archived
import bucketlog.shared.generated.resources.filter_completed
import bucketlog.shared.generated.resources.filter_monthly
import bucketlog.shared.generated.resources.filter_stats
import bucketlog.shared.generated.resources.relative_days_ago
import bucketlog.shared.generated.resources.relative_today
import bucketlog.shared.generated.resources.relative_yesterday
import bucketlog.shared.generated.resources.stats_by_category
import bucketlog.shared.generated.resources.stats_by_month
import bucketlog.shared.generated.resources.stats_empty
import bucketlog.shared.generated.resources.stats_total_completed
import coil3.compose.AsyncImage
import com.bucketlog.domain.repository.MonthlyEntry
import com.bucketlog.domain.usecase.GoalOverview
import com.bucketlog.presentation.common.MonthKey
import com.bucketlog.presentation.common.labelRes
import kotlin.time.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArchiveScreen(
    viewModel: ArchiveViewModel,
    onGoalClick: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    targetMonth: MonthKey? = null,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(targetMonth) {
        if (targetMonth != null) viewModel.onIntent(ArchiveIntent.ShowMonth(targetMonth))
    }

    ArchiveContent(state = state, onIntent = viewModel::onIntent, onBack = onBack, onGoalClick = onGoalClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchiveContent(
    state: ArchiveUiState,
    onIntent: (ArchiveIntent) -> Unit,
    onBack: (() -> Unit)?,
    onGoalClick: (String) -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScreenHeader(
                title = stringResource(Res.string.archive_title),
                onBack = onBack,
                backLabel = stringResource(Res.string.back),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PillChip(
                    label = stringResource(Res.string.filter_completed),
                    selected = state.tab == ArchiveTab.COMPLETED,
                    onClick = { onIntent(ArchiveIntent.SelectTab(ArchiveTab.COMPLETED)) },
                )
                PillChip(
                    label = stringResource(Res.string.filter_archived),
                    selected = state.tab == ArchiveTab.ARCHIVED,
                    onClick = { onIntent(ArchiveIntent.SelectTab(ArchiveTab.ARCHIVED)) },
                )
                PillChip(
                    label = stringResource(Res.string.filter_monthly),
                    selected = state.tab == ArchiveTab.MONTHLY,
                    onClick = { onIntent(ArchiveIntent.ShowMonth(MonthKey.current())) },
                )
                PillChip(
                    label = stringResource(Res.string.filter_all),
                    selected = state.tab == ArchiveTab.ALL,
                    onClick = { onIntent(ArchiveIntent.SelectTab(ArchiveTab.ALL)) },
                )
                PillChip(
                    label = stringResource(Res.string.filter_stats),
                    selected = state.tab == ArchiveTab.STATS,
                    onClick = { onIntent(ArchiveIntent.SelectTab(ArchiveTab.STATS)) },
                )
            }

            when (state.tab) {
                ArchiveTab.COMPLETED -> CompletedGrid(state.completed, onGoalClick)
                ArchiveTab.ARCHIVED -> ArchivedList(state.archived, onGoalClick)
                ArchiveTab.MONTHLY -> MonthlyEntriesList(
                    entries = state.monthlyEntries,
                    emptyText = stringResource(Res.string.empty_monthly),
                    onGoalClick = onGoalClick,
                )
                ArchiveTab.ALL -> MonthlyEntriesList(
                    entries = state.allEntries,
                    emptyText = stringResource(Res.string.empty_timeline),
                    onGoalClick = onGoalClick,
                )
                ArchiveTab.STATS -> StatsSection(state.stats)
            }
        }
    }
}

/** A-01: 사진 중심 그리드 — 완료 아카이브가 "체크리스트"가 아니라 "앨범"처럼 보이게 한다. */
@Composable
private fun CompletedGrid(overviews: List<GoalOverview>, onGoalClick: (String) -> Unit) {
    if (overviews.isEmpty()) {
        EmptyMessage(stringResource(Res.string.empty_completed))
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 112.dp),
        horizontalArrangement = Arrangement.spacedBy(BucketLogSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(BucketLogSpacing.lg),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(overviews, key = { it.goal.id }) { overview ->
            CompletedGridCell(overview = overview, onClick = { onGoalClick(overview.goal.id) })
        }
    }
}

/** docs/DESIGN.md §5.7 — 사진 위에 텍스트를 얹지 않고, 사진첩처럼 사진 아래에 캡션을 둔다. */
@Composable
private fun CompletedGridCell(overview: GoalOverview, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(BucketLogSpacing.PhotoGridRadius))
                .background(photoFallbackBrush(overview.goal.id.hashCode())),
        ) {
            overview.recentPhotoPaths.firstOrNull()?.let { coverPath ->
                AsyncImage(
                    model = coverPath,
                    contentDescription = overview.goal.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Text(
            text = overview.goal.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            modifier = Modifier.padding(top = BucketLogSpacing.sm),
        )
        overview.goal.completedAt?.toLocalDateTime(TimeZone.currentSystemDefault())?.date?.year?.let { year ->
            MonoMeta(
                text = stringResource(Res.string.completed_grid_caption, year),
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}

/** A-02: 이유가 남아있으면 함께 보여준다 — "실패"가 아니라 "정리"라는 톤. */
@Composable
private fun ArchivedList(overviews: List<GoalOverview>, onGoalClick: (String) -> Unit) {
    if (overviews.isEmpty()) {
        EmptyMessage(stringResource(Res.string.empty_archived))
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 112.dp),
    ) {
        itemsIndexed(overviews, key = { _, o -> o.goal.id }) { index, overview ->
            ArchivedRow(overview = overview, onClick = { onGoalClick(overview.goal.id) })
            if (index < overviews.lastIndex) Hairline()
        }
    }
}

@Composable
private fun ArchivedRow(overview: GoalOverview, onClick: () -> Unit) {
    val goal = overview.goal
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
    ) {
        MonoMeta(text = stringResource(goal.category.labelRes()))
        Text(
            text = goal.title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 5.dp),
        )
        goal.archiveReason?.takeIf { it.isNotBlank() }?.let { reason ->
            Text(
                text = "${stringResource(Res.string.archive_reason_label)} $reason",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            goal.archivedAt?.let {
                MonoMeta(
                    text = "${stringResource(Res.string.archived_date_label)} ${it.toLocalDateTime(TimeZone.currentSystemDefault()).date}",
                )
            }
            TextButton(onClick = onClick) { Text(stringResource(Res.string.archived_view_button)) }
        }
    }
}

/** N-01 월간 회고 딥링크 도착지("이번 달") · A-03 전체 타임라인이 함께 쓴다. */
@Composable
private fun MonthlyEntriesList(entries: List<MonthlyEntry>, emptyText: String, onGoalClick: (String) -> Unit) {
    if (entries.isEmpty()) {
        EmptyMessage(emptyText)
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 112.dp),
    ) {
        itemsIndexed(entries, key = { _, e -> e.entry.id }) { index, monthlyEntry ->
            MonthlyEntryRow(monthlyEntry, onClick = { onGoalClick(monthlyEntry.entry.goalId) })
            if (index < entries.lastIndex) Hairline()
        }
    }
}

@Composable
private fun MonthlyEntryRow(monthlyEntry: MonthlyEntry, onClick: () -> Unit) {
    val entry = monthlyEntry.entry
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = monthlyEntry.goalTitle, style = MaterialTheme.typography.titleMedium)
            MonoMeta(text = relativeDayLabel(entry.recordedAt))
        }
        entry.memo?.takeIf { it.isNotBlank() }?.let { memo ->
            Text(text = memo, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
        }
        if (monthlyEntry.photoPaths.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                monthlyEntry.photoPaths.forEach { path ->
                    AsyncImage(
                        model = path,
                        contentDescription = monthlyEntry.goalTitle,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(BucketLogSpacing.PhotoGridRadius)),
                        contentScale = ContentScale.Crop,
                    )
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

/** A-04 간단 통계 — 차트 없이 숫자/텍스트로. 절제된 톤. */
@Composable
private fun StatsSection(stats: ArchiveStats) {
    if (stats.totalCompleted == 0) {
        EmptyMessage(stringResource(Res.string.stats_empty))
        return
    }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Text(
            text = stringResource(Res.string.stats_total_completed, stats.totalCompleted),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 8.dp),
        )
        MonoMeta(
            text = stringResource(Res.string.stats_by_category),
            modifier = Modifier.padding(top = 28.dp, bottom = 4.dp),
        )
        stats.byCategory.forEach { (category, count) ->
            StatRow(label = stringResource(category.labelRes()), value = "$count")
        }
        MonoMeta(
            text = stringResource(Res.string.stats_by_month),
            modifier = Modifier.padding(top = 28.dp, bottom = 4.dp),
        )
        stats.byMonth.forEach { (month, count) ->
            StatRow(label = month.toString(), value = "$count")
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        MonoMeta(text = value)
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 8.dp), contentAlignment = Alignment.TopStart) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
