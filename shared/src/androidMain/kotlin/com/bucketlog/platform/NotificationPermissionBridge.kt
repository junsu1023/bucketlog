package com.bucketlog.platform

import androidx.activity.result.ActivityResultLauncher
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * POST_NOTIFICATIONS(API 33+) 런타임 권한 요청은 Activity가 있어야만 가능하다.
 * MainActivity가 onCreate에서 [attach]로 런처를 넘겨주고, [NotificationScheduler]는
 * Koin으로 주입받은 이 인스턴스를 통해 요청한다. Activity가 없는 상태([attach] 전/[detach] 후)면
 * 요청은 실패로 처리된다 — 권한 요청은 항상 화면이 보이는 상태(목표 등록 직후)에서만 일어난다.
 */
class NotificationPermissionBridge {
    private var launcher: ActivityResultLauncher<String>? = null
    private var pending: Continuation<Boolean>? = null

    fun attach(launcher: ActivityResultLauncher<String>) {
        this.launcher = launcher
    }

    fun detach() {
        launcher = null
        pending?.resume(false)
        pending = null
    }

    fun onResult(granted: Boolean) {
        pending?.resume(granted)
        pending = null
    }

    suspend fun request(): Boolean {
        val currentLauncher = launcher ?: return false
        return suspendCancellableCoroutine { continuation ->
            pending = continuation
            currentLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
