package com.bucketlog.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImage
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
private class CameraDelegate(
    private val onFinished: (ByteArray?) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        onFinished(image?.let { UIImageJPEGRepresentation(it, 0.95)?.toByteArray() })
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        onFinished(null)
    }
}

@Composable
actual fun rememberCameraCapture(onResult: (ByteArray?) -> Unit): () -> Unit {
    val rootViewController = LocalUIViewController.current
    var delegateRef by remember { mutableStateOf<CameraDelegate?>(null) }

    return {
        val delegate = CameraDelegate { bytes ->
            onResult(bytes)
            delegateRef = null
        }
        delegateRef = delegate

        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        picker.delegate = delegate
        rootViewController.presentViewController(picker, animated = true, completion = null)
    }
}
