package com.bucketlog.platform

/**
 * 알림 on/off, 알림 시각, 예산(NotificationBudget) 상태 같은 단순 key-value 설정 저장소.
 * docs/ARCHITECTURE.md §3 expect/actual 경계. Android는 DataStore, iOS는 NSUserDefaults.
 */
// 생성자를 선언하지 않는다 — Android는 Context, iOS는 무인자로 각기 다르게 구성한다
// (FileStorage/DatabaseFactory와 동일한 패턴). 인스턴스는 Koin DI로만 얻는다.
expect class AppSettings {
    suspend fun getBoolean(key: String, default: Boolean): Boolean
    suspend fun setBoolean(key: String, value: Boolean)
    suspend fun getInt(key: String, default: Int): Int
    suspend fun setInt(key: String, value: Int)
    suspend fun getLong(key: String, default: Long): Long
    suspend fun setLong(key: String, value: Long)
    suspend fun getString(key: String, default: String?): String?
    suspend fun setString(key: String, value: String)
}
