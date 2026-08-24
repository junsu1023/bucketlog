package com.bucketlog.platform

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}

private const val EXIT_CONFIRM_WINDOW_MILLIS = 2_000L

@Composable
actual fun ExitOnDoubleBackHandler(enabled: Boolean, message: String) {
    val context = LocalContext.current
    var lastBackPressMillis by remember { mutableLongStateOf(0L) }
    BackHandler(enabled = enabled) {
        val now = System.currentTimeMillis()
        if (now - lastBackPressMillis < EXIT_CONFIRM_WINDOW_MILLIS) {
            (context as? Activity)?.finish()
        } else {
            lastBackPressMillis = now
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
