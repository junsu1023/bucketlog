package com.bucketlog.notification

import com.bucketlog.platform.AppSettings

/** [SettingsStore]를 실제 [AppSettings](DataStore/NSUserDefaults)로 위임하는 어댑터. */
class AppSettingsStore(private val appSettings: AppSettings) : SettingsStore {
    override suspend fun getBoolean(key: String, default: Boolean): Boolean =
        appSettings.getBoolean(key, default)

    override suspend fun setBoolean(key: String, value: Boolean) =
        appSettings.setBoolean(key, value)

    override suspend fun getLong(key: String, default: Long): Long =
        appSettings.getLong(key, default)

    override suspend fun setLong(key: String, value: Long) =
        appSettings.setLong(key, value)

    override suspend fun getString(key: String, default: String?): String? =
        appSettings.getString(key, default)

    override suspend fun setString(key: String, value: String) =
        appSettings.setString(key, value)
}
