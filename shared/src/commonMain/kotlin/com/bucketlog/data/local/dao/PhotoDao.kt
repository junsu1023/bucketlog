package com.bucketlog.data.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.bucketlog.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Insert
    suspend fun insert(photo: PhotoEntity)

    // 백업 복원(M-02)용 — id 충돌 시 백업이 이긴다(docs/DATA-MODEL.md §7).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(photo: PhotoEntity)

    @Delete
    suspend fun delete(photo: PhotoEntity)

    @Query("SELECT * FROM photos WHERE entry_id = :entryId ORDER BY order_index")
    fun observeByEntry(entryId: String): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE entry_id = :entryId ORDER BY order_index")
    suspend fun getByEntry(entryId: String): List<PhotoEntity>

    // 목표 삭제 시 파일까지 정리하기 위해 entries를 거쳐 goalId로 조회한다.
    @Query(
        """
        SELECT photos.* FROM photos
        INNER JOIN entries ON photos.entry_id = entries.id
        WHERE entries.goal_id = :goalId
        """,
    )
    suspend fun getByGoal(goalId: String): List<PhotoEntity>
}
