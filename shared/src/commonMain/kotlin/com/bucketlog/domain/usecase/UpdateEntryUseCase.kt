package com.bucketlog.domain.usecase

import com.bucketlog.domain.repository.EntryRepository
import kotlinx.datetime.Instant

/**
 * MVP-SCOPE.md E-04/E-05: 기록 메모·날짜 소급 수정.
 * 사진/카운트/종류는 이 화면에서 바꿀 수 없게 두고, 메모와 recordedAt만 다시 쓴다.
 */
class UpdateEntryUseCase(private val entryRepository: EntryRepository) {
    suspend operator fun invoke(entryId: String, memo: String?, recordedAt: Instant) {
        val entry = entryRepository.getById(entryId) ?: return
        entryRepository.update(entry.copy(memo = memo?.trim()?.ifBlank { null }, recordedAt = recordedAt))
    }
}
