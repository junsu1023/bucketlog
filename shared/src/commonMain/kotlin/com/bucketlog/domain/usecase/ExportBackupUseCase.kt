package com.bucketlog.domain.usecase

import com.bucketlog.data.backup.BackupData
import com.bucketlog.data.backup.CURRENT_SCHEMA_VERSION
import com.bucketlog.data.backup.toDto
import com.bucketlog.domain.repository.EntryRepository
import com.bucketlog.domain.repository.GoalRepository
import com.bucketlog.platform.FileStorage
import com.bucketlog.platform.ZipArchiver
import com.bucketlog.platform.ZipEntryData
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** M-02 백업 내보내기. docs/DATA-MODEL.md §7 zip 포맷 그대로. */
class ExportBackupUseCase(
    private val goalRepository: GoalRepository,
    private val entryRepository: EntryRepository,
    private val fileStorage: FileStorage,
    private val zipArchiver: ZipArchiver,
) {
    suspend operator fun invoke(): BackupFile {
        val goals = goalRepository.observeAll().first()
        val entries = entryRepository.getAll()

        val zipEntries = mutableListOf<ZipEntryData>()
        val backupData = BackupData(
            schemaVersion = CURRENT_SCHEMA_VERSION,
            exportedAt = Clock.System.now().toEpochMilliseconds(),
            goals = goals.map { it.toDto() },
            entries = entries.map { entry ->
                entry.photos.forEach { photo ->
                    val display = fileStorage.readBytes(photo.path)
                    val thumbnail = fileStorage.readBytes(photo.thumbnailPath)
                    if (display != null && thumbnail != null) {
                        zipEntries += ZipEntryData(photo.path, display)
                        zipEntries += ZipEntryData(photo.thumbnailPath, thumbnail)
                    }
                }
                entry.toDto()
            },
        )

        zipEntries += ZipEntryData(DATA_JSON_PATH, Json.encodeToString(backupData).encodeToByteArray())
        zipEntries += ZipEntryData(VERSION_TXT_PATH, CURRENT_SCHEMA_VERSION.toString().encodeToByteArray())

        val zipBytes = zipArchiver.zip(zipEntries)
        return BackupFile(fileName = suggestedFileName(), bytes = zipBytes)
    }

    private fun suggestedFileName(): String {
        val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val year = date.year.toString().padStart(4, '0')
        val month = date.monthNumber.toString().padStart(2, '0')
        val day = date.dayOfMonth.toString().padStart(2, '0')
        return "bucketlog-backup-$year$month$day.zip"
    }
}

data class BackupFile(val fileName: String, val bytes: ByteArray)

internal const val DATA_JSON_PATH = "data.json"
internal const val VERSION_TXT_PATH = "version.txt"
internal const val PHOTOS_DIR_PREFIX = "photos/"
