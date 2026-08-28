package com.bucketlog.platform

import androidx.compose.runtime.Composable

/** iOS에는 하드웨어/시스템 백버튼이 없어 아무 동작도 하지 않는다. */
@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
}

/** iOS에는 뒤로가기로 앱을 종료하는 개념 자체가 없다(시스템 멀티태스킹에 맡김). */
@Composable
actual fun ExitOnDoubleBackHandler(enabled: Boolean, message: String) {
}
