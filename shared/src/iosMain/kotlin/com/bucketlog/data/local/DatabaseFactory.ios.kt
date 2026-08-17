package com.bucketlog.data.local

import androidx.room3.Room
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual class DatabaseFactory {
    actual fun create(): AppDatabase {
        val dbFilePath = documentDirectory() + "/${AppDatabase.DB_NAME}"
        return Room.databaseBuilder<AppDatabase>(name = dbFilePath).buildDatabase()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun documentDirectory(): String {
        val directory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        return requireNotNull(directory?.path) { "iOS documents 디렉토리를 찾을 수 없음" }
    }
}
