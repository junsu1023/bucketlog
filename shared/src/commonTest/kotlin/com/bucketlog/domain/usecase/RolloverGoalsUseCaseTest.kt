package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.Category
import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.model.GoalType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant

class RolloverGoalsUseCaseTest {

    private fun goal(id: String, bucketYear: Int? = 2026, status: GoalStatus = GoalStatus.IN_PROGRESS) = Goal(
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
        createdAt = Instant.fromEpochMilliseconds(0),
        completedAt = null,
        retrospect = null,
        archivedAt = null,
        archiveReason = null,
        nudgeSnoozedUntil = null,
        reminderLastSentAt = null,
    )

    private fun useCase(repo: FakeGoalRepository) = RolloverGoalsUseCase(repo, ArchiveGoalUseCase(repo))

    @Test
    fun `내년으로는 bucketYear를 년+1로 바꾼다`() = runBlocking {
        val repo = FakeGoalRepository(listOf(goal("g1")))
        useCase(repo)(2026, mapOf("g1" to RolloverDecision.NEXT_YEAR))
        assertEquals(2027, repo.getById("g1")?.bucketYear)
    }

    @Test
    fun `언젠가로는 bucketYear를 null로 바꾼다`() = runBlocking {
        val repo = FakeGoalRepository(listOf(goal("g1")))
        useCase(repo)(2026, mapOf("g1" to RolloverDecision.SOMEDAY))
        assertNull(repo.getById("g1")?.bucketYear)
    }

    @Test
    fun `이건 안 하기로 하기는 목표를 접어둔다`() = runBlocking {
        val repo = FakeGoalRepository(listOf(goal("g1")))
        useCase(repo)(2026, mapOf("g1" to RolloverDecision.ARCHIVE))
        assertEquals(GoalStatus.ARCHIVED, repo.getById("g1")?.status)
    }

    @Test
    fun `그대로 두기는 아무것도 바꾸지 않는다`() = runBlocking {
        val repo = FakeGoalRepository(listOf(goal("g1")))
        useCase(repo)(2026, mapOf("g1" to RolloverDecision.KEEP))
        assertEquals(2026, repo.getById("g1")?.bucketYear)
        assertEquals(GoalStatus.IN_PROGRESS, repo.getById("g1")?.status)
    }

    @Test
    fun `이미 완료·접어둔 목표는 결정이 와도 건드리지 않는다`() = runBlocking {
        val repo = FakeGoalRepository(listOf(goal("g1", status = GoalStatus.COMPLETED)))
        useCase(repo)(2026, mapOf("g1" to RolloverDecision.NEXT_YEAR))
        assertEquals(2026, repo.getById("g1")?.bucketYear)
        assertEquals(GoalStatus.COMPLETED, repo.getById("g1")?.status)
    }
}
