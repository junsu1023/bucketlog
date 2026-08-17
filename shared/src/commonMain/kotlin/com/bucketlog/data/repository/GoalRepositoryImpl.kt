package com.bucketlog.data.repository

import com.bucketlog.data.local.dao.GoalDao
import com.bucketlog.data.mapper.toDomain
import com.bucketlog.data.mapper.toEntity
import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GoalRepositoryImpl(private val goalDao: GoalDao) : GoalRepository {
    override fun observeByStatus(status: GoalStatus): Flow<List<Goal>> =
        goalDao.observeByStatus(status.name).map { entities -> entities.map { it.toDomain() } }

    override fun observeById(id: String): Flow<Goal?> =
        goalDao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: String): Goal? = goalDao.getById(id)?.toDomain()

    override suspend fun add(goal: Goal) = goalDao.insert(goal.toEntity())

    override suspend fun update(goal: Goal) = goalDao.update(goal.toEntity())

    override suspend fun delete(id: String) = goalDao.deleteById(id)
}
