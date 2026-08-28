package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.repository.GoalRepository
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** 위젯("2026년의 나")용 연간 요약. HomeViewModel의 summaryTotal/summaryCompleted와 같은 계산을
 * 도메인 계층에 그대로 옮겨왔다 — 위젯은 presentation 계층(HomeViewModel)에 의존할 수 없다.
 */
data class YearSummary(val year: Int, val total: Int, val completed: Int)

class GetYearSummaryUseCase(private val goalRepository: GoalRepository) {
    suspend operator fun invoke(year: Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).year): YearSummary {
        val goals = goalRepository.observeAll().first().filter { it.bucketYear == year }
        return YearSummary(
            year = year,
            total = goals.size,
            completed = goals.count { it.status == GoalStatus.COMPLETED },
        )
    }
}
