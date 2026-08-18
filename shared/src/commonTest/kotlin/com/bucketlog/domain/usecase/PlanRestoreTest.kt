package com.bucketlog.domain.usecase

import com.bucketlog.data.backup.BackupData
import com.bucketlog.data.backup.EntryDto
import com.bucketlog.data.backup.GoalDto
import com.bucketlog.data.backup.PhotoDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlanRestoreTest {

    private fun goalDto(id: String) = GoalDto(
        id = id,
        title = id,
        note = null,
        category = "OTHER",
        type = "ONE_TIME",
        targetCount = null,
        status = "IN_PROGRESS",
        bucketYear = 2026,
        dueDate = null,
        coverEntryId = null,
        reminderInterval = null,
        reminderEnabled = false,
        createdAt = 0L,
        completedAt = null,
        retrospect = null,
        archivedAt = null,
        archiveReason = null,
        nudgeSnoozedUntil = null,
    )

    private fun photoDto(id: String) = PhotoDto(
        id = id,
        path = "photos/$id.jpg",
        thumbnailPath = "photos/${id}_thumb.jpg",
        order = 0,
        width = 100,
        height = 100,
    )

    private fun entryDto(id: String, goalId: String, photos: List<PhotoDto> = emptyList()) = EntryDto(
        id = id,
        goalId = goalId,
        kind = "CHECK_IN",
        memo = null,
        countDelta = 0,
        recordedAt = 0L,
        createdAt = 0L,
        photos = photos,
    )

    @Test
    fun `백업의 goal과 entry가 그대로 계획에 담긴다`() {
        val backup = BackupData(
            schemaVersion = 1,
            exportedAt = 0L,
            goals = listOf(goalDto("g1")),
            entries = listOf(entryDto("e1", "g1")),
        )

        val plan = planRestore(backup, availablePhotoPaths = emptySet())

        assertEquals(listOf("g1"), plan.goals.map { it.id })
        assertEquals(1, plan.entries.size)
        assertEquals("e1", plan.entries.single().first.id)
    }

    @Test
    fun `사진 파일이 둘 다 있으면 계획에 포함된다`() {
        val photo = photoDto("p1")
        val backup = BackupData(
            schemaVersion = 1,
            exportedAt = 0L,
            goals = listOf(goalDto("g1")),
            entries = listOf(entryDto("e1", "g1", listOf(photo))),
        )

        val plan = planRestore(backup, availablePhotoPaths = setOf(photo.path, photo.thumbnailPath))

        val (entry, photos) = plan.entries.single()
        assertEquals("e1", entry.id)
        assertEquals(listOf("p1"), photos.map { it.id })
    }

    @Test
    fun `사진 파일이 없으면 Entry는 남고 사진만 빠진다`() {
        val photo = photoDto("p1")
        val backup = BackupData(
            schemaVersion = 1,
            exportedAt = 0L,
            goals = listOf(goalDto("g1")),
            entries = listOf(entryDto("e1", "g1", listOf(photo))),
        )

        val plan = planRestore(backup, availablePhotoPaths = emptySet())

        val (entry, photos) = plan.entries.single()
        assertEquals("e1", entry.id) // Entry 자체는 유지
        assertTrue(photos.isEmpty()) // 사진 참조만 빠짐
    }

    @Test
    fun `사진 중 썸네일만 없으면 그 사진은 제외된다`() {
        val photo = photoDto("p1")
        val backup = BackupData(
            schemaVersion = 1,
            exportedAt = 0L,
            goals = listOf(goalDto("g1")),
            entries = listOf(entryDto("e1", "g1", listOf(photo))),
        )

        // display만 있고 thumbnail은 zip에 없는 상태
        val plan = planRestore(backup, availablePhotoPaths = setOf(photo.path))

        assertTrue(plan.entries.single().second.isEmpty())
    }

    @Test
    fun `여러 goal과 entry를 모두 계획에 담는다`() {
        val backup = BackupData(
            schemaVersion = 1,
            exportedAt = 0L,
            goals = listOf(goalDto("g1"), goalDto("g2")),
            entries = listOf(entryDto("e1", "g1"), entryDto("e2", "g2")),
        )

        val plan = planRestore(backup, availablePhotoPaths = emptySet())

        assertEquals(2, plan.goals.size)
        assertEquals(2, plan.entries.size)
    }
}
