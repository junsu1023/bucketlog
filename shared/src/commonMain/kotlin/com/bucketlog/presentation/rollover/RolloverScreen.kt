package com.bucketlog.presentation.rollover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.back
import bucketlog.shared.generated.resources.rollover_confirm
import bucketlog.shared.generated.resources.rollover_empty
import bucketlog.shared.generated.resources.rollover_option_archive
import bucketlog.shared.generated.resources.rollover_option_keep
import bucketlog.shared.generated.resources.rollover_option_next_year
import bucketlog.shared.generated.resources.rollover_option_someday
import bucketlog.shared.generated.resources.rollover_subtitle
import bucketlog.shared.generated.resources.rollover_title
import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.usecase.RolloverDecision
import com.bucketlog.presentation.theme.BucketLogSpacing
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RolloverScreen(viewModel: RolloverViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.done) {
        if (state.done) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.rollover_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                text = stringResource(Res.string.rollover_subtitle, state.year),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = BucketLogSpacing.lg, vertical = BucketLogSpacing.sm),
            )

            if (state.goals.isEmpty()) {
                Text(
                    text = stringResource(Res.string.rollover_empty, state.year),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(BucketLogSpacing.lg),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = BucketLogSpacing.lg, vertical = BucketLogSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(BucketLogSpacing.md),
                ) {
                    items(state.goals, key = { it.id }) { goal ->
                        RolloverGoalRow(
                            goal = goal,
                            decision = state.decisions[goal.id] ?: RolloverDecision.KEEP,
                            onSelect = { onIntentDecision(viewModel, goal.id, it) },
                        )
                    }
                }
                Button(
                    onClick = { viewModel.onIntent(RolloverIntent.Confirm) },
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth().padding(BucketLogSpacing.lg),
                ) {
                    Text(stringResource(Res.string.rollover_confirm))
                }
            }
        }
    }
}

private fun onIntentDecision(viewModel: RolloverViewModel, goalId: String, decision: RolloverDecision) {
    viewModel.onIntent(RolloverIntent.SelectDecision(goalId, decision))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RolloverGoalRow(goal: Goal, decision: RolloverDecision, onSelect: (RolloverDecision) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(BucketLogSpacing.md)) {
            Text(text = goal.title, style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = BucketLogSpacing.sm),
            ) {
                FilterChip(
                    selected = decision == RolloverDecision.NEXT_YEAR,
                    onClick = { onSelect(RolloverDecision.NEXT_YEAR) },
                    label = { Text(stringResource(Res.string.rollover_option_next_year)) },
                )
                FilterChip(
                    selected = decision == RolloverDecision.SOMEDAY,
                    onClick = { onSelect(RolloverDecision.SOMEDAY) },
                    label = { Text(stringResource(Res.string.rollover_option_someday)) },
                )
                FilterChip(
                    selected = decision == RolloverDecision.ARCHIVE,
                    onClick = { onSelect(RolloverDecision.ARCHIVE) },
                    label = { Text(stringResource(Res.string.rollover_option_archive)) },
                )
                FilterChip(
                    selected = decision == RolloverDecision.KEEP,
                    onClick = { onSelect(RolloverDecision.KEEP) },
                    label = { Text(stringResource(Res.string.rollover_option_keep)) },
                )
            }
        }
    }
}
