package com.bucketlog.data.local.entity

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import androidx.room3.Relation

/** docs/DATA-MODEL.md §3 `entries` 테이블. 퀵 체크인/진행 기록/완료 인증이 모두 이 테이블 하나로 저장된다. */
@Entity(
    tableName = "entries",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["goal_id", "recorded_at"]),
        Index(value = ["recorded_at"]),
        Index(value = ["goal_id", "created_at"]),
    ],
)
data class EntryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "goal_id") val goalId: String,
    val kind: String,
    val memo: String?,
    @ColumnInfo(name = "count_delta", defaultValue = "0") val countDelta: Int,
    @ColumnInfo(name = "recorded_at") val recordedAt: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

data class EntryWithPhotos(
    @Embedded val entry: EntryEntity,
    @Relation(parentColumns = ["id"], entityColumns = ["entry_id"])
    val photos: List<PhotoEntity>,
)
