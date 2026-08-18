package com.bucketlog.platform

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

const val NOTIFICATION_CHANNEL_ID = "bucketlog_default"
private const val KEY_ID = "id"
private const val KEY_TITLE = "title"
private const val KEY_BODY = "body"
private const val KEY_DEEP_LINK = "deepLink"

fun ensureNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = context.getSystemService(NotificationManager::class.java)
    // docs/DESIGN.md 톤에 맞춰 "알림"이라는 사무적인 이름 대신 앱 성격이 드러나는 이름을 쓴다.
    val channel = NotificationChannel(
        NOTIFICATION_CHANNEL_ID,
        "버킷로그 알림",
        NotificationManager.IMPORTANCE_DEFAULT,
    )
    manager.createNotificationChannel(channel)
}

/** WorkManager가 예약 시각에 실행 — 실제 시스템 알림을 띄운다. */
class LocalNotificationWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getString(KEY_ID) ?: return Result.failure()
        val title = inputData.getString(KEY_TITLE).orEmpty()
        val body = inputData.getString(KEY_BODY).orEmpty()
        val deepLink = inputData.getString(KEY_DEEP_LINK).orEmpty()

        ensureNotificationChannel(applicationContext)

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink), applicationContext, MainActivityClassHolder.clazz)
        val pendingIntent = TaskStackBuilder.create(applicationContext)
            .addNextIntentWithParentStack(intent)
            .getPendingIntent(id.hashCode(), android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)

        // TODO(6주차 스토어 제출 전): 시스템 기본 아이콘 대신 앱 전용 알림 아이콘으로 교체.
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            NotificationManagerCompat.from(applicationContext).notify(id.hashCode(), notification)
        }
        return Result.success()
    }

    companion object {
        fun inputData(notification: LocalNotification) = androidx.work.workDataOf(
            KEY_ID to notification.id,
            KEY_TITLE to notification.title,
            KEY_BODY to notification.body,
            KEY_DEEP_LINK to notification.deepLink,
        )
    }
}

/**
 * Worker는 androidMain(shared)에 있고 MainActivity는 androidApp 모듈에 있어 직접 참조할 수 없다.
 * androidApp의 Application이 시작 시 실제 Activity 클래스를 등록해 준다.
 */
object MainActivityClassHolder {
    lateinit var clazz: Class<*>
}
