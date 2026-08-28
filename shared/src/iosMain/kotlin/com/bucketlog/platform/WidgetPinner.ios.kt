package com.bucketlog.platform

import androidx.compose.runtime.Composable

@Composable
actual fun rememberWidgetPinner(): (WidgetKind) -> Unit = { /* iOS 위젯 익스텐션 준비 전까지 no-op */ }
