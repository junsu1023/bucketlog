package com.bucketlog.data.local.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/** docs/DATA-MODEL.md §3 `goals` 테이블. enum/Instant/LocalDate는 매퍼(data/mapper)에서 변환한다. */
@Entity(
    tableName = "goals",
    indices = [
        Index(value = ["status", "bucket_year"]),
        Index(value = ["completed_at"]),
    ],
)
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val note: String?,
    val category: String,
    val type: String,
    @ColumnInfo(name = "target_count") val targetCount: Int?,
    val status: String,
    @ColumnInfo(name = "bucket_year") val bucketYear: Int?,
    @ColumnInfo(name = "due_date") val dueDate: Long?,
    @ColumnInfo(name = "cover_entry_id") val coverEntryId: String?,
    @ColumnInfo(name = "reminder_interval") val reminderInterval: String?,
    @ColumnInfo(name = "reminder_enabled") val reminderEnabled: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "completed_at") val completedAt: Long?,
    val retrospect: String?,
    @ColumnInfo(name = "archived_at") val archivedAt: Long?,
    @ColumnInfo(name = "archive_reason") val archiveReason: String?,
    @ColumnInfo(name = "nudge_snoozed_until") val nudgeSnoozedUntil: Long?,
)
