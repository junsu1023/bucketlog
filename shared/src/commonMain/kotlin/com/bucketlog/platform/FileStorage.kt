package com.bucketlog.platform

/** 앱 전용 디렉토리에 저장된 사진의 상대 경로 쌍. docs/DATA-MODEL.md의 photos.path/thumbnail_path에 대응. */
data class PhotoPaths(val displayPath: String, val thumbnailPath: String)

/**
 * 앱 전용 디렉토리(Android: filesDir, iOS: Documents)에 표시용/썸네일 JPEG를 저장한다.
 * 갤러리에 노출되지 않는다. 경로는 상대 경로로 주고받는다 — 절대 경로를 저장하면
 * 클라우드 이전(Phase 1) 때 전부 재작성해야 한다(docs/ARCHITECTURE.md §9).
 */
// 생성자를 선언하지 않는다 — Android는 Context, iOS는 무인자로 각기 다르게 구성한다
// (DatabaseFactory와 동일한 패턴). 인스턴스는 Koin DI로만 얻는다.
expect class FileStorage {
    suspend fun writePhoto(photoId: String, display: ByteArray, thumbnail: ByteArray): PhotoPaths
    suspend fun delete(relativePath: String)
    fun resolveAbsolutePath(relativePath: String): String

    // 백업/복원(M-02)용. writePhoto와 달리 경로를 새로 만들지 않고 주어진 상대 경로 그대로 읽고 쓴다.
    suspend fun readBytes(relativePath: String): ByteArray?
    suspend fun writeBytes(relativePath: String, bytes: ByteArray)
}
