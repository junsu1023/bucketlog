package com.bucketlog.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.bucketlog.MainActivity
import com.bucketlog.presentation.theme.BucketLogColors

/**
 * 위젯 3종(오늘의 기억/지금 어때요/마감 임박)이 공유하는 뼈대. Glance는 Compose Multiplatform
 * 컴포지션과 별개의 트리라 CMP의 `Res.string`/`MaterialTheme`을 그대로 못 쓴다 — 색상만이라도
 * shared의 `BucketLogColors`(공개 object)를 직접 참조해 앱 팔레트와 어긋나지 않게 맞춘다.
 * Compose Multiplatform의 Android 타깃은 실제 androidx.compose.ui.graphics.Color를 그대로
 * actual로 쓰므로 별도 변환 없이 바로 참조할 수 있다.
 */
object WidgetColors {
    val background = BucketLogColors.LightGoalCard
    val onBackground = BucketLogColors.LightOnSurface
    val onBackgroundVariant = BucketLogColors.LightOnSurfaceVariant
}

/** 위젯을 탭하면 목표 상세의 퀵 체크인 필드로 바로 들어간다 — 딥링크는 기존 알림과 동일한 규칙. */
fun goalCheckInDeepLink(goalId: String): String = "bucketlog://goal/$goalId?focus=checkin"

private fun goalDeepLinkIntent(context: Context, deepLink: String): Intent =
    Intent(context, MainActivity::class.java).apply {
        data = Uri.parse(deepLink)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

/** 위젯 안에서 보여줄 대상이 없을 때(아직 조건에 맞는 목표가 없음) 공통으로 쓰는 빈 상태. */
@Composable
fun WidgetEmptyState(message: String) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.background)
            .padding(16.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
    ) {
        Text(
            text = message,
            style = TextStyle(color = ColorProvider(WidgetColors.onBackgroundVariant), fontSize = 13.sp),
        )
    }
}

/** 위젯 3종 공통 레이아웃 — 위 라벨(회색) + 아래 본문(굵게), 탭하면 해당 목표 체크인으로 이동. */
@Composable
fun WidgetCard(label: String, body: String, deepLink: String) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.background)
            .padding(16.dp)
            .clickable(actionStartActivity(goalDeepLinkIntent(context, deepLink))),
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(WidgetColors.onBackgroundVariant),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Text(
            text = body,
            style = TextStyle(
                color = ColorProvider(WidgetColors.onBackground),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            ),
            modifier = GlanceModifier.padding(top = 6.dp),
        )
    }
}
