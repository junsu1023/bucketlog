package com.bucketlog.notification

/**
 * [com.bucketlog.platform.AppSettings]를 감싼 인터페이스. NotificationBudget이
 * 이 인터페이스에만 의존해서, 실제 플랫폼 저장소(DataStore/NSUserDefaults) 없이도
 * 예산 로직(7일 상한·조용한 시간·on/off)을 유닛 테스트할 수 있다.
 */
interface SettingsStore {
    suspend fun getBoolean(key: String, default: Boolean): Boolean
    suspend fun setBoolean(key: String, value: Boolean)
    suspend fun getLong(key: String, default: Long): Long
    suspend fun setLong(key: String, value: Long)
    suspend fun getString(key: String, default: String?): String?
    suspend fun setString(key: String, value: String)
}
