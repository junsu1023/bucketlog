package com.bucketlog.domain.model

import kotlinx.datetime.Instant

/**
 * 기록 3종(퀵 체크인 / 진행 기록 / 완료 인증)을 하나로 다루는 엔티티.
 * 상세 규칙: docs/DATA-MODEL.md §2
 */
data class Entry(
    val id: String,
    val goalId: String,
    val kind: EntryKind,
    val memo: String?,
    val photos: List<Photo>,         // 0~5
    val countDelta: Int,             // REPEATABLE 진행량. CHECK_IN은 기본 0
    val recordedAt: Instant,         // 유저가 수정 가능 (소급 기록)
    val createdAt: Instant,          // 실제 생성 시각 (수정 불가) — 넛지 판정에 사용
)

enum class EntryKind {
    CHECK_IN,     // 퀵 체크인 — 한 줄. countDelta = 0
    PROGRESS,     // 진행 기록 — 사진 + 메모. countDelta = 1 (조정 가능)
    COMPLETION,   // 완료 인증 — 목표당 1개
}
