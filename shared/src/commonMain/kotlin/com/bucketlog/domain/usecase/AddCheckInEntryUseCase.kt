package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.Entry
import com.bucketlog.domain.model.EntryKind
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.util.newId
import kotlin.time.Clock

/** MVP-SCOPE.md E-01: 한 줄 퀵 체크인. countDelta는 항상 0 — "가고 싶다"와 "갔다"를 구분한다. */
class AddCheckInEntryUseCase(private val entryRepository: EntryRepository) {
    suspend operator fun invoke(goalId: String, memo: String) {
        require(memo.isNotBlank()) { "check-in memo must not be blank" }
        val now = Clock.System.now()
        entryRepository.add(
            Entry(
                id = newId(),
                goalId = goalId,
                kind = EntryKind.CHECK_IN,
                memo = memo.trim(),
                photos = emptyList(),
                countDelta = 0,
                recordedAt = now,
                createdAt = now,
            ),
        )
    }
}
