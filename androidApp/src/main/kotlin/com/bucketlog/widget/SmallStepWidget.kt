package com.bucketlog.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.bucketlog.R
import com.bucketlog.domain.usecase.PickRecommendedGoalUseCase
import com.bucketlog.presentation.theme.widgetPastelColor
import org.koin.mp.KoinPlatformTools

/**
 * 위젯 "오늘의 한 걸음"(Small, 2x2). "오늘 할 일을 또 해야 하는구나"가 아니라
 * "맞다, 나 이거 해보고 싶었지"가 떠오르게 한다 — 목표 하나 + 부드러운 CTA만.
 * [PickRecommendedGoalUseCase]는 알림처럼 "방치됨"을 경고하지 않고 항상 하나를 추천한다.
 */
class SmallStepWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val pickRecommendedGoal = KoinPlatformTools.defaultContext().get().get<PickRecommendedGoalUseCase>()
        val goal = runCatching { pickRecommendedGoal() }.getOrNull()

        provideContent {
            if (goal == null) {
                WidgetEmptyState(title = context.getString(R.string.widget_small_step_empty_title))
            } else {
                SmallStepContent(
                    goalId = goal.id,
                    title = goal.title,
                    background = goal.category.widgetPastelColor(),
                )
            }
        }
    }
}

@Composable
private fun SmallStepContent(goalId: String, title: String, background: Color) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(background)
            .cornerRadius(WidgetCardRadius)
            .padding(16.dp)
            .clickable(actionStartActivity(goalDeepLinkIntent(context, goalDetailDeepLink(goalId)))),
    ) {
        Text(
            text = context.getString(R.string.widget_small_step_label),
            style = TextStyle(
                color = ColorProvider(WidgetColors.onSurfaceVariant),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Text(
            text = title,
            maxLines = 2,
            style = TextStyle(
                color = ColorProvider(WidgetColors.onSurface),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            ),
            modifier = GlanceModifier.padding(top = 6.dp),
        )
        Row(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
            verticalAlignment = Alignment.Vertical.Bottom,
        ) {
            Text(
                text = context.getString(R.string.widget_small_step_cta),
                style = TextStyle(
                    color = ColorProvider(WidgetColors.accent),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = GlanceModifier
                    .defaultWeight()
                    .clickable(actionStartActivity(goalDeepLinkIntent(context, goalCheckInDeepLink(goalId)))),
            )
            WidgetMascotImage(size = 30.dp)
        }
    }
}

class SmallStepWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallStepWidget()
}
