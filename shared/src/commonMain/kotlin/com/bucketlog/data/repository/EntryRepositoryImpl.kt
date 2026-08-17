package com.bucketlog.data.repository

import com.bucketlog.data.local.dao.EntryDao
import com.bucketlog.data.mapper.toDomain
import com.bucketlog.data.mapper.toEntity
import com.bucketlog.domain.model.Entry
import com.bucketlog.domain.model.EntryKind
import com.bucketlog.domain.repository.EntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class EntryRepositoryImpl(private val entryDao: EntryDao) : EntryRepository {
    override fun observeByGoal(goalId: String): Flow<List<Entry>> =
        entryDao.observeByGoal(goalId).map { rows -> rows.map { it.toDomain() } }

    override fun observeProgressTotals(): Flow<Map<String, Int>> =
        entryDao.observeProgressTotals().map { rows -> rows.associate { it.goalId to it.total } }

    override fun observeLastRecordedAt(): Flow<Map<String, Instant>> =
        entryDao.observeLastRecordedAt().map { rows ->
            rows.associate { it.goalId to Instant.fromEpochMilliseconds(it.lastRecordedAt) }
        }

    override suspend fun getById(id: String): Entry? = entryDao.getById(id)?.let { entity ->
        // photos 없이 단건 조회 — 현재 UseCase들은 photos를 쓰지 않는다. 필요해지면 EntryWithPhotos 쿼리로 교체.
        Entry(
            id = entity.id,
            goalId = entity.goalId,
            kind = EntryKind.valueOf(entity.kind),
            memo = entity.memo,
            photos = emptyList(),
            countDelta = entity.countDelta,
            recordedAt = Instant.fromEpochMilliseconds(entity.recordedAt),
            createdAt = Instant.fromEpochMilliseconds(entity.createdAt),
        )
    }

    override suspend fun add(entry: Entry) = entryDao.insert(entry.toEntity())

    override suspend fun update(entry: Entry) = entryDao.update(entry.toEntity())

    override suspend fun delete(id: String) {
        entryDao.getById(id)?.let { entryDao.delete(it) }
    }

    override suspend fun demoteCompletionEntry(goalId: String): Boolean {
        val completion = entryDao.findCompletionEntry(goalId) ?: return false
        entryDao.update(completion.copy(kind = EntryKind.PROGRESS.name))
        return true
    }
}
