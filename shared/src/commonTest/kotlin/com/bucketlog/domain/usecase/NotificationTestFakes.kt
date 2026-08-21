package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.Entry
import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.repository.GoalRepository
import com.bucketlog.domain.repository.MonthlyEntry
import com.bucketlog.notification.SettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * 알림 관련 usecase 테스트(PickNudgeTargetUseCaseTest, ScheduleGoalRemindersUseCaseTest,
 * EvaluateNotificationsUseCaseTest)가 공유하는 페이크. 같은 패키지 안에서 top-level
 * private 클래스는 이름이 같으면 파일이 달라도 충돌하므로(private 함수와 달리 파일별로
 * 맹글링되지 않음) 여기 한 곳에 모아 internal로 공유한다.
 */
internal class FakeGoalRepository(initial: List<Goal> = emptyList()) : GoalRepository {
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
    override suspend fun upsert(goal: Goal) = update(goal)
    override suspend fun deleteAll() { goals.value = emptyList() }
}

internal class FakeEntryRepository(
    private val lastRecordedAt: Map<String, Instant> = emptyMap(),
    private val monthlyEntries: List<MonthlyEntry> = emptyList(),
) : EntryRepository {
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
    override fun observeEntriesInMonth(year: Int, month: Int): Flow<List<MonthlyEntry>> = flowOf(monthlyEntries)
    override fun observeEntriesOnDate(date: LocalDate): Flow<List<MonthlyEntry>> = flowOf(monthlyEntries)
    override fun observeAllEntries(): Flow<List<MonthlyEntry>> = flowOf(monthlyEntries)
}

internal class FakeSettingsStore : SettingsStore {
    private val booleans = mutableMapOf<String, Boolean>()
    private val longs = mutableMapOf<String, Long>()
    private val strings = mutableMapOf<String, String>()
    override suspend fun getBoolean(key: String, default: Boolean) = booleans[key] ?: default
    override suspend fun setBoolean(key: String, value: Boolean) { booleans[key] = value }
    override suspend fun getLong(key: String, default: Long) = longs[key] ?: default
    override suspend fun setLong(key: String, value: Long) { longs[key] = value }
    override suspend fun getString(key: String, default: String?) = strings[key] ?: default
    override suspend fun setString(key: String, value: String) { strings[key] = value }
}
