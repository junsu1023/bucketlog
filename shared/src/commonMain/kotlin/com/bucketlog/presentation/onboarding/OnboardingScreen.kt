package com.bucketlog.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bucketlog.presentation.theme.BucketLogSpacing
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.cancel
import bucketlog.shared.generated.resources.error_generic
import bucketlog.shared.generated.resources.notification_permission_allow
import bucketlog.shared.generated.resources.notification_permission_body
import bucketlog.shared.generated.resources.notification_permission_deny
import bucketlog.shared.generated.resources.notification_permission_title
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
            // edge-to-edge에서 Scaffold가 bottomBar 내부 인셋을 자동 처리하지 않는다 —
            // 3버튼 내비게이션 모드에서 "시작하기" 버튼이 가려지는 걸 막는다.
            Surface(tonalElevation = 3.dp, modifier = Modifier.navigationBarsPadding()) {
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

    // O-03: 프리셋 탭으로 첫 목표가 만들어진 직후에만. 거절해도 계속 프리셋을 고를 수 있다.
    if (state.showNotificationPermissionPrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(OnboardingIntent.SkipNotificationPermission) },
            title = { Text(stringResource(Res.string.notification_permission_title, state.permissionPromptGoalTitle)) },
            text = { Text(stringResource(Res.string.notification_permission_body)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(OnboardingIntent.RequestNotificationPermission) }) {
                    Text(stringResource(Res.string.notification_permission_allow))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(OnboardingIntent.SkipNotificationPermission) }) {
                    Text(stringResource(Res.string.notification_permission_deny))
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
            PresetCard(title = title, added = added, onClick = { onPresetClick(title) })
        }
    }
}

/** 온보딩 프리셋 — 담긴 것은 배지가 아니라 카드 자체가 강조색으로 바뀌고 체크 아이콘이 붙는다. */
@Composable
private fun PresetCard(title: String, added: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(BucketLogSpacing.CardRadius),
        colors = if (added) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = BucketLogSpacing.md, vertical = BucketLogSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(BucketLogSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (added) {
                Icon(Icons.Filled.Check, contentDescription = stringResource(Res.string.onboarding_added), modifier = Modifier.size(16.dp))
            }
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
