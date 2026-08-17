package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.Entry
import com.bucketlog.domain.model.EntryKind
import com.bucketlog.domain.model.GoalStatus
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.repository.GoalRepository
import com.bucketlog.domain.util.newId
import com.bucketlog.platform.FileStorage
import com.bucketlog.platform.ImageProcessor
import kotlin.time.Clock

/**
 * MVP-SCOPE.md G-06/E-07: 완료 처리 — 사진 + 회고 + 완료일.
 * docs/DATA-MODEL.md §5: COMPLETION Entry 생성 + completedAt/retrospect 기록.
 */
class CompleteGoalUseCase(
    private val goalRepository: GoalRepository,
    private val entryRepository: EntryRepository,
    private val imageProcessor: ImageProcessor,
    private val fileStorage: FileStorage,
) {
    suspend operator fun invoke(goalId: String, retrospect: String?, photoBytes: List<ByteArray> = emptyList()) {
        require(photoBytes.size <= 5) { "사진은 최대 5장까지" }
        val goal = requireNotNull(goalRepository.getById(goalId)) { "goal not found: $goalId" }
        check(goal.status == GoalStatus.IN_PROGRESS) { "only IN_PROGRESS goals can be completed" }

        val now = Clock.System.now()
        val trimmedRetrospect = retrospect?.trim()?.ifBlank { null }
        val entryId = newId()
        val photos = processPhotosForEntry(entryId, photoBytes, imageProcessor, fileStorage)

        entryRepository.add(
            Entry(
                id = entryId,
                goalId = goalId,
                kind = EntryKind.COMPLETION,
                memo = trimmedRetrospect,
                photos = photos,
                countDelta = 0,
                recordedAt = now,
                createdAt = now,
            ),
        )
        goalRepository.update(
            goal.copy(
                status = GoalStatus.COMPLETED,
                completedAt = now,
                retrospect = trimmedRetrospect,
            ),
        )
    }
}
