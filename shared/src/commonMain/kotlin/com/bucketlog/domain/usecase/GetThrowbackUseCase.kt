package com.bucketlog.domain.usecase

import com.bucketlog.domain.repository.EntryRepository
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

/** H-07 작년 오늘 — 위젯에서 쓸 수 있게 도메인 계층에 둔 버전. 1년 전 기록이 있으면 우선,
 * 없으면 1개월 전으로 대체한다. `presentation.home.ThrowbackBanner`와 선정 로직은 같지만,
 * 위젯(도메인 계층에서 직접 접근)이 presentation 패키지에 의존하지 않도록 따로 둔다.
 */
data class ThrowbackPick(val goalId: String, val goalTitle: String, val isYearAgo: Boolean)

class GetThrowbackUseCase(private val entryRepository: EntryRepository) {
    suspend operator fun invoke(): ThrowbackPick? {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        val yearAgo = entryRepository.observeEntriesOnDate(today.minus(DatePeriod(years = 1))).first()
        yearAgo.firstOrNull()?.let { return ThrowbackPick(it.entry.goalId, it.goalTitle, isYearAgo = true) }

        val monthAgo = entryRepository.observeEntriesOnDate(today.minus(DatePeriod(months = 1))).first()
        val month = monthAgo.firstOrNull() ?: return null
        return ThrowbackPick(month.entry.goalId, month.goalTitle, isYearAgo = false)
    }
}
