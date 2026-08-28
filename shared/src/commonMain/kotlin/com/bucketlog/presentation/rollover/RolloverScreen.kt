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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.bucketlog.presentation.common.Hairline
import com.bucketlog.presentation.common.PillChip
import com.bucketlog.presentation.common.ScreenHeader
import org.jetbrains.compose.resources.stringResource

@Composable
fun RolloverScreen(viewModel: RolloverViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.done) {
        if (state.done) onBack()
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScreenHeader(
                title = stringResource(Res.string.rollover_title),
                onBack = onBack,
                backLabel = stringResource(Res.string.back),
            )
            Text(
                text = stringResource(Res.string.rollover_subtitle, state.year),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )

            if (state.goals.isEmpty()) {
                Text(
                    text = stringResource(Res.string.rollover_empty, state.year),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                ) {
                    itemsIndexed(state.goals, key = { _, g -> g.id }) { index, goal ->
                        RolloverGoalRow(
                            goal = goal,
                            decision = state.decisions[goal.id] ?: RolloverDecision.KEEP,
                            onSelect = { onIntentDecision(viewModel, goal.id, it) },
                        )
                        if (index < state.goals.lastIndex) Hairline()
                    }
                }
                Button(
                    onClick = { viewModel.onIntent(RolloverIntent.Confirm) },
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
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
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp)) {
        Text(text = goal.title, style = MaterialTheme.typography.titleMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 12.dp),
        ) {
            PillChip(
                label = stringResource(Res.string.rollover_option_next_year),
                selected = decision == RolloverDecision.NEXT_YEAR,
                onClick = { onSelect(RolloverDecision.NEXT_YEAR) },
            )
            PillChip(
                label = stringResource(Res.string.rollover_option_someday),
                selected = decision == RolloverDecision.SOMEDAY,
                onClick = { onSelect(RolloverDecision.SOMEDAY) },
            )
            PillChip(
                label = stringResource(Res.string.rollover_option_archive),
                selected = decision == RolloverDecision.ARCHIVE,
                onClick = { onSelect(RolloverDecision.ARCHIVE) },
            )
            PillChip(
                label = stringResource(Res.string.rollover_option_keep),
                selected = decision == RolloverDecision.KEEP,
                onClick = { onSelect(RolloverDecision.KEEP) },
            )
        }
    }
}
