package com.bucketlog.presentation.archive

import com.bucketlog.presentation.common.MonthKey

sealed interface ArchiveIntent {
    data class SelectTab(val tab: ArchiveTab) : ArchiveIntent

    /** "이번 달" 탭 클릭 또는 N-01 딥링크 진입 — 탭도 MONTHLY로 같이 바꾼다. */
    data class ShowMonth(val month: MonthKey) : ArchiveIntent
}
