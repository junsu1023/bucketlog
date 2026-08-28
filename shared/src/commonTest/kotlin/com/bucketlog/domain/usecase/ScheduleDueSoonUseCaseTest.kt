package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.Category
import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.model.GoalType
import com.bucketlog.notification.NotificationBudget
import com.bucketlog.platform.LocalNotification
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.Clock
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/**
 * dueDate를 테스트 실행 시점의 실제 오늘(Clock.System.todayIn)에 상대적으로 잡는다 —
 * 리터럴 날짜를 쓰면 세션이 여러 날에 걸쳐 진행될 때 픽스처가 낡아 깨진다
 * (EvaluateNotificationsUseCaseTest/ScheduleGoalRemindersUseCaseTest가 겪고 있는 문제, test.md 참고).
 */
class ScheduleDueSoonUseCaseTest {

    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    private fun goal(id: String, dueDate: kotlinx.datetime.LocalDate?, status: GoalStatus = GoalStatus.IN_PROGRESS) = Goal(
        id = id,
        title = id,
        note = null,
        category = Category.OTHER,
        type = GoalType.ONE_TIME,
        targetCount = null,
        status = status,
        bucketYear = today.year,
        dueDate = dueDate,
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

    private fun useCase(repo: FakeGoalRepository, captured: MutableList<LocalNotification>): ScheduleDueSoonUseCase {
        val settings = FakeSettingsStore()
        val budget = NotificationBudget(settings) { captured += it }
        return ScheduleDueSoonUseCase(PickDueSoonGoalUseCase(repo), budget, settings) { title -> "due:$title" }
    }

    @Test
    fun `마감 7일 이내면 알림을 보낸다`() = runBlocking {
        val repo = FakeGoalRepository(listOf(goal("g1", today.plus(DatePeriod(days = 5)))))
        val captured = mutableListOf<LocalNotification>()
        useCase(repo, captured)()
        assertEquals("g1", captured.single().title)
    }

    @Test
    fun `마감이 8일 이상 남았으면 보내지 않는다`() = runBlocking {
        val repo = FakeGoalRepository(listOf(goal("g1", today.plus(DatePeriod(days = 8)))))
        val captured = mutableListOf<LocalNotification>()
        useCase(repo, captured)()
        assertFalse(captured.isNotEmpty())
    }

    @Test
    fun `이미 지난 마감일은 보내지 않는다`() = runBlocking {
        val repo = FakeGoalRepository(listOf(goal("g1", today.plus(DatePeriod(days = -1)))))
        val captured = mutableListOf<LocalNotification>()
        useCase(repo, captured)()
        assertFalse(captured.isNotEmpty())
    }

    @Test
    fun `마감일이 없는 목표는 무시한다`() = runBlocking {
        val repo = FakeGoalRepository(listOf(goal("g1", dueDate = null)))
        val captured = mutableListOf<LocalNotification>()
        useCase(repo, captured)()
        assertFalse(captured.isNotEmpty())
    }

    @Test
    fun `여러 목표 중 마감이 가장 가까운 것 하나만 고른다`() = runBlocking {
        val repo = FakeGoalRepository(
            listOf(
                goal("far", today.plus(DatePeriod(days = 6))),
                goal("near", today.plus(DatePeriod(days = 2))),
            ),
        )
        val captured = mutableListOf<LocalNotification>()
        useCase(repo, captured)()
        assertEquals("near", captured.single().title)
    }
}
