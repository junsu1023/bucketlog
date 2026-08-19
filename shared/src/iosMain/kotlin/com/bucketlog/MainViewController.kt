package com.bucketlog

import androidx.compose.ui.window.ComposeUIViewController
import com.bucketlog.data.local.DatabaseFactory
import com.bucketlog.di.initKoin
import com.bucketlog.domain.usecase.EvaluateNotificationsUseCase
import com.bucketlog.platform.AppSettings
import com.bucketlog.platform.FileStorage
import com.bucketlog.platform.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

private fun ensureKoinStarted() {
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return
    initKoin(
        platformModule = module {
            single { DatabaseFactory() }
            single { FileStorage() }
            single { AppSettings() }
            single { NotificationScheduler() }
        },
    )
    // docs/ARCHITECTURE.md §7: iOS는 백그라운드 실행이 보장되지 않아 앱 실행 시점에 1회 평가한다.
    CoroutineScope(Dispatchers.Default).launch {
        KoinPlatformTools.defaultContext().get().get<EvaluateNotificationsUseCase>().invoke()
    }
}

fun MainViewController() = ComposeUIViewController {
    ensureKoinStarted()
    App()
}

/** iOSApp.swift의 .onOpenURL에서 호출 — 알림(5주차) 딥링크. docs/ARCHITECTURE.md §6 */
fun handleDeepLink(uri: String) {
    DeepLinkHolder.push(uri)
}