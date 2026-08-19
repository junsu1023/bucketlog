package com.bucketlog.presentation.common

import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** N-01 월간 회고 / 보관함 "이번 달" 탭이 공유하는 연-월 키. kotlinx-datetime에 YearMonth가 없어 직접 정의. */
data class MonthKey(val year: Int, val month: Int) {
    override fun toString(): String = "$year-${month.toString().padStart(2, '0')}"

    companion object {
        fun current(): MonthKey {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            return MonthKey(today.year, today.monthNumber)
        }

        /** "2026-09" -> MonthKey(2026, 9). 딥링크(bucketlog://archive?month=2026-09) 파싱용. */
        fun parse(text: String): MonthKey? {
            val parts = text.split("-")
            if (parts.size != 2) return null
            val year = parts[0].toIntOrNull() ?: return null
            val month = parts[1].toIntOrNull() ?: return null
            if (month !in 1..12) return null
            return MonthKey(year, month)
        }
    }
}
