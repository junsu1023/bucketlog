package com.bucketlog

import androidx.compose.ui.window.ComposeUIViewController
import com.bucketlog.data.local.DatabaseFactory
import com.bucketlog.di.initKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

private fun ensureKoinStarted() {
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return
    initKoin(
        platformModule = module {
            single { DatabaseFactory() }
        },
    )
}

fun MainViewController() = ComposeUIViewController {
    ensureKoinStarted()
    App()
}