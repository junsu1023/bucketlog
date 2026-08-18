package com.bucketlog

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.bucketlog.platform.NotificationPermissionBridge
import org.koin.mp.KoinPlatformTools

class MainActivity : ComponentActivity() {
    private val permissionBridge: NotificationPermissionBridge
        get() = KoinPlatformTools.defaultContext().get().get()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> permissionBridge.onResult(granted) }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        permissionBridge.attach(permissionLauncher)
        handleDeepLink(intent)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    // 알림(5주차) 딥링크. docs/ARCHITECTURE.md §6 — launchMode=singleTop이라 실행 중에도 onNewIntent로 들어온다.
    private fun handleDeepLink(intent: Intent) {
        intent.data?.toString()?.let { DeepLinkHolder.push(it) }
    }

    override fun onDestroy() {
        permissionBridge.detach()
        super.onDestroy()
    }
}