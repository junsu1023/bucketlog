package com.bucketlog.platform

import kotlin.time.Clock
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

actual class NotificationScheduler {
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun schedule(notification: LocalNotification) {
        val content = UNMutableNotificationContent().apply {
            setTitle(notification.title)
            setBody(notification.body)
            setUserInfo(mapOf("deepLink" to notification.deepLink))
        }
        val intervalSeconds = (notification.scheduledAt.toEpochMilliseconds() - Clock.System.now().toEpochMilliseconds()) / 1000.0
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = intervalSeconds.coerceAtLeast(1.0),
            repeats = false,
        )
        val request = UNNotificationRequest.requestWithIdentifier(notification.id, content, trigger)
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    actual suspend fun cancel(id: String) {
        center.removePendingNotificationRequestsWithIdentifiers(listOf(id))
        center.removeDeliveredNotificationsWithIdentifiers(listOf(id))
    }

    actual suspend fun cancelAll() {
        center.removeAllPendingNotificationRequests()
        center.removeAllDeliveredNotifications()
    }

    actual suspend fun hasPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        center.getNotificationSettingsWithCompletionHandler { settings ->
            continuation.resume(settings?.authorizationStatus == UNAuthorizationStatusAuthorized, onCancellation = null)
        }
    }

    actual suspend fun requestPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
        ) { granted, _ ->
            continuation.resume(granted, onCancellation = null)
        }
    }
}
