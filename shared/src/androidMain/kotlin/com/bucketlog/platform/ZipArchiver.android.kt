package com.bucketlog.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

actual class ZipArchiver {
    // iOS는 cinterop 없이 압축 없는(STORED) zip을 직접 구현한다(ZipArchiver.ios.kt 참고).
    // 두 플랫폼이 서로 만든 zip을 그대로 열 수 있어야 해서(기기 교체 시나리오) Android도
    // DEFLATE 대신 STORED로 통일한다 — 사진은 이미 JPEG라 압축 이득도 크지 않다.
    actual suspend fun zip(entries: List<ZipEntryData>): ByteArray = withContext(Dispatchers.Default) {
        val buffer = ByteArrayOutputStream()
        ZipOutputStream(buffer).use { zipOut ->
            entries.forEach { entry ->
                val zipEntry = ZipEntry(entry.path).apply {
                    method = ZipEntry.STORED
                    size = entry.bytes.size.toLong()
                    compressedSize = entry.bytes.size.toLong()
                    crc = CRC32().apply { update(entry.bytes) }.value
                }
                zipOut.putNextEntry(zipEntry)
                zipOut.write(entry.bytes)
                zipOut.closeEntry()
            }
        }
        buffer.toByteArray()
    }

    actual suspend fun unzip(zipBytes: ByteArray): List<ZipEntryData> = withContext(Dispatchers.Default) {
        val result = mutableListOf<ZipEntryData>()
        ZipInputStream(zipBytes.inputStream()).use { zipIn ->
            var entry = zipIn.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    result += ZipEntryData(entry.name, zipIn.readBytes())
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }
        result
    }
}
