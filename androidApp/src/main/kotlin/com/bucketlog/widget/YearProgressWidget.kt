package com.bucketlog.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.bucketlog.R
import com.bucketlog.domain.model.Goal
import com.bucketlog.domain.usecase.GetYearSummaryUseCase
import com.bucketlog.domain.usecase.PickRecommendedGoalUseCase
import com.bucketlog.domain.usecase.YearSummary
import com.bucketlog.presentation.theme.widgetPastelColor
import org.koin.mp.KoinPlatformTools

/**
 * 위젯 "N년의 나"(Medium, 4x2). 완료 개수를 "3개 완료"가 아니라 "추억을 만들었어요"로
 * 표현해 실적 압박이 아니라 회상으로 느껴지게 한다. 원형 진행률은 Glance가 커스텀 드로잉을
 * 지원하지 않아 비트맵으로 미리 그려 얹는다([drawProgressRing]).
 */
class YearProgressWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val koin = KoinPlatformTools.defaultContext().get()
        val summary = koin.get<GetYearSummaryUseCase>().invoke()
        val nextGoal = runCatching { koin.get<PickRecommendedGoalUseCase>().invoke() }.getOrNull()
        val density = context.resources.displayMetrics.density

        provideContent {
            if (summary.total == 0) {
                WidgetEmptyState(
                    title = context.getString(R.string.widget_year_progress_empty, summary.year),
                )
            } else {
                YearProgressContent(context, summary, nextGoal, density)
            }
        }
    }
}

@Composable
private fun YearProgressContent(context: Context, summary: YearSummary, nextGoal: Goal?, density: Float) {
    val progress = if (summary.total > 0) summary.completed.toFloat() / summary.total else 0f
    val ringSizeDp = 64
    val ringBitmap = remember(summary.completed, summary.total) { drawProgressRing(progress, ringSizeDp, density) }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.cardBackground)
            .cornerRadius(WidgetCardRadius)
            .padding(18.dp)
            .clickable(actionStartActivity(homeIntent(context))),
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = context.getString(R.string.widget_year_progress_title, summary.year),
                    style = TextStyle(
                        color = ColorProvider(WidgetColors.onSurface),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text = context.getString(R.string.widget_year_progress_subtitle),
                    style = TextStyle(color = ColorProvider(WidgetColors.onSurfaceVariant), fontSize = 12.sp),
                    modifier = GlanceModifier.padding(top = 2.dp),
                )
            }
            WidgetMascotImage(resId = WidgetMascot.GOOD, size = 36.dp)
        }

        Row(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight().padding(top = 8.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(ringBitmap),
                contentDescription = null,
                modifier = GlanceModifier.size(ringSizeDp.dp),
            )
            Column(modifier = GlanceModifier.padding(start = 4.dp)) {
                Text(
                    text = context.getString(R.string.widget_year_progress_count, summary.completed, summary.total),
                    style = TextStyle(
                        color = ColorProvider(WidgetColors.onSurface),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text = context.getString(R.string.widget_year_progress_completed_label),
                    style = TextStyle(color = ColorProvider(WidgetColors.onSurfaceVariant), fontSize = 11.sp),
                )
            }
            Spacer(modifier = GlanceModifier.width(12.dp))
            if (nextGoal != null) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = context.getString(R.string.widget_year_progress_next_label),
                        style = TextStyle(color = ColorProvider(WidgetColors.onSurfaceVariant), fontSize = 11.sp),
                    )
                    Text(
                        text = nextGoal.title,
                        maxLines = 1,
                        style = TextStyle(
                            color = ColorProvider(WidgetColors.onSurface),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(nextGoal.category.widgetPastelColor())
                            .cornerRadius(12.dp)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .clickable(
                                actionStartActivity(
                                    goalDeepLinkIntent(context, goalCheckInDeepLink(nextGoal.id)),
                                ),
                            ),
                    )
                }
            }
        }

        Text(
            text = context.getString(R.string.widget_year_progress_footer),
            style = TextStyle(color = ColorProvider(WidgetColors.onSurfaceVariant), fontSize = 11.sp),
            modifier = GlanceModifier.padding(top = 6.dp),
        )
    }
}

class YearProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = YearProgressWidget()
}
