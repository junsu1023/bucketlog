package com.bucketlog.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bucketlog.domain.usecase.SearchGoalsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** 목표 제목으로 찾는 간단한 검색 — 별도 화면(MVP-SCOPE.md에는 없던 기능, 디자인 리뉴얼 때 추가). */
class SearchViewModel(private val searchGoals: SearchGoalsUseCase) : ViewModel() {

    private val query = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SearchUiState> = query
        .flatMapLatest { q -> searchGoals(q).map { results -> SearchUiState(q, results) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> query.value = intent.query
        }
    }
}
