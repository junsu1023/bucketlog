package com.bucketlog.data.repository

import com.bucketlog.data.local.dao.GoalDao
import com.bucketlog.data.local.dao.PhotoDao
import com.bucketlog.data.mapper.toDomain
import com.bucketlog.data.mapper.toEntity
import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.repository.GoalRepository
import com.bucketlog.platform.FileStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GoalRepositoryImpl(
    private val goalDao: GoalDao,
    private val photoDao: PhotoDao,
    private val fileStorage: FileStorage,
) : GoalRepository {
    override fun observeByStatus(status: GoalStatus): Flow<List<Goal>> =
        goalDao.observeByStatus(status.name).map { entities -> entities.map { it.toDomain() } }

    override fun observeAll(): Flow<List<Goal>> =
        goalDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeById(id: String): Flow<Goal?> =
        goalDao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: String): Goal? = goalDao.getById(id)?.toDomain()

    override suspend fun add(goal: Goal) = goalDao.insert(goal.toEntity())

    override suspend fun update(goal: Goal) = goalDao.update(goal.toEntity())

    override suspend fun upsert(goal: Goal) = goalDao.upsert(goal.toEntity())

    override suspend fun delete(id: String) {
        val photos = photoDao.getByGoal(id)
        goalDao.deleteById(id)
        photos.forEach { photo ->
            fileStorage.delete(photo.path)
            fileStorage.delete(photo.thumbnailPath)
        }
    }

    override suspend fun deleteAll() {
        val photos = photoDao.getAll()
        goalDao.deleteAll()
        photos.forEach { photo ->
            fileStorage.delete(photo.path)
            fileStorage.delete(photo.thumbnailPath)
        }
    }
}
