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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.archive_reason_label
import bucketlog.shared.generated.resources.archive_title
import bucketlog.shared.generated.resources.back
import bucketlog.shared.generated.resources.empty_archived
import bucketlog.shared.generated.resources.empty_completed
import bucketlog.shared.generated.resources.empty_monthly
import bucketlog.shared.generated.resources.filter_archived
import bucketlog.shared.generated.resources.filter_completed
import bucketlog.shared.generated.resources.filter_monthly
import bucketlog.shared.generated.resources.relative_days_ago
import bucketlog.shared.generated.resources.relative_today
import bucketlog.shared.generated.resources.relative_yesterday
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    viewModel: ArchiveViewModel,
    onBack: () -> Unit,
    onGoalClick: (String) -> Unit,
    targetMonth: MonthKey? = null,
) {
    val state by viewModel.uiState.collectAsState()

    // N-01 딥링크(bucketlog://archive?month=...)로 진입했을 때만 특정 월로 이동한다.
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
    onBack: () -> Unit,
    onGoalClick: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.archive_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(Res.string.back)) } },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.tab == ArchiveTab.COMPLETED,
                    onClick = { onIntent(ArchiveIntent.SelectTab(ArchiveTab.COMPLETED)) },
                    label = { Text(stringResource(Res.string.filter_completed)) },
                )
                FilterChip(
                    selected = state.tab == ArchiveTab.ARCHIVED,
                    onClick = { onIntent(ArchiveIntent.SelectTab(ArchiveTab.ARCHIVED)) },
                    label = { Text(stringResource(Res.string.filter_archived)) },
                )
                FilterChip(
                    selected = state.tab == ArchiveTab.MONTHLY,
                    onClick = { onIntent(ArchiveIntent.ShowMonth(MonthKey.current())) },
                    label = { Text(stringResource(Res.string.filter_monthly)) },
                )
            }

            when (state.tab) {
                ArchiveTab.COMPLETED -> CompletedGrid(state.completed, onGoalClick)
                ArchiveTab.ARCHIVED -> ArchivedList(state.archived, onGoalClick)
                ArchiveTab.MONTHLY -> MonthlyEntriesList(state.monthlyEntries)
            }
        }
    }
}

/** A-01: 사진 중심 그리드 — 이 앱의 완료 아카이브가 "체크리스트"가 아니라 "앨범"처럼 보이게 한다. */
@Composable
private fun CompletedGrid(overviews: List<GoalOverview>, onGoalClick: (String) -> Unit) {
    if (overviews.isEmpty()) {
        EmptyMessage(stringResource(Res.string.empty_completed))
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(overviews, key = { it.goal.id }) { overview ->
            CompletedGridCell(overview = overview, onClick = { onGoalClick(overview.goal.id) })
        }
    }
}

@Composable
private fun CompletedGridCell(overview: GoalOverview, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.BottomStart,
    ) {
        val coverPath = overview.recentPhotoPaths.firstOrNull()
        if (coverPath != null) {
            AsyncImage(
                model = coverPath,
                contentDescription = overview.goal.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
        Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f))) {
            Text(
                text = overview.goal.title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.surface,
                maxLines = 1,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

/** A-02: 이유가 남아있으면 함께 보여준다 — "실패"가 아니라 "정리"라는 톤(docs/DESIGN.md). */
@Composable
private fun ArchivedList(overviews: List<GoalOverview>, onGoalClick: (String) -> Unit) {
    if (overviews.isEmpty()) {
        EmptyMessage(stringResource(Res.string.empty_archived))
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(overviews, key = { it.goal.id }) { overview ->
            ArchivedRow(overview = overview, onClick = { onGoalClick(overview.goal.id) })
        }
    }
}

@Composable
private fun ArchivedRow(overview: GoalOverview, onClick: () -> Unit) {
    val goal = overview.goal
    OutlinedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = goal.title, style = MaterialTheme.typography.titleMedium)
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(stringResource(goal.category.labelRes())) },
                modifier = Modifier.padding(top = 4.dp),
            )
            goal.archiveReason?.takeIf { it.isNotBlank() }?.let { reason ->
                Text(
                    text = "${stringResource(Res.string.archive_reason_label)} $reason",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

/** N-01 월간 회고 딥링크 도착지 · A-03 전체 타임라인으로 확장하기 쉽도록 월 필터만 걸어둔 형태. */
@Composable
private fun MonthlyEntriesList(entries: List<MonthlyEntry>) {
    if (entries.isEmpty()) {
        EmptyMessage(stringResource(Res.string.empty_monthly))
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(entries, key = { it.entry.id }) { monthlyEntry ->
            MonthlyEntryRow(monthlyEntry)
        }
    }
}

@Composable
private fun MonthlyEntryRow(monthlyEntry: MonthlyEntry) {
    val entry = monthlyEntry.entry
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = monthlyEntry.goalTitle, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = relativeDayLabel(entry.recordedAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            entry.memo?.takeIf { it.isNotBlank() }?.let { memo ->
                Text(text = memo, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
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
                            modifier = Modifier.size(72.dp).clip(MaterialTheme.shapes.small),
                            contentScale = ContentScale.Crop,
                        )
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
private fun EmptyMessage(text: String) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
