package com.bucketlog.platform

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberBackupExporter(onResult: (Boolean) -> Unit): (fileName: String, bytes: ByteArray) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingBytes by remember { mutableStateOf<ByteArray?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        val bytes = pendingBytes
        pendingBytes = null
        if (uri == null || bytes == null) {
            onResult(false)
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val success = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                        ?: error("no output stream")
                }.isSuccess
            }
            onResult(success)
        }
    }

    return { fileName, bytes ->
        pendingBytes = bytes
        launcher.launch(fileName)
    }
}

@Composable
actual fun rememberBackupImporter(onResult: (ByteArray?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            onResult(null)
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val bytes = withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
            }
            onResult(bytes)
        }
    }

    return { launcher.launch(arrayOf("application/zip", "application/octet-stream")) }
}
