package com.bucketlog.data.local

import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers

/** 플랫폼별 파일 경로(Context / NSFileManager)만 다르고 나머지 빌드 설정은 공통이다. */
expect class DatabaseFactory {
    fun create(): AppDatabase
}

/** N-03 목표별 리마인더의 마지막 발송 시각 컬럼 추가. 기록은 유저의 기억이라(CLAUDE.md 규칙 6)
 *  destructive migration은 쓰지 않는다 — 기존 데이터를 보존하는 ALTER TABLE만 수행한다. */
private val MIGRATION_1_2 = Migration(1, 2) { connection ->
    connection.execSQL("ALTER TABLE goals ADD COLUMN reminder_last_sent_at INTEGER")
}

internal fun RoomDatabase.Builder<AppDatabase>.buildDatabase(): AppDatabase =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .addMigrations(MIGRATION_1_2)
        .build()
