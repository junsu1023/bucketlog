package com.bucketlog.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.cancel
import bucketlog.shared.generated.resources.error_generic
import bucketlog.shared.generated.resources.onboarding_added
import bucketlog.shared.generated.resources.onboarding_custom_input
import bucketlog.shared.generated.resources.onboarding_start
import bucketlog.shared.generated.resources.onboarding_subtitle
import bucketlog.shared.generated.resources.onboarding_title
import com.bucketlog.domain.model.Category
import com.bucketlog.presentation.common.PresetGoal
import com.bucketlog.presentation.common.labelRes
import com.bucketlog.presentation.common.presetGoals
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel, onCustomInput: () -> Unit, onDone: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TextButton(onClick = onCustomInput) { Text(stringResource(Res.string.onboarding_custom_input)) }
                    Button(onClick = onDone, modifier = Modifier.weight(1f)) {
                        Text(stringResource(Res.string.onboarding_start))
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(stringResource(Res.string.onboarding_title), style = MaterialTheme.typography.headlineSmall)
            Text(
                text = stringResource(Res.string.onboarding_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )

            Category.entries.forEach { category ->
                val presetsInCategory = presetGoals.filter { it.category == category }
                if (presetsInCategory.isNotEmpty()) {
                    CategoryPresetSection(
                        category = category,
                        presets = presetsInCategory,
                        addedTitles = state.addedTitles,
                        onPresetClick = { title -> viewModel.onIntent(OnboardingIntent.AddPreset(title, category)) },
                    )
                }
            }
        }
    }

    if (state.hasError) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(OnboardingIntent.DismissError) },
            text = { Text(stringResource(Res.string.error_generic)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(OnboardingIntent.DismissError) }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryPresetSection(
    category: Category,
    presets: List<PresetGoal>,
    addedTitles: Set<String>,
    onPresetClick: (String) -> Unit,
) {
    Text(
        text = stringResource(category.labelRes()),
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        presets.forEach { preset ->
            val title = stringResource(preset.titleRes)
            val added = title in addedTitles
            FilterChip(
                selected = added,
                onClick = { onPresetClick(title) },
                label = { Text(if (added) "$title · ${stringResource(Res.string.onboarding_added)}" else title) },
            )
        }
    }
}
