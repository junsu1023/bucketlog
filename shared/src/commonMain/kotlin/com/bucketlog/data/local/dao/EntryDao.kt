package com.bucketlog.data.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import com.bucketlog.data.local.entity.EntryEntity
import com.bucketlog.data.local.entity.EntryWithPhotos
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Insert
    suspend fun insert(entry: EntryEntity)

    @Update
    suspend fun update(entry: EntryEntity)

    @Delete
    suspend fun delete(entry: EntryEntity)

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getById(id: String): EntryEntity?

    @Transaction
    @Query("SELECT * FROM entries WHERE goal_id = :goalId ORDER BY recorded_at DESC")
    fun observeByGoal(goalId: String): Flow<List<EntryWithPhotos>>

    @Query("SELECT * FROM entries WHERE goal_id = :goalId AND kind = 'COMPLETION' LIMIT 1")
    suspend fun findCompletionEntry(goalId: String): EntryEntity?

    // 파생 규칙(docs/DATA-MODEL.md §4)의 progressCount를 DB에서 집계. goals 테이블에 값을 저장하지 않는다.
    @Query("SELECT goal_id AS goalId, COALESCE(SUM(count_delta), 0) AS total FROM entries GROUP BY goal_id")
    fun observeProgressTotals(): Flow<List<GoalProgressTotal>>

    // H-03 "마지막 기록 시점" — recordedAt 기준(넛지 판정용 createdAt과는 다름, docs/DATA-MODEL.md 설계 노트)
    @Query("SELECT goal_id AS goalId, MAX(recorded_at) AS lastRecordedAt FROM entries GROUP BY goal_id")
    fun observeLastRecordedAt(): Flow<List<GoalLastRecorded>>
}

data class GoalProgressTotal(val goalId: String, val total: Int)

data class GoalLastRecorded(val goalId: String, val lastRecordedAt: Long)
