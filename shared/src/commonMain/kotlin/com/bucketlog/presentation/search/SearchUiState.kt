package com.bucketlog.presentation.search

import com.bucketlog.domain.usecase.GoalOverview

data class SearchUiState(
    val query: String = "",
    val results: List<GoalOverview> = emptyList(),
)
