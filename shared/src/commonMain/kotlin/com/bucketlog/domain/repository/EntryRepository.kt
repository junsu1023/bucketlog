package com.bucketlog.domain.repository

import com.bucketlog.domain.model.Entry
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface EntryRepository {
    fun observeByGoal(goalId: String): Flow<List<Entry>>

    /** goalId -> REPEATABLE 진행 카운트 합계 (docs/DATA-MODEL.md §4 progressCount) */
    fun observeProgressTotals(): Flow<Map<String, Int>>

    /** goalId -> 가장 최근 recordedAt (홈 카드 H-03 "마지막 기록 시점") */
    fun observeLastRecordedAt(): Flow<Map<String, Instant>>

    /** goalId -> 가장 최근 사진 있는 기록의 사진 전체 썸네일 경로(순서대로). docs/DATA-MODEL.md §4 coverPhoto 확장. */
    fun observeRecentPhotoPaths(): Flow<Map<String, List<String>>>

    suspend fun getById(id: String): Entry?
    suspend fun add(entry: Entry)
    suspend fun update(entry: Entry)
    suspend fun delete(id: String)

    /** 완료 되돌리기(G-08)용: COMPLETION Entry를 찾아 kind만 PROGRESS로 강등한다. 메모/사진은 보존. */
    suspend fun demoteCompletionEntry(goalId: String): Boolean

    /** 백업 내보내기(M-02)용 — 상태 무관 전체 기록. */
    suspend fun getAll(): List<Entry>

    /** 백업 복원(M-02)용 — id가 이미 있으면 덮어쓴다(백업이 이긴다). photos도 같이 upsert. */
    suspend fun upsert(entry: Entry)
}
