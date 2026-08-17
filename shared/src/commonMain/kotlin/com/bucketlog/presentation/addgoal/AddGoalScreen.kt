package com.bucketlog.presentation.addgoal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.add_goal_title
import bucketlog.shared.generated.resources.cancel
import bucketlog.shared.generated.resources.error_generic
import bucketlog.shared.generated.resources.goal_bucket_someday
import bucketlog.shared.generated.resources.goal_bucket_this_year
import bucketlog.shared.generated.resources.goal_bucket_year_label
import bucketlog.shared.generated.resources.goal_category_label
import bucketlog.shared.generated.resources.goal_note_label
import bucketlog.shared.generated.resources.goal_photo_label
import bucketlog.shared.generated.resources.goal_target_count_label
import bucketlog.shared.generated.resources.goal_title_label
import bucketlog.shared.generated.resources.goal_type_label
import bucketlog.shared.generated.resources.goal_type_one_time
import bucketlog.shared.generated.resources.goal_type_repeatable
import bucketlog.shared.generated.resources.save
import com.bucketlog.domain.model.Category
import com.bucketlog.domain.model.GoalType
import com.bucketlog.platform.rememberCameraCapture
import com.bucketlog.platform.rememberPhotoPicker
import com.bucketlog.presentation.common.PhotoAttachRow
import com.bucketlog.presentation.common.labelRes
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(viewModel: AddGoalViewModel, onSaved: () -> Unit, onCancel: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    val launchCamera = rememberCameraCapture { bytes ->
        if (bytes != null) viewModel.onIntent(AddGoalIntent.AddPhotos(listOf(bytes)))
    }
    val launchGallery = rememberPhotoPicker(maxItems = (5 - state.photoBytes.size).coerceAtLeast(1)) { photos ->
        viewModel.onIntent(AddGoalIntent.AddPhotos(photos))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.add_goal_title)) },
                navigationIcon = { TextButton(onClick = onCancel) { Text(stringResource(Res.string.cancel)) } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(state = rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.onIntent(AddGoalIntent.TitleChanged(it)) },
                label = { Text(stringResource(Res.string.goal_title_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = state.note,
                onValueChange = { viewModel.onIntent(AddGoalIntent.NoteChanged(it)) },
                label = { Text(stringResource(Res.string.goal_note_label)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Column {
                Text(stringResource(Res.string.goal_category_label), style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    items(Category.entries) { category ->
                        FilterChip(
                            selected = state.category == category,
                            onClick = { viewModel.onIntent(AddGoalIntent.CategoryChanged(category)) },
                            label = { Text(stringResource(category.labelRes())) },
                        )
                    }
                }
            }

            Column {
                Text(stringResource(Res.string.goal_type_label), style = MaterialTheme.typography.labelLarge)
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    FilterChip(
                        selected = state.type == GoalType.ONE_TIME,
                        onClick = { viewModel.onIntent(AddGoalIntent.TypeChanged(GoalType.ONE_TIME)) },
                        label = { Text(stringResource(Res.string.goal_type_one_time)) },
                    )
                    FilterChip(
                        selected = state.type == GoalType.REPEATABLE,
                        onClick = { viewModel.onIntent(AddGoalIntent.TypeChanged(GoalType.REPEATABLE)) },
                        label = { Text(stringResource(Res.string.goal_type_repeatable)) },
                    )
                }
            }

            if (state.type == GoalType.REPEATABLE) {
                OutlinedTextField(
                    value = state.targetCountText,
                    onValueChange = { viewModel.onIntent(AddGoalIntent.TargetCountChanged(it)) },
                    label = { Text(stringResource(Res.string.goal_target_count_label)) },
                    modifier = Modifier.wrapContentWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }

            Column {
                Text(stringResource(Res.string.goal_bucket_year_label), style = MaterialTheme.typography.labelLarge)
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    FilterChip(
                        selected = state.bucketYear == viewModel.thisYear,
                        onClick = { viewModel.onIntent(AddGoalIntent.BucketYearChanged(viewModel.thisYear)) },
                        label = { Text(stringResource(Res.string.goal_bucket_this_year)) },
                    )
                    FilterChip(
                        selected = state.bucketYear == null,
                        onClick = { viewModel.onIntent(AddGoalIntent.BucketYearChanged(null)) },
                        label = { Text(stringResource(Res.string.goal_bucket_someday)) },
                    )
                }
            }

            Column {
                Text(stringResource(Res.string.goal_photo_label), style = MaterialTheme.typography.labelLarge)
                PhotoAttachRow(
                    photoCount = state.photoBytes.size,
                    onCameraClick = launchCamera,
                    onGalleryClick = launchGallery,
                    onClearClick = { viewModel.onIntent(AddGoalIntent.ClearPhotos) },
                )
            }

            Button(
                onClick = { viewModel.onIntent(AddGoalIntent.Save) },
                enabled = state.canSave && !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.save))
            }
        }
    }

    if (state.hasError) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(AddGoalIntent.DismissError) },
            text = { Text(stringResource(Res.string.error_generic)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(AddGoalIntent.DismissError) }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }
}
