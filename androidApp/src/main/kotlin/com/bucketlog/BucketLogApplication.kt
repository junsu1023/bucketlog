package com.bucketlog

import android.app.Application
import com.bucketlog.background.schedulePeriodicNudgeEvaluation
import com.bucketlog.data.local.DatabaseFactory
import com.bucketlog.di.initKoin
import com.bucketlog.domain.usecase.EvaluateNotificationsUseCase
import com.bucketlog.platform.AppSettings
import com.bucketlog.platform.FileStorage
import com.bucketlog.platform.MainActivityClassHolder
import com.bucketlog.platform.NotificationPermissionBridge
import com.bucketlog.platform.NotificationScheduler
import com.bucketlog.platform.ensureNotificationChannel
import com.bucketlog.widget.observeAndRefreshWidgets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

class BucketLogApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        MainActivityClassHolder.clazz = MainActivity::class.java
        ensureNotificationChannel(this)

        initKoin(
            platformModule = module {
                single { DatabaseFactory(this@BucketLogApplication) }
                single { FileStorage(this@BucketLogApplication) }
                single { AppSettings(this@BucketLogApplication) }
                single { NotificationPermissionBridge() }
                single { NotificationScheduler(this@BucketLogApplication, get()) }
            },
        )

        // docs/ARCHITECTURE.md §7: 알림 평가(월간회고/목표별리마인더/넛지)는 주 1회 백그라운드 +
        // 앱 실행 시에도 1회. docs/NOTIFICATIONS.md §1 우선순위는 EvaluateNotificationsUseCase가 중재한다.
        schedulePeriodicNudgeEvaluation(this)
        CoroutineScope(Dispatchers.Default).launch {
            KoinPlatformTools.defaultContext().get().get<EvaluateNotificationsUseCase>().invoke()
        }

        // 홈 화면 위젯(Phase 1) — 목표/기록이 바뀔 때마다 위젯 3종을 갱신한다.
        observeAndRefreshWidgets(this, CoroutineScope(Dispatchers.Default))
    }
}
