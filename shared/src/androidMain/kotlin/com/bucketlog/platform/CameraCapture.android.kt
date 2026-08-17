package com.bucketlog.platform

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

@Composable
actual fun rememberCameraCapture(onResult: (ByteArray?) -> Unit): () -> Unit {
    val context = LocalContext.current
    var pendingFile by remember { mutableStateOf<File?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val file = pendingFile
        pendingFile = null
        if (success && file != null) {
            onResult(file.readBytes())
        } else {
            onResult(null)
        }
        file?.delete()
    }

    return {
        val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        pendingFile = file
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        launcher.launch(uri)
    }
}
