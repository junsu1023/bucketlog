package com.bucketlog.platform

/**
 * 로컬 알림 예약/취소 + 권한 확인. docs/ARCHITECTURE.md §3 expect/actual 경계,
 * docs/NOTIFICATIONS.md §6 시그니처. 이 클래스를 직접 호출하지 말고 반드시
 * notification/NotificationBudget을 거쳐라 (알림 총량 제어가 여기서 깨진다).
 */
// 생성자를 선언하지 않는다 — Android는 Context, iOS는 무인자로 각기 다르게 구성한다
// (FileStorage/DatabaseFactory와 동일한 패턴). 인스턴스는 Koin DI로만 얻는다.
expect class NotificationScheduler {
    suspend fun schedule(notification: LocalNotification)
    suspend fun cancel(id: String)
    suspend fun cancelAll()
    suspend fun hasPermission(): Boolean
    suspend fun requestPermission(): Boolean
}
