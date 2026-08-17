package com.bucketlog.platform

import androidx.compose.runtime.Composable

/**
 * 시스템 갤러리 선택기를 호출한다(Android: Photo Picker, iOS: PHPickerViewController).
 * 둘 다 별도 권한 프롬프트 없이 동작한다. maxItems까지 다중 선택 가능.
 */
@Composable
expect fun rememberPhotoPicker(maxItems: Int, onResult: (List<ByteArray>) -> Unit): () -> Unit
