package com.bucketlog.domain.repository

import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeByStatus(status: GoalStatus): Flow<List<Goal>>
    /** H-02 요약 헤더 · H-05 연도 필터 — 상태 무관 전체 목표. 집계는 호출측에서 인메모리로 한다. */
    fun observeAll(): Flow<List<Goal>>
    fun observeById(id: String): Flow<Goal?>
    suspend fun getById(id: String): Goal?
    suspend fun add(goal: Goal)
    suspend fun update(goal: Goal)
    suspend fun delete(id: String)
}
