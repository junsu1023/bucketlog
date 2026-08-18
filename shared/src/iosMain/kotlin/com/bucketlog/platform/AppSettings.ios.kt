package com.bucketlog.platform

import platform.Foundation.NSUserDefaults

actual class AppSettings {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual suspend fun getBoolean(key: String, default: Boolean): Boolean =
        if (defaults.objectForKey(key) != null) defaults.boolForKey(key) else default

    actual suspend fun setBoolean(key: String, value: Boolean) {
        defaults.setBool(value, key)
    }

    actual suspend fun getInt(key: String, default: Int): Int =
        if (defaults.objectForKey(key) != null) defaults.integerForKey(key).toInt() else default

    actual suspend fun setInt(key: String, value: Int) {
        defaults.setInteger(value.toLong(), key)
    }

    actual suspend fun getLong(key: String, default: Long): Long =
        if (defaults.objectForKey(key) != null) defaults.integerForKey(key) else default

    actual suspend fun setLong(key: String, value: Long) {
        defaults.setInteger(value, key)
    }

    actual suspend fun getString(key: String, default: String?): String? =
        defaults.stringForKey(key) ?: default

    actual suspend fun setString(key: String, value: String) {
        defaults.setObject(value, key)
    }
}
