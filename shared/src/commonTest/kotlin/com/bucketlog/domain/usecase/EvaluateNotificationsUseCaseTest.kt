package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.Category
import com.bucketlog.domain.model.Entry
import com.bucketlog.domain.model.EntryKind
import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.model.GoalType
import com.bucketlog.domain.model.ReminderInterval
import com.bucketlog.domain.model.ReminderRule
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.repository.GoalRepository
import com.bucketlog.domain.repository.MonthlyEntry
import com.bucketlog.notification.NotificationBudget
import com.bucketlog.notification.SettingsStore
import com.bucketlog.platform.LocalNotification
import com.bucketlog.platform.NotificationType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant

class EvaluateNotificationsUseCaseTest {

    // usecase 내부가 Clock.System.now()의 실제 현재 시각을 기준으로 판정하므로, 픽스처의 기준점도
    // 리터럴 날짜 대신 실행 시점의 실제 시각으로 잡는다 — 세션이 며칠 걸려도 안 깨지도록.
    private val now = Clock.System.now()

    private fun goal(id: String, reminderLastSentAt: Instant? = null, createdAt: Instant = now - 200.days) = Goal(
        id = id,
        title = id,
        note = null,
        category = Category.OTHER,
        type = GoalType.ONE_TIME,
        targetCount = null,
        status = GoalStatus.IN_PROGRESS,
        bucketYear = 2026,
        dueDate = null,
        coverEntryId = null,
        reminderRule = ReminderRule(ReminderInterval.WEEKLY, enabled = true),
        createdAt = createdAt,
        completedAt = null,
        retrospect = null,
        archivedAt = null,
        archiveReason = null,
        nudgeSnoozedUntil = null,
        reminderLastSentAt = reminderLastSentAt,
    )

    private fun monthlyEntry() = MonthlyEntry(
        entry = Entry(
            id = "e1", goalId = "g1", kind = EntryKind.CHECK_IN, memo = "memo",
            photos = emptyList(), countDelta = 0, recordedAt = now, createdAt = now,
        ),
        goalTitle = "g1",
        photoPaths = emptyList(),
    )

    private fun evaluate(
        goalRepository: GoalRepository,
        entryRepository: EntryRepository,
        settings: SettingsStore,
        captured: MutableList<LocalNotification>,
    ): EvaluateNotificationsUseCase {
        val budget = NotificationBudget(settings) { captured += it }
        val recap = ScheduleMonthlyRecapUseCase(
            entryRepository = entryRepository,
            notificationBudget = budget,
            settings = settings,
            cancelNotification = {},
            recapTitle = { "버킷로그" },
            recapBody = { "회고" },
        )
        val reminders = ScheduleGoalRemindersUseCase(goalRepository, budget, settings) { "지금 어때요?" }
        val nudge = ScheduleNudgeUseCase(
            PickNudgeTargetUseCase(goalRepository, entryRepository),
            entryRepository,
            goalRepository,
            budget,
            settings,
        ) { "넛지" }
        // 픽스처 목표엔 dueDate가 없어서 마감임박은 항상 false. 연말회고는 실제 오늘이 12월이면
        // true가 나올 수 있어 이 테스트만 12월엔 깨질 수 있음(같은 한계가 ScheduleYearEndRecapUseCase
        // 자체에도 있다, docs/NOTIFICATIONS.md §6 참고) — 그 외 11개월은 기존 3종 우선순위 그대로.
        val yearEndRecap = ScheduleYearEndRecapUseCase(budget, settings, { "버킷로그" }, { "" }, { "" })
        val dueSoon = ScheduleDueSoonUseCase(goalRepository, budget, settings) { "" }
        return EvaluateNotificationsUseCase(yearEndRecap, dueSoon, recap, reminders, nudge)
    }

    @Test
    fun `월간회고가 조건을 만족하면 다른 알림보다 우선한다`() = runBlocking {
        val goalRepo = FakeGoalRepository(listOf(goal("g1", reminderLastSentAt = now - 30.days)))
        val entryRepo = FakeEntryRepository(monthlyEntries = listOf(monthlyEntry()))
        val captured = mutableListOf<LocalNotification>()

        evaluate(goalRepo, entryRepo, FakeSettingsStore(), captured)()

        assertEquals(NotificationType.MONTHLY_RECAP, captured.single().type)
    }

    @Test
    fun `월간회고 조건이 안 되면 목표별 리마인더로 넘어간다`() = runBlocking {
        val goalRepo = FakeGoalRepository(listOf(goal("g1", reminderLastSentAt = now - 30.days)))
        val entryRepo = FakeEntryRepository(monthlyEntries = emptyList())
        val captured = mutableListOf<LocalNotification>()

        evaluate(goalRepo, entryRepo, FakeSettingsStore(), captured)()

        assertEquals(NotificationType.GOAL_REMINDER, captured.single().type)
    }

    @Test
    fun `월간회고·목표별리마인더 둘 다 조건이 안 되면 넛지로 넘어간다`() = runBlocking {
        val goalRepo = FakeGoalRepository(listOf(goal("g1", reminderLastSentAt = now)))
        val entryRepo = FakeEntryRepository(
            lastRecordedAt = mapOf("g1" to now - 40.days),
            monthlyEntries = emptyList(),
        )
        val captured = mutableListOf<LocalNotification>()

        evaluate(goalRepo, entryRepo, FakeSettingsStore(), captured)()

        assertEquals(NotificationType.NUDGE, captured.single().type)
    }

    @Test
    fun `아무 조건도 안 되면 아무것도 보내지 않는다`() = runBlocking {
        val goalRepo = FakeGoalRepository(listOf(goal("g1", reminderLastSentAt = now)))
        val entryRepo = FakeEntryRepository(
            lastRecordedAt = mapOf("g1" to now - 1.days),
            monthlyEntries = emptyList(),
        )
        val captured = mutableListOf<LocalNotification>()

        evaluate(goalRepo, entryRepo, FakeSettingsStore(), captured)()

        assertEquals(0, captured.size)
    }
}
