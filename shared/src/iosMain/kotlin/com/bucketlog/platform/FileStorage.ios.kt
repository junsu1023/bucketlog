package com.bucketlog.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual class FileStorage {
    actual suspend fun writePhoto(photoId: String, display: ByteArray, thumbnail: ByteArray): PhotoPaths =
        withContext(Dispatchers.Default) {
            ensurePhotosDirectory()
            val displayName = "$photoId.jpg"
            val thumbnailName = "${photoId}_thumb.jpg"
            writeFileBytes(display, "${photosDirectory()}/$displayName")
            writeFileBytes(thumbnail, "${photosDirectory()}/$thumbnailName")
            PhotoPaths(displayPath = "photos/$displayName", thumbnailPath = "photos/$thumbnailName")
        }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun delete(relativePath: String) {
        withContext(Dispatchers.Default) {
            NSFileManager.defaultManager.removeItemAtPath(absolutePath(relativePath), error = null)
        }
    }

    actual fun resolveAbsolutePath(relativePath: String): String = absolutePath(relativePath)

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun readBytes(relativePath: String): ByteArray? = withContext(Dispatchers.Default) {
        val path = absolutePath(relativePath)
        if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return@withContext null
        NSFileManager.defaultManager.contentsAtPath(path)?.toByteArray()
    }

    actual suspend fun writeBytes(relativePath: String, bytes: ByteArray) {
        withContext(Dispatchers.Default) {
            val path = absolutePath(relativePath)
            ensureParentDirectory(path)
            writeFileBytes(bytes, path)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun readAbsoluteBytes(absolutePath: String): ByteArray? = withContext(Dispatchers.Default) {
        if (!NSFileManager.defaultManager.fileExistsAtPath(absolutePath)) return@withContext null
        NSFileManager.defaultManager.contentsAtPath(absolutePath)?.toByteArray()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun writeFileBytes(bytes: ByteArray, path: String) {
        NSFileManager.defaultManager.createFileAtPath(path, contents = bytes.toNSData(), attributes = null)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun ensureParentDirectory(path: String) {
        val parent = path.substringBeforeLast("/")
        NSFileManager.defaultManager.createDirectoryAtPath(
            parent,
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun ensurePhotosDirectory() {
        NSFileManager.defaultManager.createDirectoryAtPath(
            photosDirectory(),
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        )
    }

    private fun photosDirectory(): String = documentsDirectory() + "/photos"

    private fun absolutePath(relativePath: String): String = "${documentsDirectory()}/$relativePath"

    @OptIn(ExperimentalForeignApi::class)
    private fun documentsDirectory(): String {
        val directory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        return requireNotNull(directory?.path) { "iOS documents 디렉토리를 찾을 수 없음" }
    }
}
