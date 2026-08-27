package com.bucketlog.platform

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// 위젯 Receiver는 androidApp 모듈(com.bucketlog.widget 패키지)에 있다 — shared가 androidApp에
// 의존할 수 없으므로(역방향 의존) 클래스 참조 대신 완전한 클래스명 문자열로 ComponentName을 만든다.
private const val WIDGET_PACKAGE = "com.bucketlog.widget"

@Composable
actual fun rememberWidgetPinner(): (WidgetKind) -> Unit {
    val context = LocalContext.current
    return { kind ->
        val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
        val receiverClassName = when (kind) {
            WidgetKind.THROWBACK -> "$WIDGET_PACKAGE.ThrowbackWidgetReceiver"
            WidgetKind.NUDGE -> "$WIDGET_PACKAGE.NudgeWidgetReceiver"
            WidgetKind.DUE_SOON -> "$WIDGET_PACKAGE.DueSoonWidgetReceiver"
        }
        val provider = ComponentName(context.packageName, receiverClassName)
        if (appWidgetManager?.isRequestPinAppWidgetSupported == true) {
            appWidgetManager.requestPinAppWidget(provider, null, null)
        }
    }
}
