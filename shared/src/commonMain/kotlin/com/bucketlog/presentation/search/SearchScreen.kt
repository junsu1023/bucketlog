package com.bucketlog.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.search_empty_query
import bucketlog.shared.generated.resources.search_empty_result
import bucketlog.shared.generated.resources.search_placeholder
import coil3.compose.AsyncImage
import com.bucketlog.domain.usecase.GoalOverview
import com.bucketlog.presentation.theme.BucketLogSpacing
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchScreen(viewModel: SearchViewModel, onGoalClick: (String) -> Unit) {
    val state by viewModel.uiState.collectAsState()

    // 검색어 입력칸은 뷰모델을 거쳐 돌아오는 state.query를 그대로 value로 쓰지 않는다 — 그 값은
    // Flow 파이프라인을 한 바퀴 돌아 비동기로 갱신되므로, Compose가 "이 값이 방금 내가 친 그
    // 글자가 맞나"를 못 알아채고 커서를 매번 맨 끝으로 리셋해버린다(타이핑 중 커서가 튀는 버그).
    // 입력칸은 로컬 상태로만 직접 다루고, 검색 자체(결과 목록)만 뷰모델에 위임한다.
    var textFieldValue by remember { mutableStateOf(TextFieldValue()) }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                viewModel.onIntent(SearchIntent.QueryChanged(it.text))
            },
            placeholder = { Text(stringResource(Res.string.search_placeholder)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(BucketLogSpacing.lg),
        )

        when {
            textFieldValue.text.isBlank() -> EmptyHint(stringResource(Res.string.search_empty_query))
            state.results.isEmpty() -> EmptyHint(stringResource(Res.string.search_empty_result))
            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = BucketLogSpacing.lg, vertical = BucketLogSpacing.sm),
            ) {
                items(state.results, key = { it.goal.id }) { overview ->
                    SearchResultRow(overview = overview, onClick = { onGoalClick(overview.goal.id) })
                }
            }
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(modifier = Modifier.fillMaxSize().padding(BucketLogSpacing.xxl), contentAlignment = Alignment.TopStart) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SearchResultRow(overview: GoalOverview, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = BucketLogSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val thumbnail = overview.recentPhotoPaths.firstOrNull()
        if (thumbnail != null) {
            AsyncImage(
                model = thumbnail,
                contentDescription = null,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(BucketLogSpacing.PhotoGridRadius)),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(BucketLogSpacing.PhotoGridRadius))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
        Text(
            text = overview.goal.title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = BucketLogSpacing.md),
        )
    }
}
