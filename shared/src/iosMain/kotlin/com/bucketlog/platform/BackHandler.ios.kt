package com.bucketlog.platform

import androidx.compose.runtime.Composable

/** iOS에는 하드웨어/시스템 백버튼이 없어 아무 동작도 하지 않는다. */
@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
}
