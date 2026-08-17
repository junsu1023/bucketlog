package com.bucketlog.data.local.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

/** docs/DATA-MODEL.md §3 `photos` 테이블. 파일은 앱 전용 디렉토리에 두고 여기엔 경로만 저장한다. */
@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = EntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entry_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["entry_id", "order_index"]),
    ],
)
data class PhotoEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "entry_id") val entryId: String,
    val path: String,
    @ColumnInfo(name = "thumbnail_path") val thumbnailPath: String,
    @ColumnInfo(name = "order_index") val orderIndex: Int,
    val width: Int,
    val height: Int,
)
