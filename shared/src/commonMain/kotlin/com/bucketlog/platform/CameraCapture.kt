package com.bucketlog.platform

import androidx.compose.runtime.Composable

/**
 * 커스텀 프리뷰 없이 시스템 카메라 앱을 호출한다(Android: TakePicture, iOS: UIImagePickerController).
 * 반환된 람다를 호출하면 카메라 앱이 뜨고, 결과(원본 JPEG 바이트, 취소 시 null)는 onResult로 온다.
 */
@Composable
expect fun rememberCameraCapture(onResult: (ByteArray?) -> Unit): () -> Unit
