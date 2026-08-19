package com.bucketlog.presentation.home

import com.bucketlog.domain.repository.MonthlyEntry

/** H-07 작년 오늘. 1년 전 기록이 있으면 우선, 없으면 1개월 전으로 대체(1년치 데이터가 쌓이기 전에도 뭔가 보이도록). */
data class ThrowbackBanner(val kind: ThrowbackKind, val goalId: String, val goalTitle: String)

enum class ThrowbackKind { YEAR_AGO, MONTH_AGO }

/** [yearAgo]/[monthAgo]는 이미 recordedAt DESC로 정렬되어 들어온다(EntryDao.observeEntriesInRange). */
internal fun pickThrowback(yearAgo: List<MonthlyEntry>, monthAgo: List<MonthlyEntry>): ThrowbackBanner? {
    val year = yearAgo.firstOrNull()
    if (year != null) return ThrowbackBanner(ThrowbackKind.YEAR_AGO, year.entry.goalId, year.goalTitle)
    val month = monthAgo.firstOrNull() ?: return null
    return ThrowbackBanner(ThrowbackKind.MONTH_AGO, month.entry.goalId, month.goalTitle)
}
