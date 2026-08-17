package com.bucketlog.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.darwin.NSObject

private const val IMAGE_TYPE_IDENTIFIER = "public.image"

@OptIn(ExperimentalForeignApi::class)
private class PhotoPickerDelegate(
    private val onResult: (List<ByteArray>) -> Unit,
) : NSObject(), PHPickerViewControllerDelegateProtocol {
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)

        val results = didFinishPicking.filterIsInstance<PHPickerResult>()
        if (results.isEmpty()) {
            onResult(emptyList())
            return
        }

        val collected = arrayOfNulls<ByteArray?>(results.size)
        var remaining = results.size

        results.forEachIndexed { index, result ->
            val provider = result.itemProvider
            if (!provider.hasItemConformingToTypeIdentifier(IMAGE_TYPE_IDENTIFIER)) {
                remaining -= 1
                if (remaining == 0) onResult(collected.filterNotNull())
                return@forEachIndexed
            }
            provider.loadDataRepresentationForTypeIdentifier(IMAGE_TYPE_IDENTIFIER) { data, _ ->
                collected[index] = data?.toByteArray()
                remaining -= 1
                if (remaining == 0) onResult(collected.filterNotNull())
            }
        }
    }
}

@Composable
actual fun rememberPhotoPicker(maxItems: Int, onResult: (List<ByteArray>) -> Unit): () -> Unit {
    val rootViewController = LocalUIViewController.current
    var delegateRef by remember { mutableStateOf<PhotoPickerDelegate?>(null) }

    return {
        val delegate = PhotoPickerDelegate { photos ->
            onResult(photos)
            delegateRef = null
        }
        delegateRef = delegate

        val config = PHPickerConfiguration()
        config.selectionLimit = maxItems.toLong()
        config.filter = PHPickerFilter.imagesFilter()

        val picker = PHPickerViewController(configuration = config)
        picker.delegate = delegate
        rootViewController.presentViewController(picker, animated = true, completion = null)
    }
}
