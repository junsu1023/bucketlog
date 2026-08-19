package com.bucketlog.data.local

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import com.bucketlog.data.local.dao.EntryDao
import com.bucketlog.data.local.dao.GoalDao
import com.bucketlog.data.local.dao.PhotoDao
import com.bucketlog.data.local.entity.EntryEntity
import com.bucketlog.data.local.entity.GoalEntity
import com.bucketlog.data.local.entity.PhotoEntity

@Database(
    entities = [GoalEntity::class, EntryEntity::class, PhotoEntity::class],
    version = 2,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun entryDao(): EntryDao
    abstract fun photoDao(): PhotoDao

    companion object {
        const val DB_NAME = "bucketlog.db"
    }
}

// Room KSP가 androidMain/iosMain 각각에 대해 실제 구현을 생성한다 (shared/build.gradle.kts의 kspAndroid/kspIos* 설정).
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>
