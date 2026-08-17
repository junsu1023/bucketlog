package com.bucketlog.data.mapper

import com.bucketlog.data.local.entity.GoalEntity
import com.bucketlog.domain.model.Category
import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.model.GoalType
import com.bucketlog.domain.model.ReminderInterval
import com.bucketlog.domain.model.ReminderRule
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

fun GoalEntity.toDomain(): Goal = Goal(
    id = id,
    title = title,
    note = note,
    category = Category.valueOf(category),
    type = GoalType.valueOf(type),
    targetCount = targetCount,
    status = GoalStatus.valueOf(status),
    bucketYear = bucketYear,
    dueDate = dueDate?.let(LocalDate::fromEpochDays),
    coverEntryId = coverEntryId,
    reminderRule = reminderInterval?.let {
        ReminderRule(interval = ReminderInterval.valueOf(it), enabled = reminderEnabled)
    },
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    completedAt = completedAt?.let(Instant::fromEpochMilliseconds),
    retrospect = retrospect,
    archivedAt = archivedAt?.let(Instant::fromEpochMilliseconds),
    archiveReason = archiveReason,
    nudgeSnoozedUntil = nudgeSnoozedUntil?.let(Instant::fromEpochMilliseconds),
)

fun Goal.toEntity(): GoalEntity = GoalEntity(
    id = id,
    title = title,
    note = note,
    category = category.name,
    type = type.name,
    targetCount = targetCount,
    status = status.name,
    bucketYear = bucketYear,
    dueDate = dueDate?.toEpochDays(),
    coverEntryId = coverEntryId,
    reminderInterval = reminderRule?.interval?.name,
    reminderEnabled = reminderRule?.enabled ?: false,
    createdAt = createdAt.toEpochMilliseconds(),
    completedAt = completedAt?.toEpochMilliseconds(),
    retrospect = retrospect,
    archivedAt = archivedAt?.toEpochMilliseconds(),
    archiveReason = archiveReason,
    nudgeSnoozedUntil = nudgeSnoozedUntil?.toEpochMilliseconds(),
)
