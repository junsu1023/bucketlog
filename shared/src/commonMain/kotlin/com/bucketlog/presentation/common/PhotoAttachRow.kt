package com.bucketlog.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.photo_camera
import bucketlog.shared.generated.resources.photo_clear
import bucketlog.shared.generated.resources.photo_gallery
import bucketlog.shared.generated.resources.photo_selected_count
import com.bucketlog.presentation.theme.MonoLabel
import org.jetbrains.compose.resources.stringResource

/** 진행 기록 다이얼로그(홈)와 목표 생성 화면이 함께 쓰는 사진 촬영/선택 버튼 행. */
@Composable
fun PhotoAttachRow(
    photoCount: Int,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(onClick = onCameraClick, enabled = photoCount < 5) {
            Text(stringResource(Res.string.photo_camera))
        }
        TextButton(onClick = onGalleryClick, enabled = photoCount < 5) {
            Text(stringResource(Res.string.photo_gallery))
        }
        Text(
            text = stringResource(Res.string.photo_selected_count, photoCount, 5),
            style = MaterialTheme.typography.bodySmall.merge(MonoLabel),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (photoCount > 0) {
            TextButton(onClick = onClearClick) {
                Text(stringResource(Res.string.photo_clear), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
