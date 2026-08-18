package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.Category
import com.bucketlog.domain.model.Entry
import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.model.GoalType
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.repository.GoalRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

private class FakeGoalRepository(initial: List<Goal>) : GoalRepository {
    private val goals = MutableStateFlow(initial)
    override fun observeByStatus(status: GoalStatus): Flow<List<Goal>> =
        goals.map { list -> list.filter { it.status == status } }
    override fun observeAll(): Flow<List<Goal>> = goals
    override fun observeById(id: String): Flow<Goal?> = goals.map { list -> list.find { it.id == id } }
    override suspend fun getById(id: String): Goal? = goals.value.find { it.id == id }
    override suspend fun add(goal: Goal) { goals.value = goals.value + goal }
    override suspend fun update(goal: Goal) {
        goals.value = goals.value.map { if (it.id == goal.id) goal else it }
    }
    override suspend fun delete(id: String) { goals.value = goals.value.filterNot { it.id == id } }
    override suspend fun upsert(goal: Goal) {
        goals.value = if (goals.value.any { it.id == goal.id }) {
            goals.value.map { if (it.id == goal.id) goal else it }
        } else {
            goals.value + goal
        }
    }
}

private class FakeEntryRepository(private val lastRecordedAt: Map<String, Instant>) : EntryRepository {
    override fun observeByGoal(goalId: String): Flow<List<Entry>> = flowOf(emptyList())
    override fun observeProgressTotals(): Flow<Map<String, Int>> = flowOf(emptyMap())
    override fun observeLastRecordedAt(): Flow<Map<String, Instant>> = flowOf(lastRecordedAt)
    override fun observeRecentPhotoPaths(): Flow<Map<String, List<String>>> = flowOf(emptyMap())
    override suspend fun getById(id: String): Entry? = null
    override suspend fun add(entry: Entry) = Unit
    override suspend fun update(entry: Entry) = Unit
    override suspend fun delete(id: String) = Unit
    override suspend fun demoteCompletionEntry(goalId: String) = false
    override suspend fun getAll(): List<Entry> = emptyList()
    override suspend fun upsert(entry: Entry) = Unit
}

class PickNudgeTargetUseCaseTest {

    private val now = LocalDateTime(2026, 8, 17, 12, 0).toInstant(TimeZone.currentSystemDefault())

    private fun goal(
        id: String,
        status: GoalStatus = GoalStatus.IN_PROGRESS,
        bucketYear: Int? = 2026,
        nudgeSnoozedUntil: Instant? = null,
        createdAt: Instant = now - 100.days,
    ) = Goal(
        id = id,
        title = id,
        note = null,
        category = Category.OTHER,
        type = GoalType.ONE_TIME,
        targetCount = null,
        status = status,
        bucketYear = bucketYear,
        dueDate = null,
        coverEntryId = null,
        reminderRule = null,
        createdAt = createdAt,
        completedAt = null,
        retrospect = null,
        archivedAt = null,
        archiveReason = null,
        nudgeSnoozedUntil = nudgeSnoozedUntil,
    )

    @Test
    fun `30일 이상 방치된 목표를 고른다`() = runBlocking {
        val useCase = PickNudgeTargetUseCase(
            FakeGoalRepository(listOf(goal("g1"))),
            FakeEntryRepository(mapOf("g1" to now - 31.days)),
        )
        assertEquals("g1", useCase(now)?.id)
    }

    @Test
    fun `30일 미만이면 고르지 않는다`() = runBlocking {
        val useCase = PickNudgeTargetUseCase(
            FakeGoalRepository(listOf(goal("g1"))),
            FakeEntryRepository(mapOf("g1" to now - 10.days)),
        )
        assertNull(useCase(now))
    }

    @Test
    fun `언젠가 버킷은 넛지하지 않는다`() = runBlocking {
        val useCase = PickNudgeTargetUseCase(
            FakeGoalRepository(listOf(goal("g1", bucketYear = null))),
            FakeEntryRepository(mapOf("g1" to now - 40.days)),
        )
        assertNull(useCase(now))
    }

    @Test
    fun `스누즈 중인 목표는 제외한다`() = runBlocking {
        val useCase = PickNudgeTargetUseCase(
            FakeGoalRepository(listOf(goal("g1", nudgeSnoozedUntil = now + 1.days))),
            FakeEntryRepository(mapOf("g1" to now - 40.days)),
        )
        assertNull(useCase(now))
    }

    @Test
    fun `진행중이 아닌 목표는 제외한다`() = runBlocking {
        val useCase = PickNudgeTargetUseCase(
            FakeGoalRepository(listOf(goal("g1", status = GoalStatus.COMPLETED))),
            FakeEntryRepository(mapOf("g1" to now - 40.days)),
        )
        assertNull(useCase(now))
    }

    @Test
    fun `여러 후보 중 가장 오래 방치된 것을 고른다`() = runBlocking {
        val useCase = PickNudgeTargetUseCase(
            FakeGoalRepository(listOf(goal("g1"), goal("g2"))),
            FakeEntryRepository(mapOf("g1" to now - 35.days, "g2" to now - 50.days)),
        )
        assertEquals("g2", useCase(now)?.id)
    }

    @Test
    fun `기록이 없으면 목표 생성 시점을 마지막 활동으로 본다`() = runBlocking {
        val useCase = PickNudgeTargetUseCase(
            FakeGoalRepository(listOf(goal("g1", createdAt = now - 31.days))),
            FakeEntryRepository(emptyMap()),
        )
        assertEquals("g1", useCase(now)?.id)
    }
}
