package com.bucketlog.platform

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

private const val WORK_TAG = "bucketlog_notification"

actual class NotificationScheduler(
    private val context: Context,
    private val permissionBridge: NotificationPermissionBridge,
) {
    actual suspend fun schedule(notification: LocalNotification) {
        val delay = (notification.scheduledAt.toEpochMilliseconds() - Clock.System.now().toEpochMilliseconds())
            .milliseconds
            .coerceAtLeast(kotlin.time.Duration.ZERO)
        val request = OneTimeWorkRequestBuilder<LocalNotificationWorker>()
            .setInitialDelay(delay.inWholeMilliseconds, java.util.concurrent.TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG)
            .setInputData(LocalNotificationWorker.inputData(notification))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(notification.id, ExistingWorkPolicy.REPLACE, request)
    }

    actual suspend fun cancel(id: String) {
        WorkManager.getInstance(context).cancelUniqueWork(id)
        NotificationManagerCompat.from(context).cancel(id.hashCode())
    }

    actual suspend fun cancelAll() {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
        NotificationManagerCompat.from(context).cancelAll()
    }

    actual suspend fun hasPermission(): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    actual suspend fun requestPermission(): Boolean {
        if (hasPermission()) return true
        // API 33 미만은 POST_NOTIFICATIONS 런타임 권한 자체가 없다 — 시스템 다이얼로그를 띄울 수 없으므로
        // 현재 상태(설정에서 꺼져 있으면 false)를 그대로 반환한다.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return hasPermission()
        return permissionBridge.request()
    }
}
