package com.bucketlog.data.mapper

import com.bucketlog.data.local.entity.EntryEntity
import com.bucketlog.data.local.entity.EntryWithPhotos
import com.bucketlog.data.local.entity.PhotoEntity
import com.bucketlog.domain.model.Entry
import com.bucketlog.domain.model.EntryKind
import com.bucketlog.domain.model.Photo
import kotlinx.datetime.Instant

fun PhotoEntity.toDomain(): Photo = Photo(
    id = id,
    entryId = entryId,
    path = path,
    thumbnailPath = thumbnailPath,
    order = orderIndex,
    width = width,
    height = height,
)

fun Photo.toEntity(): PhotoEntity = PhotoEntity(
    id = id,
    entryId = entryId,
    path = path,
    thumbnailPath = thumbnailPath,
    orderIndex = order,
    width = width,
    height = height,
)

fun EntryWithPhotos.toDomain(): Entry = Entry(
    id = entry.id,
    goalId = entry.goalId,
    kind = EntryKind.valueOf(entry.kind),
    memo = entry.memo,
    photos = photos.sortedBy { it.orderIndex }.map { it.toDomain() },
    countDelta = entry.countDelta,
    recordedAt = Instant.fromEpochMilliseconds(entry.recordedAt),
    createdAt = Instant.fromEpochMilliseconds(entry.createdAt),
)

fun Entry.toEntity(): EntryEntity = EntryEntity(
    id = id,
    goalId = goalId,
    kind = kind.name,
    memo = memo,
    countDelta = countDelta,
    recordedAt = recordedAt.toEpochMilliseconds(),
    createdAt = createdAt.toEpochMilliseconds(),
)
