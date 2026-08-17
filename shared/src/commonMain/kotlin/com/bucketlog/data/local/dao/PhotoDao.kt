package com.bucketlog.data.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import com.bucketlog.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

/** 2주차 사진 파이프라인에서 본격적으로 쓰인다. 지금은 테이블/DAO만 준비해둔다. */
@Dao
interface PhotoDao {
    @Insert
    suspend fun insert(photo: PhotoEntity)

    @Delete
    suspend fun delete(photo: PhotoEntity)

    @Query("SELECT * FROM photos WHERE entry_id = :entryId ORDER BY order_index")
    fun observeByEntry(entryId: String): Flow<List<PhotoEntity>>
}
