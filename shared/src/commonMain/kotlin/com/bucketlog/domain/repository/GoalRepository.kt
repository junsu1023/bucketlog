package com.bucketlog.domain.repository

import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeByStatus(status: GoalStatus): Flow<List<Goal>>
    fun observeById(id: String): Flow<Goal?>
    suspend fun getById(id: String): Goal?
    suspend fun add(goal: Goal)
    suspend fun update(goal: Goal)
    suspend fun delete(id: String)
}
