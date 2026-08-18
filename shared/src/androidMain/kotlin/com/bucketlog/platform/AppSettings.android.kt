package com.bucketlog.platform

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "bucketlog_settings")

actual class AppSettings(private val context: Context) {
    actual suspend fun getBoolean(key: String, default: Boolean): Boolean =
        context.dataStore.data.first()[booleanPreferencesKey(key)] ?: default

    actual suspend fun setBoolean(key: String, value: Boolean) {
        context.dataStore.edit { it[booleanPreferencesKey(key)] = value }
    }

    actual suspend fun getInt(key: String, default: Int): Int =
        context.dataStore.data.first()[intPreferencesKey(key)] ?: default

    actual suspend fun setInt(key: String, value: Int) {
        context.dataStore.edit { it[intPreferencesKey(key)] = value }
    }

    actual suspend fun getLong(key: String, default: Long): Long =
        context.dataStore.data.first()[longPreferencesKey(key)] ?: default

    actual suspend fun setLong(key: String, value: Long) {
        context.dataStore.edit { it[longPreferencesKey(key)] = value }
    }

    actual suspend fun getString(key: String, default: String?): String? =
        context.dataStore.data.first()[stringPreferencesKey(key)] ?: default

    actual suspend fun setString(key: String, value: String) {
        context.dataStore.edit { it[stringPreferencesKey(key)] = value }
    }
}

