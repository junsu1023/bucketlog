package com.bucketlog.platform

import androidx.compose.runtime.Composable

/**
 * 백업 zip 저장 위치를 고르는 시스템 피커(Android: SAF, iOS: UIDocumentPicker).
 * 반환된 람다를 호출하면 피커가 뜨고, 저장이 끝나면 onResult(성공 여부)가 불린다.
 */
@Composable
expect fun rememberBackupExporter(onResult: (Boolean) -> Unit): (fileName: String, bytes: ByteArray) -> Unit

/** 백업 zip 파일을 고르는 시스템 피커. 선택/읽기에 실패하면 onResult(null). */
@Composable
expect fun rememberBackupImporter(onResult: (ByteArray?) -> Unit): () -> Unit
