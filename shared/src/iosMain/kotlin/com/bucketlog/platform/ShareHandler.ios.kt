package com.bucketlog.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberShareImage(): (fileName: String, bytes: ByteArray) -> Unit {
    val rootViewController = LocalUIViewController.current
    return { fileName, bytes ->
        val tempPath = NSTemporaryDirectory() + fileName
        NSFileManager.defaultManager.createFileAtPath(tempPath, contents = bytes.toNSData(), attributes = null)
        val fileURL = NSURL.fileURLWithPath(tempPath)
        val activityController = UIActivityViewController(activityItems = listOf(fileURL), applicationActivities = null)
        rootViewController.presentViewController(activityController, animated = true, completion = null)
    }
}
