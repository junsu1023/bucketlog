package com.bucketlog.platform

/** zip 안의 파일 하나. path는 zip 내부 상대 경로(예: "photos/abc.jpg"), bytes는 압축 전 원본 내용. */
data class ZipEntryData(val path: String, val bytes: ByteArray)

/**
 * 백업/복원(M-02)용 zip 압축·해제. docs/ARCHITECTURE.md §3에 정의된 경계.
 * Context가 필요 없어 Android/iOS 모두 무인자 생성자다.
 */
expect class ZipArchiver() {
    suspend fun zip(entries: List<ZipEntryData>): ByteArray
    suspend fun unzip(zipBytes: ByteArray): List<ZipEntryData>
}
