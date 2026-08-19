package com.bucketlog.presentation.home

import com.bucketlog.domain.model.Entry
import com.bucketlog.domain.model.EntryKind
import com.bucketlog.domain.repository.MonthlyEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.Instant

class ThrowbackBannerTest {

    private fun monthlyEntry(goalId: String, goalTitle: String) = MonthlyEntry(
        entry = Entry(
            id = "e-$goalId",
            goalId = goalId,
            kind = EntryKind.PROGRESS,
            memo = null,
            photos = emptyList(),
            countDelta = 0,
            recordedAt = Instant.fromEpochMilliseconds(0),
            createdAt = Instant.fromEpochMilliseconds(0),
        ),
        goalTitle = goalTitle,
        photoPaths = emptyList(),
    )

    @Test
    fun `1년 전 기록이 있으면 그걸 고른다`() {
        val result = pickThrowback(
            yearAgo = listOf(monthlyEntry("g1", "여행")),
            monthAgo = listOf(monthlyEntry("g2", "독서")),
        )
        assertEquals(ThrowbackKind.YEAR_AGO, result?.kind)
        assertEquals("g1", result?.goalId)
    }

    @Test
    fun `1년 전은 없고 1개월 전만 있으면 그걸 고른다`() {
        val result = pickThrowback(
            yearAgo = emptyList(),
            monthAgo = listOf(monthlyEntry("g2", "독서")),
        )
        assertEquals(ThrowbackKind.MONTH_AGO, result?.kind)
        assertEquals("g2", result?.goalId)
    }

    @Test
    fun `둘 다 없으면 null`() {
        assertNull(pickThrowback(yearAgo = emptyList(), monthAgo = emptyList()))
    }

    @Test
    fun `같은 날짜에 여러 기록이 있으면 가장 최근 것을 고른다`() {
        // observeEntriesInRange가 이미 recordedAt DESC로 정렬해 내려주므로 firstOrNull이 최신이다.
        val latest = monthlyEntry("g-latest", "최신")
        val older = monthlyEntry("g-older", "예전")
        val result = pickThrowback(yearAgo = listOf(latest, older), monthAgo = emptyList())
        assertEquals("g-latest", result?.goalId)
    }
}
