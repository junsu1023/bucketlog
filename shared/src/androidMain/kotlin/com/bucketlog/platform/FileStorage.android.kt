package com.bucketlog.platform

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class FileStorage(private val context: Context) {
    private val photosDir: File
        get() = File(context.filesDir, "photos").apply { if (!exists()) mkdirs() }

    actual suspend fun writePhoto(photoId: String, display: ByteArray, thumbnail: ByteArray): PhotoPaths =
        withContext(Dispatchers.IO) {
            val displayFile = File(photosDir, "$photoId.jpg")
            val thumbnailFile = File(photosDir, "${photoId}_thumb.jpg")
            displayFile.writeBytes(display)
            thumbnailFile.writeBytes(thumbnail)
            PhotoPaths(
                displayPath = "photos/${displayFile.name}",
                thumbnailPath = "photos/${thumbnailFile.name}",
            )
        }

    actual suspend fun delete(relativePath: String) {
        withContext(Dispatchers.IO) {
            File(context.filesDir, relativePath).delete()
        }
    }

    actual fun resolveAbsolutePath(relativePath: String): String =
        File(context.filesDir, relativePath).absolutePath

    actual suspend fun readBytes(relativePath: String): ByteArray? = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, relativePath)
        if (file.exists()) file.readBytes() else null
    }

    actual suspend fun writeBytes(relativePath: String, bytes: ByteArray) {
        withContext(Dispatchers.IO) {
            val file = File(context.filesDir, relativePath)
            file.parentFile?.let { if (!it.exists()) it.mkdirs() }
            file.writeBytes(bytes)
        }
    }
}
