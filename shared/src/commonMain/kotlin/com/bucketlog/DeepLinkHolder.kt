package com.bucketlog

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 알림 탭으로 들어온 딥링크를 App()이 관찰할 수 있게 넘겨주는 다리.
 * docs/ARCHITECTURE.md §6 딥링크 — 지금은 bucketlog://goal/{id}?focus=checkin 하나만 처리한다.
 * Android(MainActivity.onCreate/onNewIntent)와 iOS(.onOpenURL)가 각자 [push]를 호출한다.
 */
object DeepLinkHolder {
    val pending = MutableStateFlow<String?>(null)

    fun push(uri: String) {
        pending.value = uri
    }

    fun consume() {
        pending.value = null
    }
}

/** bucketlog://goal/{id}?focus=checkin -> (goalId, focusCheckIn) */
fun parseGoalDeepLink(uri: String): Pair<String, Boolean>? {
    val prefix = "bucketlog://goal/"
    if (!uri.startsWith(prefix)) return null
    val remainder = uri.removePrefix(prefix)
    val goalId = remainder.substringBefore('?')
    val query = remainder.substringAfter('?', missingDelimiterValue = "")
    if (goalId.isBlank()) return null
    val focusCheckIn = query.split('&').any { it == "focus=checkin" }
    return goalId to focusCheckIn
}
