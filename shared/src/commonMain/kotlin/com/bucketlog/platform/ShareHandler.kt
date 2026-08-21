package com.bucketlog.platform

import androidx.compose.runtime.Composable

/**
 * S-01 완료 카드 이미지 공유. docs/ARCHITECTURE.md §3 — Android `Intent.ACTION_SEND` /
 * iOS `UIActivityViewController`. 반환된 람다를 호출하면 시스템 공유 시트가 뜬다.
 */
@Composable
expect fun rememberShareImage(): (fileName: String, bytes: ByteArray) -> Unit
