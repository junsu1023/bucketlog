package com.bucketlog.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.bucketlog.R
import com.bucketlog.domain.usecase.GetThrowbackUseCase
import com.bucketlog.domain.usecase.ThrowbackPick
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.mp.KoinPlatformTools

/**
 * 위젯 "오늘의 추억"(Large, 4x4). H-07 작년 오늘의 위젯 버전 — 이 앱의 핵심 차별점인
 * "추억"을 사진과 함께 보여준다. 사진은 원본이 아니라 이미 만들어진 썸네일(~320px)만
 * 읽어서 위젯 로딩 비용을 최소화한다([loadThumbnailBitmap]).
 */
class TodayMemoryWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val getThrowback = KoinPlatformTools.defaultContext().get().get<GetThrowbackUseCase>()
        val pick = runCatching { getThrowback() }.getOrNull()
        val photoBitmap = pick?.photoPath?.let { loadThumbnailBitmap(it) }

        provideContent {
            if (pick == null) {
                WidgetEmptyState(
                    title = context.getString(R.string.widget_today_memory_empty_title),
                    body = context.getString(R.string.widget_today_memory_empty_body),
                )
            } else {
                TodayMemoryContent(pick = pick, photoBitmap = photoBitmap)
            }
        }
    }
}

@Composable
private fun TodayMemoryContent(pick: ThrowbackPick, photoBitmap: Bitmap?) {
    val context = LocalContext.current
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.cardBackground)
            .cornerRadius(WidgetCardRadius)
            .padding(18.dp)
            .clickable(actionStartActivity(goalDeepLinkIntent(context, goalDetailDeepLink(pick.goalId)))),
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Vertical.Top) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = context.getString(R.string.widget_today_memory_title),
                    style = TextStyle(
                        color = ColorProvider(WidgetColors.onSurface),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text = if (pick.isYearAgo) {
                        context.getString(R.string.widget_today_memory_year_ago)
                    } else {
                        context.getString(R.string.widget_today_memory_month_ago)
                    },
                    style = TextStyle(color = ColorProvider(WidgetColors.onSurfaceVariant), fontSize = 12.sp),
                    modifier = GlanceModifier.padding(top = 2.dp),
                )
            }
            Text(
                text = context.getString(R.string.widget_today_memory_calendar_format, today.monthNumber, today.dayOfMonth),
                style = TextStyle(
                    color = ColorProvider(WidgetColors.onSurface),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = GlanceModifier
                    .background(WidgetColors.recommendCard)
                    .cornerRadius(10.dp)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }

        Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight().padding(top = 12.dp)) {
            if (photoBitmap != null) {
                Image(
                    provider = ImageProvider(photoBitmap),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight()
                        .cornerRadius(14.dp),
                )
                Spacer(modifier = GlanceModifier.width(10.dp))
            }
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight()
                    .background(WidgetColors.recommendCard)
                    .cornerRadius(14.dp)
                    .padding(14.dp),
            ) {
                Text(
                    text = pick.memo ?: pick.goalTitle,
                    maxLines = 6,
                    style = TextStyle(color = ColorProvider(WidgetColors.onSurface), fontSize = 14.sp),
                )
            }
        }

        Row(modifier = GlanceModifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.Vertical.CenterVertically) {
            Text(
                text = context.getString(
                    R.string.widget_today_memory_date_format,
                    pick.recordedDate.year,
                    pick.recordedDate.monthNumber,
                    pick.recordedDate.dayOfMonth,
                ),
                style = TextStyle(color = ColorProvider(WidgetColors.onSurfaceVariant), fontSize = 12.sp),
                modifier = GlanceModifier.defaultWeight(),
            )
            WidgetMascotImage(size = 32.dp)
        }
    }
}

class TodayMemoryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayMemoryWidget()
}
