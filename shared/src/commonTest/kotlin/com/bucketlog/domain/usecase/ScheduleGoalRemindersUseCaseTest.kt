package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.Category
import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.model.GoalType
import com.bucketlog.domain.model.ReminderInterval
import com.bucketlog.domain.model.ReminderRule
import com.bucketlog.domain.repository.GoalRepository
import com.bucketlog.notification.NotificationBudget
import com.bucketlog.platform.LocalNotification
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class ScheduleGoalRemindersUseCaseTest {

    private val now = LocalDateTime(2026, 8, 17, 12, 0).toInstant(TimeZone.currentSystemDefault())

    private fun goal(
        id: String,
        interval: ReminderInterval? = ReminderInterval.WEEKLY,
        reminderEnabled: Boolean = true,
        reminderLastSentAt: Instant? = null,
        createdAt: Instant = now - 100.days,
        status: GoalStatus = GoalStatus.IN_PROGRESS,
    ) = Goal(
        id = id,
        title = id,
        note = null,
        category = Category.OTHER,
        type = GoalType.ONE_TIME,
        targetCount = null,
        status = status,
        bucketYear = 2026,
        dueDate = null,
        coverEntryId = null,
        reminderRule = interval?.let { ReminderRule(it, reminderEnabled) },
        createdAt = createdAt,
        completedAt = null,
        retrospect = null,
        archivedAt = null,
        archiveReason = null,
        nudgeSnoozedUntil = null,
        reminderLastSentAt = reminderLastSentAt,
    )

    private fun useCase(goalRepository: GoalRepository, captured: MutableList<LocalNotification>) =
        ScheduleGoalRemindersUseCase(
            goalRepository = goalRepository,
            notificationBudget = NotificationBudget(FakeSettingsStore()) { captured += it },
            settings = FakeSettingsStore(),
        ) { "지금 어때요?" }

    @Test
    fun `주 1회 주기가 지난 목표를 고른다`() = runBlocking {
        val repo = FakeGoalRepository(listOf(goal("g1", reminderLastSentAt = now - 8.days)))
        val captured = mutableListOf<LocalNotification>()

        val sent = useCase(repo, captured)()

        assertTrue(sent)
        assertEquals("g1", captured.single().deepLink.substringAfter("goal/").substringBefore("?"))
    }

    @Test
    fun `주기가 아직 안 된 목표는 고르지 않는다`() = runBlocking {
        val repo = FakeGoalRepository(listOf(goal("g1", reminderLastSentAt = now - 2.days)))
        val captured = mutableListOf<LocalNotification>()

        val sent = useCase(repo, captured)()

        assertFalse(sent)
        assertTrue(captured.isEmpty())
    }

    @Test
    fun `리마인더가 꺼진 목표는 제외한다`() = runBlocking {
        val repo = FakeGoalRepository(listOf(goal("g1", reminderEnabled = false, reminderLastSentAt = now - 30.days)))
        val captured = mutableListOf<LocalNotification>()

        val sent = useCase(repo, captured)()

        assertFalse(sent)
    }

    @Test
    fun `리마인더를 켜지 않은 목표는 제외한다`() = runBlocking {
        val repo = FakeGoalRepository(listOf(goal("g1", interval = null, reminderLastSentAt = now - 30.days)))
        val captured = mutableListOf<LocalNotification>()

        val sent = useCase(repo, captured)()

        assertFalse(sent)
    }

    @Test
    fun `여러 개가 밀렸으면 가장 오래 밀린 것을 고른다`() = runBlocking {
        val repo = FakeGoalRepository(
            listOf(
                goal("g1", reminderLastSentAt = now - 9.days),
                goal("g2", reminderLastSentAt = now - 20.days),
            ),
        )
        val captured = mutableListOf<LocalNotification>()

        useCase(repo, captured)()

        assertEquals("goal_reminder_g2", captured.single().id)
    }

    @Test
    fun `발송 성공 시 reminderLastSentAt을 갱신한다`() = runBlocking {
        val original = now - 8.days
        val repo = FakeGoalRepository(listOf(goal("g1", reminderLastSentAt = original)))
        val captured = mutableListOf<LocalNotification>()

        useCase(repo, captured)()

        // usecase 내부는 Clock.System.now()의 실제 현재 시각을 쓰므로 픽스처의 now와 정확히
        // 같지 않다 — 갱신됐는지(원래 값보다 뒤로 밀렸는지)만 확인한다.
        val updated = repo.getById("g1")?.reminderLastSentAt
        assertTrue(updated != null && updated > original)
    }

    @Test
    fun `기록 없이 생성만 됐으면 생성 시점을 기준으로 삼는다`() = runBlocking {
        val repo = FakeGoalRepository(listOf(goal("g1", reminderLastSentAt = null, createdAt = now - 8.days)))
        val captured = mutableListOf<LocalNotification>()

        val sent = useCase(repo, captured)()

        assertTrue(sent)
    }

    @Test
    fun `진행중이 아닌 목표는 제외한다`() = runBlocking {
        val original = now - 30.days
        val repo = FakeGoalRepository(
            listOf(goal("g1", reminderLastSentAt = original, status = GoalStatus.COMPLETED)),
        )
        val captured = mutableListOf<LocalNotification>()

        val sent = useCase(repo, captured)()

        assertFalse(sent)
        assertEquals(original, repo.getById("g1")?.reminderLastSentAt)
    }
}
