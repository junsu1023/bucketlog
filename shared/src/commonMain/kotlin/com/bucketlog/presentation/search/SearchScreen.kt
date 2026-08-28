package com.bucketlog.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.search_empty_query
import bucketlog.shared.generated.resources.search_empty_result
import bucketlog.shared.generated.resources.search_nav_button
import bucketlog.shared.generated.resources.search_placeholder
import coil3.compose.AsyncImage
import com.bucketlog.domain.usecase.GoalOverview
import com.bucketlog.presentation.common.Hairline
import com.bucketlog.presentation.common.MonoMeta
import com.bucketlog.presentation.common.labelRes
import com.bucketlog.presentation.common.photoFallbackBrush
import com.bucketlog.presentation.theme.BucketLogSpacing
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchScreen(viewModel: SearchViewModel, onGoalClick: (String) -> Unit) {
    val state by viewModel.uiState.collectAsState()

    // 입력칸은 로컬 상태로만 다룬다 — state.query를 그대로 value로 쓰면 타이핑 중 커서가 튄다(비동기 왕복).
    var textFieldValue by remember { mutableStateOf(TextFieldValue()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Text(
            text = stringResource(Res.string.search_nav_button),
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 30.sp, lineHeight = 34.sp),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 34.dp, bottom = 14.dp),
        )
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                viewModel.onIntent(SearchIntent.QueryChanged(it.text))
            },
            placeholder = { Text(stringResource(Res.string.search_placeholder)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
        )
        Hairline(Modifier.padding(horizontal = 20.dp, vertical = 12.dp))

        when {
            textFieldValue.text.isBlank() -> EmptyHint(stringResource(Res.string.search_empty_query))
            state.results.isEmpty() -> EmptyHint(stringResource(Res.string.search_empty_result))
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 112.dp),
            ) {
                itemsIndexed(state.results, key = { _, o -> o.goal.id }) { index, overview ->
                    SearchResultRow(overview = overview, onClick = { onGoalClick(overview.goal.id) })
                    if (index < state.results.lastIndex) Hairline()
                }
            }
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.TopStart,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SearchResultRow(overview: GoalOverview, onClick: () -> Unit) {
    val thumbnail = overview.recentPhotoPaths.firstOrNull()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BucketLogSpacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(BucketLogSpacing.PhotoGridRadius))
                .background(photoFallbackBrush(overview.goal.id.hashCode())),
        ) {
            if (thumbnail != null) {
                AsyncImage(
                    model = thumbnail,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Column {
            MonoMeta(text = stringResource(overview.goal.category.labelRes()))
            Text(
                text = overview.goal.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}
