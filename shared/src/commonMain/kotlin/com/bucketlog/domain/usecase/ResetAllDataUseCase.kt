package com.bucketlog.domain.usecase

import com.bucketlog.domain.repository.GoalRepository

/** M-03 데이터 초기화 — 목표/기록/사진을 전부 지운다. 되돌릴 수 없다(docs/MVP-SCOPE.md §2.10). */
class ResetAllDataUseCase(private val goalRepository: GoalRepository) {
    suspend operator fun invoke() = goalRepository.deleteAll()
}
