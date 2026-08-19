package com.bucketlog.data.backup

import com.bucketlog.domain.model.Category
import com.bucketlog.domain.model.Entry
import com.bucketlog.domain.model.EntryKind
import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.model.GoalType
import com.bucketlog.domain.model.Photo
import com.bucketlog.domain.model.ReminderInterval
import com.bucketlog.domain.model.ReminderRule
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

fun Goal.toDto(): GoalDto = GoalDto(
    id = id,
    title = title,
    note = note,
    category = category.name,
    type = type.name,
    targetCount = targetCount,
    status = status.name,
    bucketYear = bucketYear,
    dueDate = dueDate?.toString(),
    coverEntryId = coverEntryId,
    reminderInterval = reminderRule?.interval?.name,
    reminderEnabled = reminderRule?.enabled ?: false,
    createdAt = createdAt.toEpochMilliseconds(),
    completedAt = completedAt?.toEpochMilliseconds(),
    retrospect = retrospect,
    archivedAt = archivedAt?.toEpochMilliseconds(),
    archiveReason = archiveReason,
    nudgeSnoozedUntil = nudgeSnoozedUntil?.toEpochMilliseconds(),
    reminderLastSentAt = reminderLastSentAt?.toEpochMilliseconds(),
)

fun GoalDto.toDomain(): Goal = Goal(
    id = id,
    title = title,
    note = note,
    category = Category.valueOf(category),
    type = GoalType.valueOf(type),
    targetCount = targetCount,
    status = GoalStatus.valueOf(status),
    bucketYear = bucketYear,
    dueDate = dueDate?.let { LocalDate.parse(it) },
    coverEntryId = coverEntryId,
    reminderRule = reminderInterval?.let { ReminderRule(ReminderInterval.valueOf(it), reminderEnabled) },
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    completedAt = completedAt?.let { Instant.fromEpochMilliseconds(it) },
    retrospect = retrospect,
    archivedAt = archivedAt?.let { Instant.fromEpochMilliseconds(it) },
    archiveReason = archiveReason,
    nudgeSnoozedUntil = nudgeSnoozedUntil?.let { Instant.fromEpochMilliseconds(it) },
    reminderLastSentAt = reminderLastSentAt?.let { Instant.fromEpochMilliseconds(it) },
)

fun Entry.toDto(): EntryDto = EntryDto(
    id = id,
    goalId = goalId,
    kind = kind.name,
    memo = memo,
    countDelta = countDelta,
    recordedAt = recordedAt.toEpochMilliseconds(),
    createdAt = createdAt.toEpochMilliseconds(),
    photos = photos.map { it.toDto() },
)

/** photos는 zip에 실제 파일이 있었는지 확인한 뒤(RestoreBackupUseCase) 걸러서 넘겨받는다. */
fun EntryDto.toDomain(photos: List<Photo>): Entry = Entry(
    id = id,
    goalId = goalId,
    kind = EntryKind.valueOf(kind),
    memo = memo,
    photos = photos,
    countDelta = countDelta,
    recordedAt = Instant.fromEpochMilliseconds(recordedAt),
    createdAt = Instant.fromEpochMilliseconds(createdAt),
)

fun Photo.toDto(): PhotoDto = PhotoDto(
    id = id,
    path = path,
    thumbnailPath = thumbnailPath,
    order = order,
    width = width,
    height = height,
)

fun PhotoDto.toDomain(entryId: String): Photo = Photo(
    id = id,
    entryId = entryId,
    path = path,
    thumbnailPath = thumbnailPath,
    order = order,
    width = width,
    height = height,
)
