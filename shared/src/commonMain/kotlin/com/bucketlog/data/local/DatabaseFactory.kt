package com.bucketlog.data.local

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

/** 플랫폼별 파일 경로(Context / NSFileManager)만 다르고 나머지 빌드 설정은 공통이다. */
expect class DatabaseFactory {
    fun create(): AppDatabase
}

internal fun RoomDatabase.Builder<AppDatabase>.buildDatabase(): AppDatabase =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
