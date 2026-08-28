package com.bucketlog.presentation.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bucketlog.presentation.common.Hairline
import com.bucketlog.presentation.common.MonoMeta
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

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel, onCustomInput: () -> Unit, onDone: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                Hairline()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
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
                .padding(horizontal = 20.dp),
        ) {
            Text(
                text = stringResource(Res.string.onboarding_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 30.sp, lineHeight = 38.sp),
                modifier = Modifier.padding(top = 40.dp),
            )
            Text(
                text = stringResource(Res.string.onboarding_subtitle),
                style = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp),
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
            androidx.compose.foundation.layout.Spacer(Modifier.size(24.dp))
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
    MonoMeta(
        text = stringResource(category.labelRes()),
        modifier = Modifier.padding(top = 28.dp, bottom = 10.dp),
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        presets.forEach { preset ->
            val title = stringResource(preset.titleRes)
            val added = title in addedTitles
            PresetChip(title = title, added = added, onClick = { onPresetClick(title) })
        }
    }
}

/** 온보딩 프리셋 — 담긴 것은 강조색으로 채워지고 체크가 붙는다. 나머지 화면의 PillChip과 같은 언어. */
@Composable
private fun PresetChip(title: String, added: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        if (added) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(220),
        label = "presetBg",
    )
    val fg by animateColorAsState(
        if (added) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(220),
        label = "presetFg",
    )
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(bg)
            .then(
                if (added) Modifier
                else Modifier.border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.16f), shape),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (added) {
            Icon(
                Icons.Filled.Check,
                contentDescription = stringResource(Res.string.onboarding_added),
                tint = fg,
                modifier = Modifier.size(15.dp),
            )
        }
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = fg)
    }
}
