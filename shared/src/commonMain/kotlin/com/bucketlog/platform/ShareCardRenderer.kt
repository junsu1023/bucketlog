package com.bucketlog.platform

/**
 * S-01/S-02 완료 카드를 만드는 데 필요한 값. 문자열은 commonMain에서 미리 리소스를
 * 해석해서 넘긴다(플랫폼 렌더러는 compose-resources에 접근할 수 없음).
 */
data class ShareCardRenderRequest(
    val width: Int = 1080,
    val height: Int = 1920,
    val appName: String,
    val dateText: String?,
    val goalTitle: String,
    val retrospect: String?,
    val photoBytes: ByteArray?,
)

/**
 * 완료 카드를 PNG 바이트로 직접 그린다. Compose의 GraphicsLayer 캡처(record + toImageBitmap)가
 * 이 프로젝트 환경에서 캡처된 이미지에 화면의 다른 내용이 섞여 들어가는 재현 가능한 버그가 있어,
 * Compose 컴포저블을 캡처하는 대신 각 플랫폼의 네이티브 2D 그리기 API(Android:
 * android.graphics.Canvas, iOS: Core Graphics)로 직접 합성한다.
 */
expect suspend fun renderShareCard(request: ShareCardRenderRequest): ByteArray
