package com.bucketlog.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * 작은 목표 / 버킷리스트 항목.
 * 상세 규칙: docs/DATA-MODEL.md, docs/MVP-SCOPE.md
 */
data class Goal(
    val id: String,                  // UUID
    val title: String,
    val note: String?,               // "왜 하고 싶은지"
    val category: Category,
    val type: GoalType,
    val targetCount: Int?,           // REPEATABLE만. ONE_TIME이면 null
    val status: GoalStatus,
    val bucketYear: Int?,            // 2026 / null = "언젠가"
    val dueDate: LocalDate?,
    val coverEntryId: String?,       // 대표 사진이 담긴 Entry
    val reminderRule: ReminderRule?, // 목표별 리마인더 (기본 null = 꺼짐)
    val createdAt: Instant,
    val completedAt: Instant?,
    val retrospect: String?,         // 완료 회고
    val archivedAt: Instant?,
    val archiveReason: String?,      // 접어둔 이유
    val nudgeSnoozedUntil: Instant?, // 넛지 무응답 시 제외 기간
)

enum class GoalType { ONE_TIME, REPEATABLE }

enum class GoalStatus { IN_PROGRESS, COMPLETED, ARCHIVED }

enum class Category {
    TRAVEL, HOBBY, RELATIONSHIP, CHALLENGE, LEARNING, HEALTH, OTHER
}

/** 목표별 리마인더. 최소 주기는 주 1회 — 매일 옵션은 존재하지 않는다 (docs/NOTIFICATIONS.md) */
data class ReminderRule(
    val interval: ReminderInterval,
    val enabled: Boolean,
)

enum class ReminderInterval { WEEKLY, BIWEEKLY, MONTHLY }
