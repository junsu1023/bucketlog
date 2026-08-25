package com.bucketlog.notification

import com.bucketlog.platform.LocalNotification
import com.bucketlog.platform.NotificationType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private class FakeSettingsStore : SettingsStore {
    val booleans = mutableMapOf<String, Boolean>()
    val longs = mutableMapOf<String, Long>()
    val strings = mutableMapOf<String, String>()

    override suspend fun getBoolean(key: String, default: Boolean) = booleans[key] ?: default
    override suspend fun setBoolean(key: String, value: Boolean) { booleans[key] = value }
    override suspend fun getLong(key: String, default: Long) = longs[key] ?: default
    override suspend fun setLong(key: String, value: Long) { longs[key] = value }
    override suspend fun getString(key: String, default: String?) = strings[key] ?: default
    override suspend fun setString(key: String, value: String) { strings[key] = value }
}

class NotificationBudgetTest {

    private val zone = TimeZone.currentSystemDefault()

    private fun notificationAt(localDateTime: LocalDateTime, id: String = "n1") = LocalNotification(
        id = id,
        type = NotificationType.NUDGE,
        title = "title",
        body = "body",
        scheduledAt = localDateTime.toInstant(zone),
        deepLink = "bucketlog://goal/g1?focus=checkin",
    )

    @Test
    fun `일반 시간대 알림은 그대로 예약된다`() = runBlocking {
        val settings = FakeSettingsStore()
        var scheduled: LocalNotification? = null
        val budget = NotificationBudget(settings) { scheduled = it }

        val notification = notificationAt(LocalDateTime(2026, 8, 17, 20, 0))
        val sent = budget.requestSend(notification)

        assertTrue(sent)
        assertEquals(notification.scheduledAt, scheduled?.scheduledAt)
    }

    @Test
    fun `조용한 시간 21시 이후엔 다음날 9시로 미뤄진다`() = runBlocking {
        val settings = FakeSettingsStore()
        var scheduled: LocalNotification? = null
        val budget = NotificationBudget(settings) { scheduled = it }

        budget.requestSend(notificationAt(LocalDateTime(2026, 8, 17, 22, 30)))

        val adjustedLocal = requireNotNull(scheduled).scheduledAt.toLocalDateTime(zone)
        assertEquals(LocalDateTime(2026, 8, 18, 9, 0), adjustedLocal)
    }

    @Test
    fun `조용한 시간 9시 이전엔 같은 날 9시로 미뤄진다`() = runBlocking {
        val settings = FakeSettingsStore()
        var scheduled: LocalNotification? = null
        val budget = NotificationBudget(settings) { scheduled = it }

        budget.requestSend(notificationAt(LocalDateTime(2026, 8, 17, 6, 0)))

        val adjustedLocal = requireNotNull(scheduled).scheduledAt.toLocalDateTime(zone)
        assertEquals(LocalDateTime(2026, 8, 17, 9, 0), adjustedLocal)
    }

    @Test
    fun `마지막 발송 후 7일 이내면 차단된다`() = runBlocking {
        val settings = FakeSettingsStore()
        var scheduledCount = 0
        val budget = NotificationBudget(settings) { scheduledCount++ }

        budget.requestSend(notificationAt(LocalDateTime(2026, 8, 10, 20, 0), id = "first"))
        val blocked = budget.requestSend(notificationAt(LocalDateTime(2026, 8, 16, 20, 0), id = "second"))

        assertFalse(blocked)
        assertEquals(1, scheduledCount)
    }

    @Test
    fun `마지막 발송 후 7일이 지나면 다시 보낼 수 있다`() = runBlocking {
        val settings = FakeSettingsStore()
        var scheduledCount = 0
        val budget = NotificationBudget(settings) { scheduledCount++ }

        budget.requestSend(notificationAt(LocalDateTime(2026, 8, 10, 20, 0), id = "first"))
        val sent = budget.requestSend(notificationAt(LocalDateTime(2026, 8, 17, 20, 0), id = "second"))

        assertTrue(sent)
        assertEquals(2, scheduledCount)
    }

    @Test
    fun `전체 알림이 꺼져 있으면 종류와 무관하게 차단된다`() = runBlocking {
        val settings = FakeSettingsStore().apply { booleans[NotificationSettingsKeys.NOTIFICATIONS_ENABLED] = false }
        var scheduled: LocalNotification? = null
        val budget = NotificationBudget(settings) { scheduled = it }

        val sent = budget.requestSend(notificationAt(LocalDateTime(2026, 8, 17, 20, 0)))

        assertFalse(sent)
        assertNull(scheduled)
    }

    @Test
    fun `넛지만 꺼져 있으면 넛지만 차단된다`() = runBlocking {
        val settings = FakeSettingsStore().apply { booleans[NotificationSettingsKeys.NUDGE_ENABLED] = false }
        var scheduled: LocalNotification? = null
        val budget = NotificationBudget(settings) { scheduled = it }

        val sent = budget.requestSend(notificationAt(LocalDateTime(2026, 8, 17, 20, 0)))

        assertFalse(sent)
        assertNull(scheduled)
    }
}
