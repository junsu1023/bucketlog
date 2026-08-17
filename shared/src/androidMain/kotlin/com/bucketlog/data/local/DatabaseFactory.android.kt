package com.bucketlog.data.local

import android.content.Context
import androidx.room3.Room

actual class DatabaseFactory(private val context: Context) {
    actual fun create(): AppDatabase {
        val dbFile = context.applicationContext.getDatabasePath(AppDatabase.DB_NAME)
        return Room.databaseBuilder<AppDatabase>(
            context = context.applicationContext,
            name = dbFile.absolutePath,
        ).buildDatabase()
    }
}
