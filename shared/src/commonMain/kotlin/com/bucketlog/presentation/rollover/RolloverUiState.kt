package com.bucketlog.presentation.rollover

import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.usecase.RolloverDecision

data class RolloverUiState(
    val year: Int,
    val goals: List<Goal> = emptyList(),
    /** 아직 안 고른 목표는 이 맵에 없다 — 결정 안 함 = "그대로 두기"와 같은 결과. */
    val decisions: Map<String, RolloverDecision> = emptyMap(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val done: Boolean = false,
)
