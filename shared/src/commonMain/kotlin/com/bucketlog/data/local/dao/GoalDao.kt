package com.bucketlog.data.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.bucketlog.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Insert
    suspend fun insert(goal: GoalEntity)

    // 백업 복원(M-02)용 — id 충돌 시 백업이 이긴다(docs/DATA-MODEL.md §7).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: GoalEntity)

    @Update
    suspend fun update(goal: GoalEntity)

    @Delete
    suspend fun delete(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: String): GoalEntity?

    @Query("SELECT * FROM goals WHERE id = :id")
    fun observeById(id: String): Flow<GoalEntity?>

    @Query("SELECT * FROM goals WHERE status = :status ORDER BY created_at DESC")
    fun observeByStatus(status: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals ORDER BY created_at DESC")
    fun observeAll(): Flow<List<GoalEntity>>
}
