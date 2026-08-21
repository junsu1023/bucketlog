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

    /** 백업 복원(M-02)용 — id가 이미 있으면 덮어쓴다(백업이 이긴다). */
    suspend fun upsert(goal: Goal)

    /** M-03 데이터 초기화용 — 목표/기록/사진 전부(로컬 파일 포함) 삭제. 되돌릴 수 없다. */
    suspend fun deleteAll()
}
