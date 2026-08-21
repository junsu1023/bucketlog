package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.Entry
import com.bucketlog.domain.model.EntryKind
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.util.newId
import com.bucketlog.platform.FileStorage
import com.bucketlog.platform.ImageProcessor
import kotlin.time.Clock

/**
 * MVP-SCOPE.md E-02: 진행 기록 — 메모 + 사진(0~5장) + 날짜.
 * 규칙 3(CLAUDE.md): 메모만, 사진만, 둘 다 허용 — 카운트 증가만으로도 유효한 기록이라 허용한다.
 * 셋 다 없는 완전히 빈 기록만 막는다.
 */
class AddProgressEntryUseCase(
    private val entryRepository: EntryRepository,
    private val imageProcessor: ImageProcessor,
    private val fileStorage: FileStorage,
) {
    suspend operator fun invoke(
        goalId: String,
        memo: String?,
        photoBytes: List<ByteArray>,
        incrementCount: Boolean,
    ) {
        require(photoBytes.size <= 5) { "사진은 최대 5장까지" }
        val trimmedMemo = memo?.trim()?.ifBlank { null }
        require(trimmedMemo != null || photoBytes.isNotEmpty() || incrementCount) { "메모, 사진, 카운트 중 하나는 있어야 함" }

        val entryId = newId()
        val photos = processPhotosForEntry(entryId, photoBytes, imageProcessor, fileStorage)
        val now = Clock.System.now()

        entryRepository.add(
            Entry(
                id = entryId,
                goalId = goalId,
                kind = EntryKind.PROGRESS,
                memo = trimmedMemo,
                photos = photos,
                countDelta = if (incrementCount) 1 else 0,
                recordedAt = now,
                createdAt = now,
            ),
        )
    }
}
