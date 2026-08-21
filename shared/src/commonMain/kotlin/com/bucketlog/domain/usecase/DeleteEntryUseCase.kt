package com.bucketlog.domain.usecase

import com.bucketlog.domain.repository.EntryRepository

/** MVP-SCOPE.md E-05: 기록 삭제. 사진 파일 정리는 EntryRepositoryImpl.delete가 처리한다. */
class DeleteEntryUseCase(private val entryRepository: EntryRepository) {
    suspend operator fun invoke(entryId: String) {
        entryRepository.delete(entryId)
    }
}
