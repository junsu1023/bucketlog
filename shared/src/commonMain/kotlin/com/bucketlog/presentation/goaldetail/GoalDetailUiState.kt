package com.bucketlog.presentation.goaldetail

import com.bucketlog.domain.model.Entry
import com.bucketlog.domain.model.Goal

data class GoalDetailUiState(
    val goal: Goal? = null,
    /** 최신순(recordedAt DESC) — DAO 쿼리와 동일한 정렬을 그대로 씀. 타임라인 렌더링 시 위가 최신. */
    val timeline: List<TimelineEntry> = emptyList(),
    val progressCount: Int = 0,
    val checkInDraft: String = "",
    val pendingAction: PendingAction? = null,
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val deleted: Boolean = false,
)

/** Entry + 사진의 절대(file://) 경로. FileStorage 해석은 ViewModel(플랫폼 접근 가능 계층)에서 미리 끝내둔다. */
data class TimelineEntry(
    val entry: Entry,
    val photos: List<TimelinePhoto>,
)

/** [thumbnailPath]는 타임라인 인라인 표시용, [displayPath]는 D-05 전체화면 뷰어의 원본 화질용. */
data class TimelinePhoto(
    val thumbnailPath: String,
    val displayPath: String,
)

sealed interface PendingAction {
    data object ConfirmComplete : PendingAction
    data object ConfirmArchive : PendingAction
    data object ConfirmDelete : PendingAction
    data object AddProgress : PendingAction
}
