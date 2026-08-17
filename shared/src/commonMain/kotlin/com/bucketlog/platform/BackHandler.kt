package com.bucketlog.platform

import androidx.compose.runtime.Composable

/**
 * 시스템 뒤로가기(Android 하드웨어/제스처 백)를 가로채 앱 내부 네비게이션으로 처리한다.
 * enabled가 false면 시스템 기본 동작(액티비티 종료 등)을 그대로 따른다.
 */
@Composable
expect fun AppBackHandler(enabled: Boolean, onBack: () -> Unit)
