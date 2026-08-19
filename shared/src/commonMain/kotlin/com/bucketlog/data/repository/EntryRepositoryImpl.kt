package com.bucketlog.data.repository

import com.bucketlog.data.local.dao.EntryDao
import com.bucketlog.data.local.dao.EntryWithGoalTitle
import com.bucketlog.data.local.dao.PhotoDao
import com.bucketlog.data.mapper.toDomain
import com.bucketlog.data.mapper.toEntity
import com.bucketlog.domain.model.Entry
import com.bucketlog.domain.model.EntryKind
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.repository.MonthlyEntry
import com.bucketlog.platform.FileStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus

class EntryRepositoryImpl(
    private val entryDao: EntryDao,
    private val photoDao: PhotoDao,
    private val fileStorage: FileStorage,
) : EntryRepository {
    override fun observeByGoal(goalId: String): Flow<List<Entry>> =
        entryDao.observeByGoal(goalId).map { rows -> rows.map { it.toDomain() } }

    override fun observeProgressTotals(): Flow<Map<String, Int>> =
        entryDao.observeProgressTotals().map { rows -> rows.associate { it.goalId to it.total } }

    override fun observeLastRecordedAt(): Flow<Map<String, Instant>> =
        entryDao.observeLastRecordedAt().map { rows ->
            rows.associate { it.goalId to Instant.fromEpochMilliseconds(it.lastRecordedAt) }
        }

    override fun observeRecentPhotoPaths(): Flow<Map<String, List<String>>> =
        entryDao.observeCoverCandidates().map { rows ->
            rows.groupBy { it.goalId }
                .mapValues { (_, candidates) ->
                    val latestEntryId = candidates.maxBy { it.recordedAt }.entryId
                    candidates.filter { it.entryId == latestEntryId }
                        .sortedBy { it.orderIndex }
                        .map { "file://" + fileStorage.resolveAbsolutePath(it.thumbnailPath) }
                }
        }

    override suspend fun getById(id: String): Entry? = entryDao.getById(id)?.let { entity ->
        val photos = photoDao.getByEntry(id).sortedBy { it.orderIndex }
        Entry(
            id = entity.id,
            goalId = entity.goalId,
            kind = EntryKind.valueOf(entity.kind),
            memo = entity.memo,
            photos = photos.map { it.toDomain() },
            countDelta = entity.countDelta,
            recordedAt = Instant.fromEpochMilliseconds(entity.recordedAt),
            createdAt = Instant.fromEpochMilliseconds(entity.createdAt),
        )
    }

    override suspend fun add(entry: Entry) {
        entryDao.insert(entry.toEntity())
        entry.photos.forEach { photo -> photoDao.insert(photo.toEntity()) }
    }

    override suspend fun update(entry: Entry) = entryDao.update(entry.toEntity())

    override suspend fun delete(id: String) {
        val photos = photoDao.getByEntry(id)
        entryDao.getById(id)?.let { entryDao.delete(it) }
        photos.forEach { photo ->
            fileStorage.delete(photo.path)
            fileStorage.delete(photo.thumbnailPath)
        }
    }

    override suspend fun demoteCompletionEntry(goalId: String): Boolean {
        val completion = entryDao.findCompletionEntry(goalId) ?: return false
        entryDao.update(completion.copy(kind = EntryKind.PROGRESS.name))
        return true
    }

    override suspend fun getAll(): List<Entry> = entryDao.getAllWithPhotos().map { it.toDomain() }

    override suspend fun upsert(entry: Entry) {
        entryDao.upsert(entry.toEntity())
        entry.photos.forEach { photo -> photoDao.upsert(photo.toEntity()) }
    }

    override fun observeEntriesInMonth(year: Int, month: Int): Flow<List<MonthlyEntry>> {
        val zone = TimeZone.currentSystemDefault()
        val start = LocalDate(year, month, 1).atStartOfDayIn(zone).toEpochMilliseconds()
        val nextMonthFirstDay = if (month == 12) LocalDate(year + 1, 1, 1) else LocalDate(year, month + 1, 1)
        val end = nextMonthFirstDay.atStartOfDayIn(zone).toEpochMilliseconds()
        return entryDao.observeEntriesInRange(start, end).map { rows -> rows.map { it.toMonthlyEntry() } }
    }

    override fun observeEntriesOnDate(date: LocalDate): Flow<List<MonthlyEntry>> {
        val zone = TimeZone.currentSystemDefault()
        val start = date.atStartOfDayIn(zone).toEpochMilliseconds()
        val end = date.plus(DatePeriod(days = 1)).atStartOfDayIn(zone).toEpochMilliseconds()
        return entryDao.observeEntriesInRange(start, end).map { rows -> rows.map { it.toMonthlyEntry() } }
    }

    override fun observeAllEntries(): Flow<List<MonthlyEntry>> =
        entryDao.observeEntriesInRange(0L, Long.MAX_VALUE).map { rows -> rows.map { it.toMonthlyEntry() } }

    private fun EntryWithGoalTitle.toMonthlyEntry() = MonthlyEntry(
        entry = toDomain(),
        goalTitle = goalTitle,
        photoPaths = photos.sortedBy { it.orderIndex }
            .map { "file://" + fileStorage.resolveAbsolutePath(it.thumbnailPath) },
    )
}
