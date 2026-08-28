package com.bucketlog.presentation.rollover

import com.bucketlog.domain.usecase.RolloverDecision

sealed interface RolloverIntent {
    data class SelectDecision(val goalId: String, val decision: RolloverDecision) : RolloverIntent
    data object Confirm : RolloverIntent
}
