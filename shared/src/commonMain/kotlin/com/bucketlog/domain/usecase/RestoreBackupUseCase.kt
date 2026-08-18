package com.bucketlog.domain.usecase

import com.bucketlog.data.backup.BackupData
import com.bucketlog.data.backup.CURRENT_SCHEMA_VERSION
import com.bucketlog.data.backup.EntryDto
import com.bucketlog.data.backup.GoalDto
import com.bucketlog.data.backup.PhotoDto
import com.bucketlog.data.backup.toDomain
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.repository.GoalRepository
import com.bucketlog.platform.FileStorage
import com.bucketlog.platform.ZipArchiver
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

sealed interface RestoreResult {
    data class Success(val goalCount: Int, val entryCount: Int) : RestoreResult
    data object SchemaTooNew : RestoreResult
    data object CorruptFile : RestoreResult
}

/** 백업이 실제로 반영할 내용. 사진 파일이 zip에 없는 PhotoDto는 photos에서 걸러진 상태다. */
data class RestorePlan(val goals: List<GoalDto>, val entries: List<Pair<EntryDto, List<PhotoDto>>>)

/**
 * docs/DATA-MODEL.md §7 복원 규칙: id 충돌 시 백업이 이기고(머지, 파괴적이지 않음),
 * 사진 파일이 없으면 Entry는 유지한 채 사진 참조만 뺀다. 순수 함수라 commonTest에서 직접 검증한다.
 */
fun planRestore(backup: BackupData, availablePhotoPaths: Set<String>): RestorePlan {
    val entries = backup.entries.map { entry ->
        val availablePhotos = entry.photos.filter {
            it.path in availablePhotoPaths && it.thumbnailPath in availablePhotoPaths
        }
        entry to availablePhotos
    }
    return RestorePlan(goals = backup.goals, entries = entries)
}

class RestoreBackupUseCase(
    private val zipArchiver: ZipArchiver,
    private val fileStorage: FileStorage,
    private val goalRepository: GoalRepository,
    private val entryRepository: EntryRepository,
) {
    suspend operator fun invoke(zipBytes: ByteArray): RestoreResult {
        val zipEntries = runCatching { zipArchiver.unzip(zipBytes) }.getOrNull()
            ?: return RestoreResult.CorruptFile
        val dataEntry = zipEntries.find { it.path == DATA_JSON_PATH }
            ?: return RestoreResult.CorruptFile
        val backup = runCatching { Json.decodeFromString<BackupData>(dataEntry.bytes.decodeToString()) }
            .getOrNull() ?: return RestoreResult.CorruptFile

        if (backup.schemaVersion > CURRENT_SCHEMA_VERSION) return RestoreResult.SchemaTooNew

        val photoBytesByPath = zipEntries
            .filter { it.path.startsWith(PHOTOS_DIR_PREFIX) }
            .associate { it.path to it.bytes }
        val plan = planRestore(backup, photoBytesByPath.keys)

        // 사진 파일을 먼저 전부 쓴다 — 도중에 실패해도 DB는 아직 손대지 않은 상태라 그대로 안전하다.
        val filesWritten = runCatching {
            plan.entries.forEach { (_, photos) ->
                photos.forEach { photo ->
                    photoBytesByPath[photo.path]?.let { fileStorage.writeBytes(photo.path, it) }
                    photoBytesByPath[photo.thumbnailPath]?.let { fileStorage.writeBytes(photo.thumbnailPath, it) }
                }
            }
        }.isSuccess
        if (!filesWritten) return RestoreResult.CorruptFile

        plan.goals.forEach { goalRepository.upsert(it.toDomain()) }
        plan.entries.forEach { (entryDto, photos) ->
            entryRepository.upsert(entryDto.toDomain(photos.map { it.toDomain(entryDto.id) }))
        }

        return RestoreResult.Success(goalCount = plan.goals.size, entryCount = plan.entries.size)
    }
}
