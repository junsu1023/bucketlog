package com.bucketlog.data.backup

import kotlinx.serialization.Serializable

/**
 * docs/DATA-MODEL.md §7 백업 포맷의 data.json 최상위 구조.
 * Instant는 epoch millis(Long), LocalDate는 ISO 문자열로 저장한다 — kotlinx-datetime 시리얼라이저에
 * 얽매이지 않고, 백업 파일을 오래 보관·이식해야 하는 포맷이라 가장 단순한 원시 타입으로 고정한다.
 */
@Serializable
data class BackupData(
    val schemaVersion: Int,
    val exportedAt: Long,
    val goals: List<GoalDto>,
    val entries: List<EntryDto>,
)

@Serializable
data class GoalDto(
    val id: String,
    val title: String,
    val note: String?,
    val category: String,
    val type: String,
    val targetCount: Int?,
    val status: String,
    val bucketYear: Int?,
    val dueDate: String?,
    val coverEntryId: String?,
    val reminderInterval: String?,
    val reminderEnabled: Boolean,
    val createdAt: Long,
    val completedAt: Long?,
    val retrospect: String?,
    val archivedAt: Long?,
    val archiveReason: String?,
    val nudgeSnoozedUntil: Long?,
)

@Serializable
data class EntryDto(
    val id: String,
    val goalId: String,
    val kind: String,
    val memo: String?,
    val countDelta: Int,
    val recordedAt: Long,
    val createdAt: Long,
    val photos: List<PhotoDto>,
)

@Serializable
data class PhotoDto(
    val id: String,
    val path: String,
    val thumbnailPath: String,
    val order: Int,
    val width: Int,
    val height: Int,
)
