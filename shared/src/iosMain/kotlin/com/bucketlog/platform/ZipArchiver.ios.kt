package com.bucketlog.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * libarchive cinterop 없이 압축 없는(STORED) zip을 직접 읽고 쓴다.
 * 사진은 이미 JPEG로 압축돼 있고 JSON도 작아 압축 생략에 따른 크기 손해가 미미하다.
 * Android(ZipArchiver.android.kt)도 STORED로 통일해 두 플랫폼이 서로 만든 zip을 그대로 연다.
 * ZIP 포맷: 로컬 파일 헤더 + 데이터(엔트리마다) → 중앙 디렉토리 → EOCD. 전부 little-endian.
 */
actual class ZipArchiver {
    actual suspend fun zip(entries: List<ZipEntryData>): ByteArray = withContext(Dispatchers.Default) {
        val out = ByteWriter()
        val localHeaderOffsets = IntArray(entries.size)

        entries.forEachIndexed { index, entry ->
            localHeaderOffsets[index] = out.size
            val nameBytes = entry.path.encodeToByteArray()
            val crc = crc32(entry.bytes)

            out.writeU32(LOCAL_FILE_HEADER_SIGNATURE)
            out.writeU16(20) // version needed to extract
            out.writeU16(0) // general purpose bit flag
            out.writeU16(0) // compression method: STORED
            out.writeU16(0) // last mod file time
            out.writeU16(0) // last mod file date
            out.writeU32(crc)
            out.writeU32(entry.bytes.size.toLong()) // compressed size
            out.writeU32(entry.bytes.size.toLong()) // uncompressed size
            out.writeU16(nameBytes.size)
            out.writeU16(0) // extra field length
            out.writeRaw(nameBytes)
            out.writeRaw(entry.bytes)
        }

        val centralDirectoryStart = out.size
        entries.forEachIndexed { index, entry ->
            val nameBytes = entry.path.encodeToByteArray()
            val crc = crc32(entry.bytes)

            out.writeU32(CENTRAL_DIRECTORY_SIGNATURE)
            out.writeU16(20) // version made by
            out.writeU16(20) // version needed to extract
            out.writeU16(0) // general purpose bit flag
            out.writeU16(0) // compression method: STORED
            out.writeU16(0) // last mod file time
            out.writeU16(0) // last mod file date
            out.writeU32(crc)
            out.writeU32(entry.bytes.size.toLong()) // compressed size
            out.writeU32(entry.bytes.size.toLong()) // uncompressed size
            out.writeU16(nameBytes.size)
            out.writeU16(0) // extra field length
            out.writeU16(0) // file comment length
            out.writeU16(0) // disk number start
            out.writeU16(0) // internal file attributes
            out.writeU32(0) // external file attributes
            out.writeU32(localHeaderOffsets[index].toLong())
            out.writeRaw(nameBytes)
        }
        val centralDirectorySize = out.size - centralDirectoryStart

        out.writeU32(END_OF_CENTRAL_DIRECTORY_SIGNATURE)
        out.writeU16(0) // number of this disk
        out.writeU16(0) // disk with start of central directory
        out.writeU16(entries.size) // entries on this disk
        out.writeU16(entries.size) // total entries
        out.writeU32(centralDirectorySize.toLong())
        out.writeU32(centralDirectoryStart.toLong())
        out.writeU16(0) // comment length

        out.toByteArray()
    }

    actual suspend fun unzip(zipBytes: ByteArray): List<ZipEntryData> = withContext(Dispatchers.Default) {
        val result = mutableListOf<ZipEntryData>()
        val reader = ByteReader(zipBytes)

        while (reader.hasRemaining(4) && reader.peekU32() == LOCAL_FILE_HEADER_SIGNATURE) {
            reader.readU32() // signature
            reader.readU16() // version needed to extract
            reader.readU16() // general purpose bit flag
            reader.readU16() // compression method (STORED만 지원)
            reader.readU16() // last mod file time
            reader.readU16() // last mod file date
            reader.readU32() // crc-32
            val compressedSize = reader.readU32().toInt()
            reader.readU32() // uncompressed size
            val nameLength = reader.readU16()
            val extraLength = reader.readU16()
            val name = reader.readRaw(nameLength).decodeToString()
            reader.readRaw(extraLength)
            val data = reader.readRaw(compressedSize)
            result += ZipEntryData(name, data)
        }
        result
    }
}

private const val LOCAL_FILE_HEADER_SIGNATURE = 0x04034b50L
private const val CENTRAL_DIRECTORY_SIGNATURE = 0x02014b50L
private const val END_OF_CENTRAL_DIRECTORY_SIGNATURE = 0x06054b50L

private class ByteWriter {
    private val bytes = mutableListOf<Byte>()
    val size: Int get() = bytes.size

    fun writeU16(value: Int) {
        bytes.add((value and 0xFF).toByte())
        bytes.add(((value shr 8) and 0xFF).toByte())
    }

    fun writeU32(value: Long) {
        bytes.add((value and 0xFF).toByte())
        bytes.add(((value shr 8) and 0xFF).toByte())
        bytes.add(((value shr 16) and 0xFF).toByte())
        bytes.add(((value shr 24) and 0xFF).toByte())
    }

    fun writeRaw(data: ByteArray) {
        data.forEach { bytes.add(it) }
    }

    fun toByteArray(): ByteArray = bytes.toByteArray()
}

private class ByteReader(private val data: ByteArray) {
    private var pos = 0

    fun hasRemaining(count: Int): Boolean = pos + count <= data.size

    fun peekU32(): Long {
        val saved = pos
        val value = readU32()
        pos = saved
        return value
    }

    fun readU16(): Int {
        val value = (data[pos].toInt() and 0xFF) or ((data[pos + 1].toInt() and 0xFF) shl 8)
        pos += 2
        return value
    }

    fun readU32(): Long {
        val value = (data[pos].toLong() and 0xFF) or
            ((data[pos + 1].toLong() and 0xFF) shl 8) or
            ((data[pos + 2].toLong() and 0xFF) shl 16) or
            ((data[pos + 3].toLong() and 0xFF) shl 24)
        pos += 4
        return value
    }

    fun readRaw(count: Int): ByteArray {
        val out = data.copyOfRange(pos, pos + count)
        pos += count
        return out
    }
}

private val CRC_TABLE = IntArray(256) { i ->
    var c = i
    repeat(8) {
        c = if (c and 1 != 0) (c ushr 1) xor CRC32_POLYNOMIAL else c ushr 1
    }
    c
}

private const val CRC32_POLYNOMIAL = -0x12477ce0 // 0xEDB88320 as Int

private fun crc32(input: ByteArray): Long {
    var crc = -1 // 0xFFFFFFFF
    for (b in input) {
        val index = (crc xor b.toInt()) and 0xFF
        crc = (crc ushr 8) xor CRC_TABLE[index]
    }
    return (crc.toLong() xor -1L) and 0xFFFFFFFFL
}
