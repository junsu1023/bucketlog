package com.bucketlog.domain.usecase

import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.repository.MonthlyEntry
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime

/** H-07 작년 오늘 — 위젯("오늘의 추억")에서 쓸 수 있게 도메인 계층에 둔 버전. 1년 전 기록이
 * 있으면 우선, 없으면 1개월 전으로 대체한다. `presentation.home.ThrowbackBanner`와 선정
 * 로직은 같지만, 위젯(도메인 계층에서 직접 접근)이 presentation 패키지에 의존하지 않도록
 * 따로 둔다. 사진/메모까지 담아 위젯이 텍스트뿐 아니라 실제 추억 카드를 그릴 수 있게 한다.
 */
data class ThrowbackPick(
    val goalId: String,
    val goalTitle: String,
    val isYearAgo: Boolean,
    val recordedDate: LocalDate,
    val memo: String?,
    /** 썸네일(~320px) 경로. 여러 장이면 대표로 첫 장만 — 위젯은 로딩 비용을 최소화해야 한다. */
    val photoPath: String?,
)

class GetThrowbackUseCase(private val entryRepository: EntryRepository) {
    suspend operator fun invoke(): ThrowbackPick? {
        val zone = TimeZone.currentSystemDefault()
        val today = Clock.System.todayIn(zone)

        val yearAgo = entryRepository.observeEntriesOnDate(today.minus(DatePeriod(years = 1))).first()
        yearAgo.firstOrNull()?.let { return it.toPick(isYearAgo = true, zone) }

        val monthAgo = entryRepository.observeEntriesOnDate(today.minus(DatePeriod(months = 1))).first()
        val month = monthAgo.firstOrNull() ?: return null
        return month.toPick(isYearAgo = false, zone)
    }

    private fun MonthlyEntry.toPick(isYearAgo: Boolean, zone: TimeZone) = ThrowbackPick(
        goalId = entry.goalId,
        goalTitle = goalTitle,
        isYearAgo = isYearAgo,
        recordedDate = entry.recordedAt.toLocalDateTime(zone).date,
        memo = entry.memo?.takeIf { it.isNotBlank() },
        photoPath = photoPaths.firstOrNull(),
    )
}
