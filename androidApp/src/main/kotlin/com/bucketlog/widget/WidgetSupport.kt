package com.bucketlog.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.bucketlog.MainActivity
import com.bucketlog.R
import com.bucketlog.presentation.theme.BucketLogColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 위젯 3종("오늘의 한 걸음"/"올해의 나"/"오늘의 추억")이 공유하는 뼈대. Glance는 Compose
 * Multiplatform 컴포지션과 별개의 트리라 CMP의 `Res.string`/`MaterialTheme`을 그대로 못 쓴다 —
 * 색상은 shared의 `BucketLogColors`(공개 object)를 직접 참조해 팔레트만이라도 어긋나지 않게
 * 맞춘다. 이 위젯은 Todo 위젯이 아니다 — 정보량보다 여백과 한 가지 메시지의 명확성을 우선한다.
 */
object WidgetColors {
    val background = BucketLogColors.LightBackground
    val cardBackground = BucketLogColors.LightSurface
    val recommendCard = BucketLogColors.LightGoalCard
    val onSurface = BucketLogColors.LightOnSurface
    val onSurfaceVariant = BucketLogColors.LightOnSurfaceVariant
    val accent = BucketLogColors.LightAccent
    val track = Color(0xFFEFEBE4)
}

val WidgetCardRadius = 20.dp

/** 위젯을 탭하면 목표 상세의 퀵 체크인 필드로 바로 들어간다 — 딥링크는 기존 알림과 동일한 규칙. */
fun goalCheckInDeepLink(goalId: String): String = "bucketlog://goal/$goalId?focus=checkin"

/** "추억 카드"를 탭하면 기록을 남기러 온 게 아니라 보러 온 것이므로 체크인 포커스 없이 연다. */
fun goalDetailDeepLink(goalId: String): String = "bucketlog://goal/$goalId"

fun goalDeepLinkIntent(context: Context, deepLink: String): Intent =
    Intent(context, MainActivity::class.java).apply {
        data = Uri.parse(deepLink)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

internal fun homeIntent(context: Context): Intent =
    Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }

/** 위젯 안에서 보여줄 대상이 없을 때 공통으로 쓰는 빈 상태 — 탭하면 앱 홈으로 자연스럽게 연결. */
@Composable
fun WidgetEmptyState(title: String, body: String? = null) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.cardBackground)
            .cornerRadius(WidgetCardRadius)
            .padding(20.dp)
            .clickable(actionStartActivity(homeIntent(context))),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        WidgetMascotImage(size = 40.dp)
        Text(
            text = title,
            style = TextStyle(
                color = ColorProvider(WidgetColors.onSurfaceVariant),
                fontSize = 14.sp,
                textAlign = androidx.glance.text.TextAlign.Center,
            ),
            modifier = GlanceModifier.padding(top = 8.dp),
        )
        if (body != null) {
            Text(
                text = body,
                style = TextStyle(
                    color = ColorProvider(WidgetColors.onSurfaceVariant),
                    fontSize = 13.sp,
                    textAlign = androidx.glance.text.TextAlign.Center,
                ),
                modifier = GlanceModifier.padding(top = 4.dp),
            )
        }
    }
}

/** 마스코트 "루미". 실제 일러스트 준비 전까지는 간단한 벡터 플레이스홀더(ic_mascot_rumi)를 쓴다. */
@Composable
fun WidgetMascotImage(size: androidx.compose.ui.unit.Dp) {
    Image(
        provider = ImageProvider(R.drawable.ic_mascot_rumi),
        contentDescription = null,
        modifier = GlanceModifier.size(size),
    )
}

/** 썸네일(~320px) 경로를 안전하게 비트맵으로 읽는다 — 실패하면 null(호출부가 placeholder 처리). */
suspend fun loadThumbnailBitmap(path: String): Bitmap? = withContext(Dispatchers.IO) {
    runCatching {
        val filePath = path.removePrefix("file://")
        BitmapFactory.decodeFile(filePath)
    }.getOrNull()
}

/**
 * 원형 진행률 링을 직접 그린다. Glance는 Canvas/커스텀 드로잉을 지원하지 않아(선언형 UI만
 * 가능) 비트맵을 미리 그려 `Image`로 얹는 방식으로 우회한다 — "2026년의 나" 위젯에서만 쓴다.
 */
fun drawProgressRing(progress: Float, sizeDp: Int, density: Float): Bitmap {
    val sizePx = (sizeDp * density).toInt().coerceAtLeast(1)
    val ringWidthPx = sizePx * 0.12f
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val rect = RectF(ringWidthPx / 2, ringWidthPx / 2, sizePx - ringWidthPx / 2, sizePx - ringWidthPx / 2)

    val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = ringWidthPx
        strokeCap = Paint.Cap.ROUND
        color = WidgetColors.track.toArgb()
    }
    canvas.drawArc(rect, 0f, 360f, false, trackPaint)

    val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = ringWidthPx
        strokeCap = Paint.Cap.ROUND
        color = WidgetColors.accent.toArgb()
    }
    val sweep = 360f * progress.coerceIn(0f, 1f)
    canvas.drawArc(rect, -90f, sweep, false, progressPaint)

    return bitmap
}
