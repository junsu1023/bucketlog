package com.bucketlog.presentation.goaldetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.app_name
import bucketlog.shared.generated.resources.photo_viewer_close
import bucketlog.shared.generated.resources.share_card_date
import bucketlog.shared.generated.resources.share_dialog_share
import coil3.compose.AsyncImage
import com.bucketlog.platform.AppBackHandler
import com.bucketlog.platform.FileStorage
import com.bucketlog.platform.ShareCardRenderRequest
import com.bucketlog.platform.renderShareCard
import com.bucketlog.platform.rememberShareImage
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * S-01/S-02 완료 카드 미리보기. 9:16 단일 레이아웃으로 인스타 스토리 규격까지 함께
 * 만족시킨다(docs/MVP-SCOPE.md "여유가 없다면 단일 레이아웃만이라도"). docs/DESIGN.md
 * "사진이 주인공" 원칙. 실제 공유 이미지는 이 컴포저블을 캡처하지 않고 별도로
 * [renderShareCard]가 네이티브 2D API로 다시 그린다 — 미리보기는 사용자가 눈으로
 * 확인하는 용도일 뿐, 공유되는 파일의 소스가 아니다.
 */
@Composable
private fun ShareCardContent(
    goalTitle: String,
    completedAt: Instant?,
    retrospect: String?,
    photoPath: String?,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.aspectRatio(9f / 16f).background(MaterialTheme.colorScheme.surfaceVariant)) {
        if (photoPath != null) {
            AsyncImage(
                model = photoPath,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                    startY = 400f,
                ),
            ),
        )
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.align(Alignment.TopStart).padding(20.dp),
        )
        Column(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(20.dp)) {
            if (completedAt != null) {
                val date = completedAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
                Text(
                    text = stringResource(Res.string.share_card_date, date.year, date.monthNumber),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
            Text(
                text = goalTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                modifier = Modifier.padding(top = 4.dp),
            )
            retrospect?.takeIf { it.isNotBlank() }?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
internal fun ShareCardOverlay(
    goalTitle: String,
    completedAt: Instant?,
    retrospect: String?,
    photoPath: String?,
    onDismiss: () -> Unit,
) {
    AppBackHandler(enabled = true, onBack = onDismiss)
    val scope = rememberCoroutineScope()
    val shareImage = rememberShareImage()
    val fileStorage: FileStorage = koinInject()
    var isSharing by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        ShareCardContent(
            goalTitle = goalTitle,
            completedAt = completedAt,
            retrospect = retrospect,
            photoPath = photoPath,
            modifier = Modifier.fillMaxWidth(0.85f).clip(RoundedCornerShape(16.dp)),
        )

        Column(modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp)) {
            if (isSharing) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp),
                    color = Color.White,
                )
            }
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primary),
            ) {
                TextButton(
                    enabled = !isSharing,
                    onClick = {
                        isSharing = true
                        scope.launch {
                            val appName = getString(Res.string.app_name)
                            val dateText = completedAt?.let {
                                val date = it.toLocalDateTime(TimeZone.currentSystemDefault()).date
                                getString(Res.string.share_card_date, date.year, date.monthNumber)
                            }
                            val photoBytes = photoPath?.removePrefix("file://")?.let { path ->
                                fileStorage.readAbsoluteBytes(path)
                            }
                            val bytes = renderShareCard(
                                ShareCardRenderRequest(
                                    appName = appName,
                                    dateText = dateText,
                                    goalTitle = goalTitle,
                                    retrospect = retrospect,
                                    photoBytes = photoBytes,
                                ),
                            )
                            shareImage("bucketlog_완료.png", bytes)
                            isSharing = false
                        }
                    },
                ) {
                    Text(
                        stringResource(Res.string.share_dialog_share),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
            ) {
                Text(stringResource(Res.string.photo_viewer_close), color = Color.White)
            }
        }
    }
}
