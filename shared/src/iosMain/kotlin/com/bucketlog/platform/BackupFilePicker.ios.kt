package com.bucketlog.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.darwin.NSObject

private const val ZIP_UTI = "public.zip-archive"

@OptIn(ExperimentalForeignApi::class)
private class ExportDelegate(
    private val onFinished: (Boolean) -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        controller.dismissViewControllerAnimated(true, completion = null)
        onFinished(didPickDocumentsAtURLs.isNotEmpty())
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        controller.dismissViewControllerAnimated(true, completion = null)
        onFinished(false)
    }
}

@OptIn(ExperimentalForeignApi::class)
private class ImportDelegate(
    private val onFinished: (NSURL?) -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        controller.dismissViewControllerAnimated(true, completion = null)
        onFinished(didPickDocumentsAtURLs.firstOrNull() as? NSURL)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        controller.dismissViewControllerAnimated(true, completion = null)
        onFinished(null)
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberBackupExporter(onResult: (Boolean) -> Unit): (fileName: String, bytes: ByteArray) -> Unit {
    val rootViewController = LocalUIViewController.current
    var delegateRef by remember { mutableStateOf<ExportDelegate?>(null) }

    return { fileName, bytes ->
        val tempPath = NSTemporaryDirectory() + fileName
        NSFileManager.defaultManager.createFileAtPath(tempPath, contents = bytes.toNSData(), attributes = null)
        val fileURL = NSURL.fileURLWithPath(tempPath)

        val delegate = ExportDelegate { success ->
            onResult(success)
            delegateRef = null
        }
        delegateRef = delegate

        val picker = UIDocumentPickerViewController(uRL = fileURL, inMode = UIDocumentPickerMode.UIDocumentPickerModeExportToService)
        picker.delegate = delegate
        rootViewController.presentViewController(picker, animated = true, completion = null)
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberBackupImporter(onResult: (ByteArray?) -> Unit): () -> Unit {
    val rootViewController = LocalUIViewController.current
    var delegateRef by remember { mutableStateOf<ImportDelegate?>(null) }

    return {
        val delegate = ImportDelegate { url ->
            val bytes = url?.let { pickedURL ->
                val started = pickedURL.startAccessingSecurityScopedResource()
                try {
                    pickedURL.path?.let { NSFileManager.defaultManager.contentsAtPath(it) }?.toByteArray()
                } finally {
                    if (started) pickedURL.stopAccessingSecurityScopedResource()
                }
            }
            onResult(bytes)
            delegateRef = null
        }
        delegateRef = delegate

        val picker = UIDocumentPickerViewController(
            documentTypes = listOf(ZIP_UTI),
            inMode = UIDocumentPickerMode.UIDocumentPickerModeImport,
        )
        picker.delegate = delegate
        rootViewController.presentViewController(picker, animated = true, completion = null)
    }
}
