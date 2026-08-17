package com.bucketlog.presentation.archive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.archive_reason_label
import bucketlog.shared.generated.resources.archive_title
import bucketlog.shared.generated.resources.back
import bucketlog.shared.generated.resources.empty_archived
import bucketlog.shared.generated.resources.empty_completed
import bucketlog.shared.generated.resources.filter_archived
import bucketlog.shared.generated.resources.filter_completed
import coil3.compose.AsyncImage
import com.bucketlog.domain.usecase.GoalOverview
import com.bucketlog.presentation.common.labelRes
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(viewModel: ArchiveViewModel, onBack: () -> Unit, onGoalClick: (String) -> Unit) {
    val state by viewModel.uiState.collectAsState()
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
            }

            when (state.tab) {
                ArchiveTab.COMPLETED -> CompletedGrid(state.completed, onGoalClick)
                ArchiveTab.ARCHIVED -> ArchivedList(state.archived, onGoalClick)
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
